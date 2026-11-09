package com.juandiego.tactica.data.remote

import com.juandiego.tactica.data.DEFAULT_ENROLLMENT
import com.juandiego.tactica.data.DEFAULT_MONTHLY
import com.juandiego.tactica.data.EventType
import com.juandiego.tactica.data.LiveEvent
import com.juandiego.tactica.data.LiveMatch
import com.juandiego.tactica.data.MatchStatus
import com.juandiego.tactica.data.SetScore
import com.juandiego.tactica.data.Side
import com.juandiego.tactica.data.Match
import com.juandiego.tactica.data.PaymentRecord
import com.juandiego.tactica.data.Player
import com.juandiego.tactica.data.PlayerPayments
import com.juandiego.tactica.data.Sport
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.TeamEvent

/*
 * Conversión entre los modelos de la app y los documentos de Firestore.
 *
 * Estructura en la nube:
 *   users/{uid}                 -> { name, email }
 *   users/{uid}/teams/{teamId}  -> un documento por equipo con su plantilla,
 *                                  pagos, agenda e historial de partidos.
 *   users/{uid}/liveMatches/{teamId} -> el partido en vivo de ese equipo (si hay uno).
 */

// ─── App -> Firestore ────────────────────────────────────────────────────────

fun Team.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "sport" to sport.name,
    "name" to name,
    "category" to category,
    "logo" to logo,
    "enrollmentFee" to enrollmentFee,
    "monthlyFee" to monthlyFee,
    "players" to players.map { it.toMap() },
    "events" to events.map { it.toMap() },
    "matches" to matches.map { it.toMap() },
)

private fun Player.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "number" to number,
    "position" to position,
    "available" to available,
    "enrollmentPaid" to payments.enrollmentPaid,
    "enrollmentDate" to payments.enrollmentDate,
    "enrollmentAmount" to payments.enrollmentAmount,
    "monthlyAmount" to payments.monthlyAmount,
    "monthlyFees" to payments.monthlyFees.map {
        mapOf("month" to it.month, "paid" to it.paid, "paidDate" to it.paidDate)
    },
)

private fun TeamEvent.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "type" to type.name, "title" to title,
    "date" to date, "time" to time, "location" to location,
)

private fun Match.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "opponent" to opponent, "date" to date,
    "ours" to ours, "theirs" to theirs, "endReason" to endReason,
)

// ─── Firestore -> App ────────────────────────────────────────────────────────

fun teamFromMap(m: Map<String, Any?>): Team = Team(
    id = m.long("id"),
    sport = enumOr(m.str("sport"), Sport.FUTBOL),
    name = m.str("name").orEmpty(),
    category = m.str("category").orEmpty(),
    logo = m.str("logo"),
    enrollmentFee = m.int("enrollmentFee").takeIf { it > 0 } ?: DEFAULT_ENROLLMENT,
    monthlyFee = m.int("monthlyFee").takeIf { it > 0 } ?: DEFAULT_MONTHLY,
    players = m.maps("players").map(::playerFromMap),
    events = m.maps("events").map(::eventFromMap).sortedBy { it.date },
    matches = m.maps("matches").map(::matchFromMap),
)

private fun playerFromMap(m: Map<String, Any?>) = Player(
    id = m.long("id"),
    name = m.str("name").orEmpty(),
    number = m.int("number"),
    position = m.str("position").orEmpty(),
    available = m.bool("available"),
    payments = PlayerPayments(
        enrollmentPaid = m.bool("enrollmentPaid"),
        enrollmentDate = m.str("enrollmentDate"),
        enrollmentAmount = m.int("enrollmentAmount"),
        monthlyAmount = m.int("monthlyAmount"),
        monthlyFees = m.maps("monthlyFees").map {
            PaymentRecord(it.str("month").orEmpty(), it.bool("paid"), it.str("paidDate"))
        },
    ),
)

private fun eventFromMap(m: Map<String, Any?>) = TeamEvent(
    id = m.long("id"),
    type = enumOr(m.str("type"), EventType.ENTRENAMIENTO),
    title = m.str("title").orEmpty(),
    date = m.str("date").orEmpty(),
    time = m.str("time").orEmpty(),
    location = m.str("location").orEmpty(),
)

private fun matchFromMap(m: Map<String, Any?>) = Match(
    id = m.long("id"),
    opponent = m.str("opponent").orEmpty(),
    date = m.str("date").orEmpty(),
    ours = m.int("ours"),
    theirs = m.int("theirs"),
    endReason = m.str("endReason"),
)

// ─── Partido en vivo ─────────────────────────────────────────────────────────

/** [savedAt] = hora del guardado; sirve para adelantar el cronómetro al recuperarlo. */
fun LiveMatch.toMap(savedAt: Long): Map<String, Any?> = mapOf(
    "opponent" to opponent,
    "sport" to sport.name,
    "status" to status.name,
    "elapsed" to elapsed,
    "ours" to ours,
    "theirs" to theirs,
    "sets" to sets.map { mapOf("ours" to it.ours, "theirs" to it.theirs) },
    "events" to events.map {
        mapOf(
            "id" to it.id, "second" to it.second, "team" to it.team.name, "kind" to it.kind,
            "player" to it.player, "playerOut" to it.playerOut, "playerIn" to it.playerIn,
            "setNumber" to it.setNumber,
        )
    },
    "lineup" to lineup,
    "period" to period,
    "endReason" to endReason,
    "saved" to saved,
    "savedAt" to savedAt,
)

/**
 * Reconstruye el partido. Si estaba corriendo, suma al cronómetro el tiempo que la
 * app estuvo cerrada ([now] - savedAt), así el reloj sigue donde debía ir.
 */
fun liveMatchFromMap(m: Map<String, Any?>, now: Long): LiveMatch {
    val status = enumOr(m.str("status"), MatchStatus.READY)
    val savedAt = m.long("savedAt")
    val catchUp = if (status == MatchStatus.LIVE && savedAt in 1..now) ((now - savedAt) / 1000).toInt() else 0
    return LiveMatch(
        opponent = m.str("opponent").orEmpty(),
        sport = enumOr(m.str("sport"), Sport.FUTBOL),
        status = status,
        elapsed = m.int("elapsed") + catchUp,
        ours = m.int("ours"),
        theirs = m.int("theirs"),
        sets = m.maps("sets").map { SetScore(it.int("ours"), it.int("theirs")) },
        events = m.maps("events").map {
            LiveEvent(
                id = it.long("id"),
                second = it.int("second"),
                team = enumOr(it.str("team"), Side.OURS),
                kind = it.str("kind").orEmpty(),
                player = it.str("player"),
                playerOut = it.str("playerOut"),
                playerIn = it.str("playerIn"),
                setNumber = (it["setNumber"] as? Number)?.toInt(),
            )
        },
        lineup = (m["lineup"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList(),
        period = m.int("period").coerceIn(1, 2),
        endReason = m.str("endReason"),
        saved = m.bool("saved"),
    )
}

// ─── Lectura segura de campos (Firestore devuelve números como Long) ─────────

private fun Map<String, Any?>.str(key: String): String? = this[key] as? String
private fun Map<String, Any?>.long(key: String): Long = (this[key] as? Number)?.toLong() ?: 0L
private fun Map<String, Any?>.int(key: String): Int = (this[key] as? Number)?.toInt() ?: 0
private fun Map<String, Any?>.bool(key: String): Boolean = this[key] as? Boolean ?: false

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.maps(key: String): List<Map<String, Any?>> =
    (this[key] as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()

private inline fun <reified T : Enum<T>> enumOr(name: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default
