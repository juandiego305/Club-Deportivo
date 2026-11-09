package com.juandiego.tactica.data.remote

import com.juandiego.tactica.data.EventType
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
 */

// ─── App -> Firestore ────────────────────────────────────────────────────────

fun Team.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "sport" to sport.name,
    "name" to name,
    "category" to category,
    "logo" to logo,
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
