package com.juandiego.tactica.data

enum class Sport(val label: String) { FUTBOL("Fútbol"), VOLEIBOL("Voleibol") }

/** Pestañas de la barra inferior dentro de un equipo. */
enum class AppTab(val label: String) {
    INICIO("Inicio"),
    PARTIDO("Partido"),
    AGENDA("Agenda"),
    EQUIPO("Equipo"),
    ESTADISTICAS("Estadísticas"),
}

enum class PaymentStatus { AL_DIA, PROXIMO, VENCIDO, PENDIENTE }

data class PaymentRecord(val month: String, val paid: Boolean, val paidDate: String? = null)

data class PlayerPayments(
    val enrollmentPaid: Boolean,
    val enrollmentDate: String? = null,
    val enrollmentAmount: Int,
    val monthlyAmount: Int,
    val monthlyFees: List<PaymentRecord> = emptyList(),
)

data class Player(
    val id: Long,
    val name: String,
    val number: Int,
    val position: String,
    val available: Boolean,
    val payments: PlayerPayments,
)

enum class EventType(val label: String) { ENTRENAMIENTO("Entrenamiento"), PARTIDO("Partido") }

data class TeamEvent(
    val id: Long,
    val type: EventType,
    val title: String,
    val date: String, // AAAA-MM-DD
    val time: String, // HH:MM
    val location: String,
)

data class Match(
    val id: Long,
    val opponent: String,
    val date: String,
    val ours: Int,
    val theirs: Int,
    val endReason: String? = null,
)

data class Team(
    val id: Long,
    val sport: Sport,
    val name: String,
    val category: String,
    val logo: String? = null, // escudo en Base64 (JPEG pequeño) para guardarlo en Firestore
    val players: List<Player> = emptyList(),
    val events: List<TeamEvent> = emptyList(),
    val matches: List<Match> = emptyList(),
)

data class User(val name: String, val email: String)

enum class Side { OURS, THEIRS }

data class LiveEvent(
    val id: Long,
    val second: Int,
    val team: Side,
    val kind: String,
    val player: String? = null,
    val playerOut: String? = null,
    val playerIn: String? = null,
    val setNumber: Int? = null,
)

data class SetScore(val ours: Int, val theirs: Int)

enum class MatchStatus { READY, LIVE, PAUSED, ENDED }

data class LiveMatch(
    val opponent: String,
    val sport: Sport,
    val status: MatchStatus = MatchStatus.READY,
    val elapsed: Int = 0,
    val ours: Int = 0,
    val theirs: Int = 0,
    val sets: List<SetScore> = emptyList(),
    val events: List<LiveEvent> = emptyList(),
    val lineup: List<Long> = emptyList(),
    val period: Int = 1,
    val endReason: String? = null,
    val saved: Boolean = false,
)

/** Genera ids únicos basados en la hora, sin colisiones. */
object IdGen {
    private var last = 0L

    @Synchronized
    fun next(): Long {
        val now = System.currentTimeMillis()
        last = if (now > last) now else last + 1
        return last
    }
}

fun String.firstName(): String = trim().split(" ").firstOrNull().orEmpty()

const val DEFAULT_ENROLLMENT = 100
const val DEFAULT_MONTHLY = 60
