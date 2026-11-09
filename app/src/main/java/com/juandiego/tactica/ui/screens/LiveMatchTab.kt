package com.juandiego.tactica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.FINISH_REASONS
import com.juandiego.tactica.data.LiveMatch
import com.juandiego.tactica.data.MatchStatus
import com.juandiego.tactica.data.Player
import com.juandiego.tactica.data.SUBSTITUTION_KINDS
import com.juandiego.tactica.data.Side
import com.juandiego.tactica.data.Sport
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.VOLLEY_POINT_KINDS
import com.juandiego.tactica.data.addEvent
import com.juandiego.tactica.data.firstName
import com.juandiego.tactica.data.footballGoal
import com.juandiego.tactica.data.formatClock
import com.juandiego.tactica.data.lineupLimit
import com.juandiego.tactica.data.substitute
import com.juandiego.tactica.data.toggleLineup
import com.juandiego.tactica.data.undoLast
import com.juandiego.tactica.data.volleyballPoint
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*
import kotlin.math.min

private enum class LiveSection { LIVE, LINEUP, STATS }

private data class Action(
    val label: String,
    val bg: Color,
    val fg: Color = Slate950,
    val border: Color? = null,
    val onClick: () -> Unit,
)

@Composable
fun LiveMatchTab(
    team: Team,
    match: LiveMatch?,
    onCreate: (String) -> Unit,
    onUpdate: ((LiveMatch) -> LiveMatch) -> Unit,
    onFinish: (String) -> Unit,
    onReset: () -> Unit,
) {
    if (match == null) {
        NewMatchCard(onCreate)
    } else {
        LiveMatchContent(team, match, onUpdate, onFinish, onReset)
    }
}

@Composable
private fun NewMatchCard(onCreate: (String) -> Unit) {
    var opponent by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().clip(R24).background(Slate950).padding(24.dp)) {
        Box(Modifier.size(56.dp).clip(R16).background(Rose500), contentAlignment = Alignment.Center) {
            Icon(Ic.ball, null, Modifier.size(28.dp), tint = White)
        }
        Spacer(Modifier.height(20.dp))
        Txt("Nuevo partido en vivo", size = 24.sp, weight = FontWeight.Black, color = White)
        Txt(
            "Inicia un marcador, controla el cronómetro y registra cada acción del partido.",
            Modifier.padding(top = 8.dp, bottom = 24.dp),
            size = 14.sp, color = Slate400, lineHeight = 21.sp,
        )
        Field("Equipo rival", opponent, { opponent = it }, placeholder = "Nombre del rival", dark = true)
        AppButton(
            "Preparar partido",
            onClick = { onCreate(opponent.trim()) },
            modifier = Modifier.fillMaxWidth(),
            bg = Rose500,
            enabled = opponent.isNotBlank(),
        )
    }
}

@Composable
private fun LiveMatchContent(
    team: Team,
    match: LiveMatch,
    onUpdate: ((LiveMatch) -> LiveMatch) -> Unit,
    onFinish: (String) -> Unit,
    onReset: () -> Unit,
) {
    var player by remember { mutableStateOf(team.players.firstOrNull()?.name.orEmpty()) }
    var section by remember { mutableStateOf(LiveSection.LIVE) }
    var showFinish by remember { mutableStateOf(false) }
    var showSubstitution by remember { mutableStateOf(false) }

    val football = team.sport == Sport.FUTBOL
    val protagonist = player.ifBlank { null }
    val lineupPlayers = team.players.filter { it.id in match.lineup }
    val benchPlayers = team.players.filter { it.id !in match.lineup }
    val ourSets = match.sets.count { it.ours > it.theirs }
    val theirSets = match.sets.count { it.theirs > it.ours }
    val regularSubs = match.events.count {
        it.kind == "Sustitución" && (football || it.setNumber == match.sets.size + 1)
    }
    val subLimit = if (football) 5 else 6

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Scoreboard(
            team = team,
            match = match,
            ourSets = ourSets,
            theirSets = theirSets,
            onToggle = {
                onUpdate { it.copy(status = if (it.status == MatchStatus.LIVE) MatchStatus.PAUSED else MatchStatus.LIVE) }
            },
            onFinish = { showFinish = true },
            onSecondHalf = { onUpdate { it.copy(period = 2, status = MatchStatus.PAUSED) } },
            onUndo = { onUpdate { it.undoLast(team.players) } },
        )

        Segmented(
            options = listOf(LiveSection.LIVE to "En vivo", LiveSection.LINEUP to "Alineación", LiveSection.STATS to "Estadísticas"),
            selected = section,
            onSelect = { section = it },
            bg = Slate200,
            size = 12.sp,
        )

        when (section) {
            LiveSection.LIVE -> {
                if (match.status != MatchStatus.ENDED) {
                    Column(Modifier.fillMaxWidth().card().padding(start = 12.dp, end = 12.dp, top = 12.dp)) {
                        SelectField(
                            label = "Jugador protagonista",
                            options = team.players.map { it.name },
                            selected = if (team.players.isEmpty()) "Sin jugadores registrados" else player,
                            onSelect = { player = it },
                        )
                    }

                    val actions = if (football) {
                        footballActions(regularSubs, protagonist, onUpdate) { showSubstitution = true }
                    } else {
                        volleyActions(regularSubs, protagonist, onUpdate) { showSubstitution = true }
                    }
                    Column {
                        SectionTitle(if (football) "Registrar acción" else "Registrar jugada")
                        Grid(actions, columns = 2) { action ->
                            AppButton(
                                action.label,
                                action.onClick,
                                Modifier.fillMaxSize(),
                                bg = action.bg,
                                fg = action.fg,
                                border = action.border,
                                vertical = 12.dp,
                            )
                        }
                    }
                }
                Timeline(team, match)
            }
            LiveSection.LINEUP -> LineupSection(team, match, lineupPlayers, benchPlayers) { id ->
                onUpdate { it.toggleLineup(id) }
            }
            LiveSection.STATS -> MatchStatsSection(team, match)
        }

        if (match.status == MatchStatus.ENDED) {
            AppButton("Preparar otro partido", onReset, Modifier.fillMaxWidth())
        }
    }

    if (showSubstitution) {
        SubstitutionSheet(
            team = team,
            match = match,
            lineupPlayers = lineupPlayers,
            benchPlayers = benchPlayers,
            regularSubs = regularSubs,
            subLimit = subLimit,
            onConfirm = { outgoing, incoming, libero ->
                onUpdate { it.substitute(outgoing, incoming, libero) }
                player = incoming.name
                showSubstitution = false
            },
            onClose = { showSubstitution = false },
        )
    }

    if (showFinish) {
        FinishSheet(
            onConfirm = { reason ->
                onFinish(reason)
                showFinish = false
            },
            onClose = { showFinish = false },
        )
    }
}

// ─── Acciones ────────────────────────────────────────────────────────────────

private fun footballActions(
    regularSubs: Int,
    player: String?,
    onUpdate: ((LiveMatch) -> LiveMatch) -> Unit,
    openSubstitution: () -> Unit,
): List<Action> {
    fun event(kind: String, side: Side = Side.OURS, withPlayer: Boolean = true): () -> Unit =
        { onUpdate { it.addEvent(kind, side, if (withPlayer) player else null) } }
    return listOf(
        Action("Gol de mi equipo", Lime300) { onUpdate { it.footballGoal(Side.OURS, player) } },
        Action("Gol del rival", Slate200) { onUpdate { it.footballGoal(Side.THEIRS, null) } },
        Action("Tarjeta amarilla", Amber300, onClick = event("Tarjeta amarilla")),
        Action("Tarjeta roja", Rose500, White, onClick = event("Tarjeta roja")),
        Action("Asistencia", Sky200, onClick = event("Asistencia")),
        Action("Falta", White, border = Slate200, onClick = event("Falta")),
        Action("Cambio ($regularSubs/5)", White, border = Slate200, onClick = openSubstitution),
        Action("Penalti", White, border = Slate200, onClick = event("Penalti")),
        Action("Parada", White, border = Slate200, onClick = event("Parada")),
        Action("Córner", White, border = Slate200, onClick = event("Córner", withPlayer = false)),
        Action("Fuera de juego", White, border = Slate200, onClick = event("Fuera de juego")),
        Action("Lesión", Rose50, Rose700, onClick = event("Lesión")),
        Action("Amarilla rival", Amber100, onClick = event("Tarjeta amarilla", Side.THEIRS, false)),
        Action("Roja rival", Rose100, Rose700, onClick = event("Tarjeta roja", Side.THEIRS, false)),
    )
}

private fun volleyActions(
    regularSubs: Int,
    player: String?,
    onUpdate: ((LiveMatch) -> LiveMatch) -> Unit,
    openSubstitution: () -> Unit,
): List<Action> {
    fun point(side: Side, kind: String): () -> Unit = { onUpdate { it.volleyballPoint(side, kind, player) } }
    fun event(kind: String, withPlayer: Boolean = true): () -> Unit =
        { onUpdate { it.addEvent(kind, Side.OURS, if (withPlayer) player else null) } }
    return listOf(
        Action("+ Punto", Orange300, onClick = point(Side.OURS, "Punto")),
        Action("+ Punto rival", Slate200, onClick = point(Side.THEIRS, "Punto rival")),
        Action("Ace", Lime300, onClick = point(Side.OURS, "Ace")),
        Action("Bloqueo", Sky200, onClick = point(Side.OURS, "Bloqueo")),
        Action("Ataque ganador", Orange200, onClick = point(Side.OURS, "Ataque ganador")),
        Action("Error de saque", Rose100, Rose700, onClick = point(Side.THEIRS, "Error de saque")),
        Action("Error de ataque", Rose100, Rose700, onClick = point(Side.THEIRS, "Error de ataque")),
        Action("Defensa", White, border = Slate200, onClick = event("Defensa")),
        Action("Recepción", White, border = Slate200, onClick = event("Recepción")),
        Action("Rotación", White, border = Slate200, onClick = event("Rotación", false)),
        Action("Tiempo muerto", White, border = Slate200, onClick = event("Tiempo muerto", false)),
        Action("Cambio ($regularSubs/6 set)", White, border = Slate200, onClick = openSubstitution),
        Action("Sanción", Amber100, onClick = event("Sanción")),
        Action("Punto corregido", White, border = Slate200) {
            onUpdate { it.copy(ours = maxOf(0, it.ours - 1)) }
        },
    )
}

// ─── Marcador ────────────────────────────────────────────────────────────────

@Composable
private fun Scoreboard(
    team: Team,
    match: LiveMatch,
    ourSets: Int,
    theirSets: Int,
    onToggle: () -> Unit,
    onFinish: () -> Unit,
    onSecondHalf: () -> Unit,
    onUndo: () -> Unit,
) {
    val football = team.sport == Sport.FUTBOL
    val isLive = match.status == MatchStatus.LIVE
    val statusLabel = when (match.status) {
        MatchStatus.LIVE -> "En vivo"
        MatchStatus.ENDED -> "Finalizado"
        MatchStatus.PAUSED -> "En pausa"
        MatchStatus.READY -> "Preparado"
    }

    Column(Modifier.fillMaxWidth().clip(R24).background(Slate950)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (isLive) PulsingDot(Rose500, 8.dp) else Box(Modifier.size(8.dp).clip(CircleShape).background(Slate500))
                Spacer(Modifier.width(8.dp))
                Txt(
                    statusLabel.uppercase(), size = 12.sp, weight = FontWeight.Black,
                    color = if (isLive) Rose400 else Slate400, letterSpacing = 1.sp,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Txt(formatClock(match.elapsed), size = 18.sp, weight = FontWeight.Black, color = White, family = FontFamily.Monospace)
                Txt(
                    if (football) "${match.period}.º tiempo" else "Set ${min(match.sets.size + 1, 5)}",
                    size = 12.sp, weight = FontWeight.Bold, color = Slate500,
                )
            }
        }
        HorizontalDivider(color = White10)

        if (football) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    TeamBadge(team.sport, team.logo, dark = true)
                    Txt(team.name, Modifier.padding(top = 8.dp), size = 12.sp, weight = FontWeight.Bold, color = White, maxLines = 1)
                }
                Txt("${match.ours} - ${match.theirs}", size = 48.sp, weight = FontWeight.Black, color = White)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(White10), contentAlignment = Alignment.Center) {
                        Txt(match.opponent.take(2).uppercase(), size = 14.sp, weight = FontWeight.Black, color = White)
                    }
                    Txt(match.opponent, Modifier.padding(top = 8.dp), size = 12.sp, weight = FontWeight.Bold, color = White, maxLines = 1)
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    (0 until 5).forEach { index ->
                        val set = match.sets.getOrNull(index)
                        val won = set != null && set.ours > set.theirs
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        set == null -> Color.Transparent
                                        won -> Orange300
                                        else -> White20
                                    }
                                )
                                .then(if (set == null) Modifier.border(1.dp, White15, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            Txt(
                                "${index + 1}", size = 12.sp, weight = FontWeight.Black,
                                color = when {
                                    won -> Slate950
                                    set != null -> White
                                    else -> Slate500
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ScoreColumn(team.name, match.ours, Modifier.weight(1f))
                    Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Txt("SETS", size = 12.sp, color = Slate500)
                        Txt("$ourSets - $theirSets", Modifier.padding(top = 4.dp), size = 20.sp, weight = FontWeight.Black, color = White)
                    }
                    ScoreColumn(match.opponent, match.theirs, Modifier.weight(1f))
                }
            }
        }

        HorizontalDivider(color = White10)
        if (match.status != MatchStatus.ENDED) {
            val buttons = buildList {
                add(Action(if (isLive) "Pausar" else if (match.elapsed > 0) "Reanudar" else "Iniciar", White, Slate950, onClick = onToggle))
                add(Action("Terminar partido", Rose500, White, onClick = onFinish))
                if (football && match.period == 1) add(Action("Comenzar 2.º tiempo", White10, White, onClick = onSecondHalf))
                add(Action("Deshacer última acción", White10, White, onClick = onUndo))
            }
            Grid(buttons, columns = 2, modifier = Modifier.padding(12.dp)) { b ->
                val enabled = b.label != "Deshacer última acción" || match.events.isNotEmpty()
                AppButton(
                    b.label, b.onClick, Modifier.fillMaxSize(),
                    bg = if (enabled) b.bg else White5, fg = if (enabled) b.fg else Slate600,
                    enabled = true, vertical = 12.dp,
                )
            }
        } else {
            Txt(
                "Motivo: ${match.endReason.orEmpty()}",
                Modifier.fillMaxWidth().padding(16.dp),
                size = 14.sp, weight = FontWeight.Bold, color = Slate300, align = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScoreColumn(name: String, score: Int, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Txt(name, size = 12.sp, weight = FontWeight.Bold, color = Slate400, maxLines = 1)
        Txt("$score", Modifier.padding(top = 4.dp), size = 48.sp, weight = FontWeight.Black, color = White)
    }
}

// ─── Minuto a minuto ─────────────────────────────────────────────────────────

@Composable
private fun Timeline(team: Team, match: LiveMatch) {
    Column {
        SectionTitle("Minuto a minuto")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            match.events.forEach { event ->
                Row(Modifier.fillMaxWidth().clip(R12).background(White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt(
                        formatClock(event.second), Modifier.width(52.dp),
                        size = 12.sp, weight = FontWeight.Black, color = Slate400, family = FontFamily.Monospace,
                    )
                    Column(Modifier.weight(1f)) {
                        Txt(event.kind + (event.player?.let { " · $it" } ?: ""), size = 14.sp, weight = FontWeight.Bold)
                        if (event.playerOut != null && event.playerIn != null) {
                            Txt(
                                "Sale ${event.playerOut} · Entra ${event.playerIn}",
                                Modifier.padding(top = 4.dp), size = 12.sp, weight = FontWeight.SemiBold, color = Slate500,
                            )
                        }
                    }
                    Txt(
                        if (event.team == Side.OURS) team.name else match.opponent,
                        Modifier.widthIn(max = 110.dp).padding(start = 8.dp),
                        size = 12.sp, color = Slate400, maxLines = 1,
                    )
                }
            }
            if (match.events.isEmpty()) Empty("Las acciones aparecerán aquí en tiempo real.")
        }
    }
}

// ─── Alineación ──────────────────────────────────────────────────────────────

@Composable
private fun LineupSection(
    team: Team,
    match: LiveMatch,
    lineupPlayers: List<Player>,
    benchPlayers: List<Player>,
    onToggle: (Long) -> Unit,
) {
    val football = team.sport == Sport.FUTBOL
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

        // Cancha
        Box(Modifier.fillMaxWidth().clip(R24).background(team.sport.pitch)) {
            Box(Modifier.matchParentSize().padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
                HorizontalDivider(color = White30)
            }
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Txt("FORMACIÓN TITULAR", size = 12.sp, weight = FontWeight.Bold, color = White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                        Txt(
                            "${lineupPlayers.size} de ${lineupLimit(team.sport)}",
                            Modifier.padding(top = 4.dp), size = 20.sp, weight = FontWeight.Black, color = White,
                        )
                    }
                    TeamBadge(team.sport, team.logo, dark = true)
                }
                if (lineupPlayers.isEmpty()) {
                    Txt(
                        "Selecciona titulares abajo",
                        Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        size = 14.sp, weight = FontWeight.Bold, color = White.copy(alpha = 0.7f), align = TextAlign.Center,
                    )
                } else {
                    Grid(lineupPlayers, columns = if (football) 3 else 2, spacing = 12.dp) { p ->
                        Column(
                            Modifier.fillMaxSize().clip(R12).background(White15).clickable { onToggle(p.id) }.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            NumberCircle(p.number, White, size = 36.dp)
                            Txt(p.name.firstName(), Modifier.padding(top = 4.dp), size = 12.sp, weight = FontWeight.Black, color = White, maxLines = 1)
                            Txt(p.position, size = 12.sp, color = White.copy(alpha = 0.6f), maxLines = 1)
                        }
                    }
                }
            }
        }

        Column {
            SectionTitle("Plantilla convocada") {
                Txt("Toca para cambiar", size = 12.sp, weight = FontWeight.Bold, color = Slate500)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                team.players.forEach { p ->
                    val selected = p.id in match.lineup
                    Row(
                        Modifier.fillMaxWidth().card().clickable { onToggle(p.id) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NumberCircle(p.number, if (selected) Slate950 else Slate100, if (selected) White else Slate500, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Txt(p.name, size = 14.sp, weight = FontWeight.Black, maxLines = 1)
                            Txt(p.position, size = 12.sp, color = Slate500)
                        }
                        Chip(
                            if (selected) "Titular" else "Suplente",
                            if (selected) Emerald50 else Slate100,
                            if (selected) Emerald700 else Slate500,
                        )
                    }
                }
                if (team.players.isEmpty()) Empty("Añade jugadores a la plantilla para preparar la alineación.")
            }
        }

        if (benchPlayers.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().clip(R16).background(Slate100).padding(16.dp)) {
                Txt(
                    "Banquillo: " + benchPlayers.joinToString(", ") { it.name },
                    size = 12.sp, weight = FontWeight.SemiBold, color = Slate500, lineHeight = 18.sp,
                )
            }
        }
    }
}

// ─── Estadísticas del partido ────────────────────────────────────────────────

@Composable
private fun MatchStatsSection(team: Team, match: LiveMatch) {
    val football = team.sport == Sport.FUTBOL
    val ours = match.events.filter { it.team == Side.OURS }
    val substitutions = match.events.filter { it.kind in SUBSTITUTION_KINDS }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (football) {
                StatTile("${match.ours}", "Goles", Modifier.weight(1f))
                StatTile("${ours.count { it.kind == "Falta" }}", "Faltas", Modifier.weight(1f))
                StatTile(
                    "${ours.count { it.kind == "Tarjeta amarilla" }}/${ours.count { it.kind == "Tarjeta roja" }}",
                    "TA / TR", Modifier.weight(1f),
                )
            } else {
                StatTile("${ours.count { it.kind in VOLLEY_POINT_KINDS }}", "Puntos", Modifier.weight(1f))
                StatTile("${ours.count { it.kind == "Ace" }}", "Aces", Modifier.weight(1f))
                StatTile("${ours.count { it.kind == "Bloqueo" }}", "Bloqueos", Modifier.weight(1f))
            }
        }

        if (!football && match.sets.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().card().padding(16.dp)) {
                Txt("Resultado por sets", Modifier.padding(bottom = 12.dp), size = 14.sp, weight = FontWeight.Black)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    match.sets.forEachIndexed { index, set ->
                        Column(
                            Modifier.weight(1f).clip(R12).background(Slate100).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Txt("Set ${index + 1}", size = 12.sp, weight = FontWeight.Bold, color = Slate500)
                            Txt("${set.ours}-${set.theirs}", Modifier.padding(top = 4.dp), size = 14.sp, weight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        if (substitutions.isNotEmpty()) {
            Column {
                SectionTitle("Cambios realizados")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    substitutions.forEach { e ->
                        Row(Modifier.fillMaxWidth().card().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Txt(formatClock(e.second), Modifier.width(52.dp), size = 12.sp, weight = FontWeight.Black, color = Slate400, family = FontFamily.Monospace)
                            Column(Modifier.weight(1f)) {
                                Txt(e.kind + (e.setNumber?.let { " · Set $it" } ?: ""), size = 12.sp, weight = FontWeight.Black, color = Slate500)
                                Row(Modifier.padding(top = 4.dp)) {
                                    Txt("Sale ", size = 12.sp, weight = FontWeight.Black, color = Rose600)
                                    Txt(e.playerOut.orEmpty(), size = 12.sp)
                                }
                                Row {
                                    Txt("Entra ", size = 12.sp, weight = FontWeight.Black, color = Emerald600)
                                    Txt(e.playerIn.orEmpty(), size = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Column {
            SectionTitle("Estadísticas de jugadores")
            val rows = team.players.mapNotNull { p ->
                val ev = match.events.filter { it.player == p.name }
                fun c(kind: String) = ev.count { it.kind == kind }
                val goals = c("Gol"); val assists = c("Asistencia"); val yellow = c("Tarjeta amarilla"); val red = c("Tarjeta roja")
                val points = ev.count { it.kind in VOLLEY_POINT_KINDS }
                val aces = c("Ace"); val blocks = c("Bloqueo"); val digs = c("Defensa"); val rec = c("Recepción")
                val line = if (football) "$goals goles · $assists asist. · $yellow TA · $red TR"
                else "$points pts · $aces aces · $blocks bloq. · $digs def. · $rec rec."
                if (goals + assists + yellow + red + points + digs + rec == 0) null else p.name to line
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { (name, line) ->
                    Row(Modifier.fillMaxWidth().card().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Txt(name, Modifier.weight(1f), size = 14.sp, weight = FontWeight.Black)
                        Txt(line, Modifier.padding(start = 8.dp), size = 12.sp, weight = FontWeight.Bold, color = Slate500, align = TextAlign.End)
                    }
                }
                if (rows.isEmpty()) Empty("Las estadísticas aparecerán al registrar acciones del partido.")
            }
        }
    }
}

// ─── Hojas: cambio y finalización ────────────────────────────────────────────

@Composable
private fun SubstitutionSheet(
    team: Team,
    match: LiveMatch,
    lineupPlayers: List<Player>,
    benchPlayers: List<Player>,
    regularSubs: Int,
    subLimit: Int,
    onConfirm: (Player, Player, Boolean) -> Unit,
    onClose: () -> Unit,
) {
    val football = team.sport == Sport.FUTBOL
    var libero by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(1) }
    var outId by remember { mutableStateOf<Long?>(null) }
    var inId by remember { mutableStateOf<Long?>(null) }
    val outgoing = team.players.find { it.id == outId }
    val incoming = team.players.find { it.id == inId }
    val limitReached = !libero && regularSubs >= subLimit
    val liberoValid = !libero ||
        outgoing?.position?.lowercase()?.contains("líbero") == true ||
        incoming?.position?.lowercase()?.contains("líbero") == true

    Sheet("Registrar cambio", onClose) {
        if (!football) {
            Segmented(
                options = listOf(false to "Cambio regular", true to "Reemplazo de líbero"),
                selected = libero,
                onSelect = { libero = it; outId = null; inId = null; step = 1 },
                size = 12.sp,
            )
            Spacer(Modifier.height(20.dp))
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProgressBar(Rose500, Modifier.weight(1f))
            ProgressBar(if (step == 2) Emerald500 else Slate200, Modifier.weight(1f))
        }

        if (step == 1) {
            Txt("PASO 1 DE 2", size = 12.sp, weight = FontWeight.Black, color = Rose600, letterSpacing = 1.sp)
            Txt("¿Quién sale?", Modifier.padding(top = 4.dp), size = 20.sp, weight = FontWeight.Black)
            Txt("Toca un jugador que está en cancha.", Modifier.padding(top = 4.dp, bottom = 16.dp), size = 14.sp, color = Slate500)
            Grid(lineupPlayers, columns = 2) { p ->
                PlayerChoice(p, Rose100, Rose700) { outId = p.id; inId = null; step = 2 }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 20.dp).clip(R16).background(Slate950).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Txt(
                        when {
                            football -> "Cambios usados"
                            !libero -> "Cambios · Set ${match.sets.size + 1}"
                            else -> "Reemplazos de líbero"
                        },
                        size = 12.sp, weight = FontWeight.Bold, color = Slate400,
                    )
                    Txt(
                        if (libero) "Sin límite regular" else "$regularSubs de $subLimit",
                        Modifier.padding(top = 4.dp), size = 20.sp, weight = FontWeight.Black, color = White,
                    )
                }
                Txt(formatClock(match.elapsed), size = 14.sp, weight = FontWeight.Black, color = White, family = FontFamily.Monospace)
            }
        } else {
            Txt(
                "← Cambiar jugador que sale",
                Modifier.padding(bottom = 12.dp).clip(R8).clickable { step = 1; inId = null }.padding(vertical = 4.dp),
                size = 12.sp, weight = FontWeight.Black, color = Slate500,
            )
            Txt("PASO 2 DE 2", size = 12.sp, weight = FontWeight.Black, color = Emerald600, letterSpacing = 1.sp)
            Txt("¿Quién entra?", Modifier.padding(top = 4.dp), size = 20.sp, weight = FontWeight.Black)
            Txt("Elige un jugador del banquillo.", Modifier.padding(top = 4.dp, bottom = 16.dp), size = 14.sp, color = Slate500)

            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp).clip(R16).background(Slate100).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NumberCircle(outgoing?.number ?: 0, Rose500, White)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Txt("Sale", size = 12.sp, weight = FontWeight.Bold, color = Slate500)
                    Txt(outgoing?.name.orEmpty(), size = 14.sp, weight = FontWeight.Black, maxLines = 1)
                }
                Txt("→", Modifier.padding(horizontal = 8.dp), size = 20.sp, color = Slate400)
                if (incoming != null) {
                    NumberCircle(incoming.number, Emerald500, White)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Txt("Entra", size = 12.sp, weight = FontWeight.Bold, color = Slate500)
                        Txt(incoming.name, size = 14.sp, weight = FontWeight.Black, maxLines = 1)
                    }
                } else {
                    Txt("Selecciona abajo", Modifier.weight(1f), size = 12.sp, weight = FontWeight.Bold, color = Slate400)
                }
            }
            Grid(benchPlayers, columns = 2) { p ->
                val selected = inId == p.id
                PlayerChoice(
                    p,
                    if (selected) Emerald500 else Slate100,
                    if (selected) White else Slate950,
                    selected = selected,
                ) { inId = p.id }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (benchPlayers.isEmpty()) {
            Notice("No hay suplentes disponibles. Añade más jugadores o mueve uno al banquillo desde Alineación.", Amber50, Amber700)
        }
        if (limitReached) {
            Notice(
                "Se alcanzó el límite de $subLimit cambios ${if (football) "del partido" else "en este set"}.",
                Rose50, Rose700,
            )
        }
        if (!liberoValid) {
            Notice("En un reemplazo de líbero, uno de los dos jugadores debe tener la posición Líbero.", Amber50, Amber700)
        }
        if (step == 2) {
            AppButton(
                if (incoming != null) "Confirmar: entra ${incoming.name.firstName()}" else "Selecciona quién entra",
                onClick = {
                    if (outgoing != null && incoming != null) onConfirm(outgoing, incoming, libero)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = outgoing != null && incoming != null && !limitReached && liberoValid,
            )
        }
    }
}

@Composable
private fun ProgressBar(color: Color, modifier: Modifier) {
    Box(modifier.height(8.dp).clip(CircleShape).background(color))
}

@Composable
private fun PlayerChoice(
    player: Player,
    circleBg: Color,
    circleFg: Color,
    selected: Boolean = false,
    circle: Dp = 40.dp,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxSize()
            .card(R12, bg = if (selected) Emerald50 else White, border = if (selected) Emerald500 else Slate200)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberCircle(player.number, circleBg, circleFg, circle)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Txt(player.name, size = 12.sp, weight = FontWeight.Black, maxLines = 1)
            Txt(player.position, size = 12.sp, color = Slate500, maxLines = 1)
        }
    }
}

@Composable
private fun FinishSheet(onConfirm: (String) -> Unit, onClose: () -> Unit) {
    var reason by remember { mutableStateOf(FINISH_REASONS.first()) }
    Sheet("Terminar partido", onClose) {
        Notice("El marcador y las estadísticas quedarán registrados en el historial del equipo.", Amber50, Amber800)
        SelectField("Motivo seleccionado", FINISH_REASONS, reason, { reason = it })
        AppButton("Confirmar finalización", { onConfirm(reason) }, Modifier.fillMaxWidth(), bg = Rose500)
    }
}
