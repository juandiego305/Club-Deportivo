package com.juandiego.tactica.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juandiego.tactica.data.AppTab
import com.juandiego.tactica.data.DEFAULT_ENROLLMENT
import com.juandiego.tactica.data.DEFAULT_MONTHLY
import com.juandiego.tactica.data.EventType
import com.juandiego.tactica.data.IdGen
import com.juandiego.tactica.data.LiveMatch
import com.juandiego.tactica.data.Match
import com.juandiego.tactica.data.MatchStatus
import com.juandiego.tactica.data.PaymentRecord
import com.juandiego.tactica.data.Player
import com.juandiego.tactica.data.PlayerPayments
import com.juandiego.tactica.data.Sport
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.TeamEvent
import com.juandiego.tactica.data.User
import com.juandiego.tactica.data.lineupLimit
import com.juandiego.tactica.data.remote.AuthException
import com.juandiego.tactica.data.remote.FirebaseRepository
import com.juandiego.tactica.data.todayIso
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Estado de toda la app. Los equipos se leen y guardan en Firebase (Firestore);
 * el partido en vivo se lleva en memoria y su resultado se guarda al terminar.
 */
class AppViewModel(
    private val repo: FirebaseRepository = FirebaseRepository(),
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set
    var teams by mutableStateOf<List<Team>>(emptyList())
        private set
    var teamId by mutableStateOf<Long?>(null)
        private set
    var inTeam by mutableStateOf(false)
        private set
    var tab by mutableStateOf(AppTab.INICIO)
    var liveMatches by mutableStateOf<Map<Long, LiveMatch>>(emptyMap())
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    var authLoading by mutableStateOf(false)
        private set
    /** true hasta que se cargan los equipos de la sesión guardada. */
    var loadingTeams by mutableStateOf(false)
        private set

    private var teamsJob: Job? = null

    val team: Team? get() = teams.find { it.id == teamId }

    init {
        // Firebase recuerda la sesión: si ya había un usuario, entra directo.
        repo.currentUser()?.let { startSession(it) }

        // Cronómetro de los partidos en vivo.
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (liveMatches.values.any { it.status == MatchStatus.LIVE }) {
                    liveMatches = liveMatches.mapValues { (_, m) ->
                        if (m.status == MatchStatus.LIVE) m.copy(elapsed = m.elapsed + 1) else m
                    }
                }
            }
        }
    }

    // ─── Sesión ─────────────────────────────────────────────────────────────

    fun login(email: String, password: String) = authAction { repo.login(email, password) }

    fun register(name: String, email: String, password: String) =
        authAction { repo.register(name, email, password) }

    fun clearAuthError() {
        authError = null
    }

    private fun authAction(block: suspend () -> User) {
        if (authLoading) return
        authLoading = true
        authError = null
        viewModelScope.launch {
            try {
                startSession(block())
            } catch (e: AuthException) {
                authError = e.message
            } finally {
                authLoading = false
            }
        }
    }

    private fun startSession(next: User) {
        user = next
        teamId = null
        inTeam = false
        teams = emptyList()
        loadingTeams = true
        teamsJob?.cancel()
        teamsJob = viewModelScope.launch {
            repo.observeTeams().collect {
                teams = it
                loadingTeams = false
            }
        }
    }

    fun logout() {
        teamsJob?.cancel()
        repo.logout()
        user = null
        teams = emptyList()
        teamId = null
        inTeam = false
        liveMatches = emptyMap()
    }

    // ─── Equipos ────────────────────────────────────────────────────────────

    fun enterTeam(id: Long) {
        teamId = id
        tab = AppTab.INICIO
        inTeam = true
    }

    fun backToClubs() {
        inTeam = false
    }

    fun createTeam(name: String, sport: Sport, category: String, logo: String?) {
        val team = Team(IdGen.next(), sport, name, category, logo)
        teams = teams + team
        repo.saveTeam(team)
        enterTeam(team.id)
    }

    fun editTeam(name: String, category: String, logo: String?) =
        updateTeam { it.copy(name = name, category = category, logo = logo) }

    /** Aplica el cambio en pantalla al instante y lo guarda en Firestore. */
    private fun updateTeam(change: (Team) -> Team) {
        val current = team ?: return
        val updated = change(current)
        teams = teams.map { if (it.id == updated.id) updated else it }
        repo.saveTeam(updated)
    }

    fun deleteTeam() {
        val id = teamId ?: return
        inTeam = false
        teamId = null
        teams = teams.filterNot { it.id == id }
        liveMatches = liveMatches - id
        repo.deleteTeam(id)
    }

    // ─── Agenda ─────────────────────────────────────────────────────────────

    fun addEvent(type: EventType, title: String, date: String, time: String, location: String) =
        updateTeam { t ->
            t.copy(events = (t.events + TeamEvent(IdGen.next(), type, title, date, time, location)).sortedBy { it.date })
        }

    fun deleteEvent(id: Long) = updateTeam { t -> t.copy(events = t.events.filterNot { it.id == id }) }

    // ─── Plantilla y pagos ──────────────────────────────────────────────────

    fun addPlayer(name: String, number: Int, position: String) = updateTeam { t ->
        val player = Player(
            id = IdGen.next(), name = name, number = number, position = position, available = true,
            payments = PlayerPayments(
                enrollmentPaid = false,
                enrollmentAmount = DEFAULT_ENROLLMENT,
                monthlyAmount = DEFAULT_MONTHLY,
            ),
        )
        t.copy(players = t.players + player)
    }

    fun toggleAvailable(id: Long) = updatePlayer(id) { it.copy(available = !it.available) }

    fun deletePlayer(id: Long) = updateTeam { t -> t.copy(players = t.players.filterNot { it.id == id }) }

    fun markEnrollment(playerId: Long) = updatePlayer(playerId) {
        it.copy(payments = it.payments.copy(enrollmentPaid = true, enrollmentDate = todayIso()))
    }

    fun markMonthly(playerId: Long, month: String) = updatePlayer(playerId) { p ->
        val today = todayIso()
        val fees = p.payments.monthlyFees
        val updated = if (fees.any { it.month == month }) {
            fees.map { if (it.month == month) it.copy(paid = true, paidDate = today) else it }
        } else {
            fees + PaymentRecord(month, true, today)
        }
        p.copy(payments = p.payments.copy(monthlyFees = updated))
    }

    private fun updatePlayer(id: Long, change: (Player) -> Player) =
        updateTeam { t -> t.copy(players = t.players.map { if (it.id == id) change(it) else it }) }

    // ─── Partido en vivo ────────────────────────────────────────────────────

    fun createMatch(opponent: String) {
        val t = team ?: return
        val lineup = t.players.filter { it.available }.take(lineupLimit(t.sport)).map { it.id }
        liveMatches = liveMatches + (t.id to LiveMatch(opponent = opponent, sport = t.sport, lineup = lineup))
    }

    fun updateMatch(change: (LiveMatch) -> LiveMatch) {
        val id = teamId ?: return
        val current = liveMatches[id] ?: return
        liveMatches = liveMatches + (id to change(current))
        saveIfEnded()
    }

    fun finishMatch(reason: String) = updateMatch { it.copy(status = MatchStatus.ENDED, endReason = reason) }

    fun resetMatch() {
        val id = teamId ?: return
        liveMatches = liveMatches - id
    }

    /** Al terminar un partido, guarda su resultado en el historial y abre Estadísticas. */
    private fun saveIfEnded() {
        val t = team ?: return
        val m = liveMatches[t.id] ?: return
        if (m.status != MatchStatus.ENDED || m.saved) return
        val football = t.sport == Sport.FUTBOL
        val ourScore = if (football) m.ours else m.sets.count { it.ours > it.theirs }
        val theirScore = if (football) m.theirs else m.sets.count { it.theirs > it.ours }
        updateTeam {
            it.copy(matches = listOf(Match(IdGen.next(), m.opponent, todayIso(), ourScore, theirScore, m.endReason)) + it.matches)
        }
        liveMatches = liveMatches + (t.id to m.copy(saved = true))
        tab = AppTab.ESTADISTICAS
    }
}
