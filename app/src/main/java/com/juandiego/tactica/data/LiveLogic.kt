package com.juandiego.tactica.data

import kotlin.math.abs
import kotlin.math.max

/** Jugadas de voleibol que suman punto (a uno u otro equipo). */
val VOLLEY_SCORING = setOf("Punto", "Punto rival", "Ace", "Bloqueo", "Ataque ganador", "Error de saque", "Error de ataque")

/** Jugadas de voleibol que cuentan como punto propio en las estadísticas. */
val VOLLEY_POINT_KINDS = setOf("Punto", "Ace", "Bloqueo", "Ataque ganador")

val SUBSTITUTION_KINDS = setOf("Sustitución", "Reemplazo de líbero")

val FINISH_REASONS = listOf(
    "Final reglamentario",
    "Suspendido por clima",
    "El rival no se presentó",
    "Lesiones o falta de jugadores",
    "Decisión arbitral",
    "Problemas en las instalaciones",
)

fun lineupLimit(sport: Sport) = if (sport == Sport.FUTBOL) 11 else 6

private fun LiveMatch.newEvent(kind: String, side: Side, player: String?) =
    LiveEvent(IdGen.next(), elapsed, side, kind, player)

fun LiveMatch.addEvent(kind: String, side: Side, player: String? = null): LiveMatch =
    copy(events = listOf(newEvent(kind, side, player)) + events)

fun LiveMatch.footballGoal(side: Side, player: String?): LiveMatch = copy(
    ours = ours + if (side == Side.OURS) 1 else 0,
    theirs = theirs + if (side == Side.THEIRS) 1 else 0,
    events = listOf(newEvent("Gol", side, if (side == Side.OURS) player else null)) + events,
)

fun LiveMatch.volleyballPoint(side: Side, kind: String, player: String?): LiveMatch {
    val o = ours + if (side == Side.OURS) 1 else 0
    val t = theirs + if (side == Side.THEIRS) 1 else 0
    val target = if (sets.size == 4) 15 else 25
    val setEnded = (o >= target || t >= target) && abs(o - t) >= 2
    val nextSets = if (setEnded) sets + SetScore(o, t) else sets
    val ourSets = nextSets.count { it.ours > it.theirs }
    val theirSets = nextSets.count { it.theirs > it.ours }
    val finished = ourSets == 3 || theirSets == 3
    return copy(
        ours = if (setEnded) 0 else o,
        theirs = if (setEnded) 0 else t,
        sets = nextSets,
        status = if (finished) MatchStatus.ENDED else status,
        endReason = if (finished) "Final reglamentario" else endReason,
        events = listOf(newEvent(kind, side, if (side == Side.OURS) player else null)) + events,
    )
}

fun LiveMatch.undoLast(players: List<Player>): LiveMatch {
    val last = events.firstOrNull() ?: return this
    var o = ours
    var t = theirs
    var s = sets
    var lu = lineup
    if (sport == Sport.FUTBOL && last.kind == "Gol") {
        if (last.team == Side.OURS) o = max(0, o - 1) else t = max(0, t - 1)
    }
    if (sport == Sport.VOLEIBOL && last.kind in VOLLEY_SCORING) {
        if (o == 0 && t == 0 && s.isNotEmpty()) {
            val previous = s.last()
            s = s.dropLast(1)
            o = previous.ours
            t = previous.theirs
        }
        if (last.team == Side.OURS) o = max(0, o - 1) else t = max(0, t - 1)
    }
    if (last.kind in SUBSTITUTION_KINDS && last.playerOut != null && last.playerIn != null) {
        val outgoing = players.find { it.name == last.playerOut }
        val incoming = players.find { it.name == last.playerIn }
        if (outgoing != null && incoming != null) lu = lu.map { if (it == incoming.id) outgoing.id else it }
    }
    return copy(ours = o, theirs = t, sets = s, lineup = lu, events = events.drop(1))
}

fun LiveMatch.toggleLineup(playerId: Long): LiveMatch = copy(
    lineup = when {
        playerId in lineup -> lineup - playerId
        lineup.size < lineupLimit(sport) -> lineup + playerId
        else -> lineup
    }
)

fun LiveMatch.substitute(outgoing: Player, incoming: Player, libero: Boolean): LiveMatch = copy(
    lineup = lineup.map { if (it == outgoing.id) incoming.id else it },
    events = listOf(
        LiveEvent(
            id = IdGen.next(),
            second = elapsed,
            team = Side.OURS,
            kind = if (libero) "Reemplazo de líbero" else "Sustitución",
            playerOut = outgoing.name,
            playerIn = incoming.name,
            setNumber = if (sport == Sport.VOLEIBOL) sets.size + 1 else null,
        )
    ) + events,
)
