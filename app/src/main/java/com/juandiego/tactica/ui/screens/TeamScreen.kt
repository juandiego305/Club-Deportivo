package com.juandiego.tactica.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.AppTab
import com.juandiego.tactica.data.MatchStatus
import com.juandiego.tactica.data.PaymentStatus
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.User
import com.juandiego.tactica.data.firstName
import com.juandiego.tactica.data.paymentStatus
import com.juandiego.tactica.ui.AppViewModel
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

private enum class TeamModal { EVENT, EDIT_EVENT, PLAYER, EDIT_PLAYER, EDIT_TEAM, PAYMENT }

private fun AppTab.icon(): ImageVector = when (this) {
    AppTab.INICIO -> Ic.home
    AppTab.PARTIDO -> Ic.ball
    AppTab.AGENDA -> Ic.calendar
    AppTab.EQUIPO -> Ic.team
    AppTab.ESTADISTICAS -> Ic.chart
}

@Composable
fun TeamScreen(vm: AppViewModel, user: User, team: Team) {
    var modal by remember { mutableStateOf<TeamModal?>(null) }
    var paymentPlayerId by remember { mutableStateOf<Long?>(null) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    val tab = vm.tab
    val accent = team.sport.accent
    val live = vm.liveMatches[team.id]
    val statuses = team.players.map { paymentStatus(it.payments) }
    val hasAlerts = statuses.any { it == PaymentStatus.VENCIDO || it == PaymentStatus.PROXIMO }

    BackHandler { vm.backToClubs() }

    Scaffold(
        containerColor = Stone50,
        bottomBar = {
            BottomNav(
                selected = tab,
                accent = accent,
                liveDot = live?.status == MatchStatus.LIVE,
                paymentDot = hasAlerts && tab != AppTab.EQUIPO,
                onSelect = { vm.tab = it },
            )
        },
    ) { padding ->
        key(tab) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Cabecera ──
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)) {
                    Row(Modifier.padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(
                            onDark = false,
                            size = 36.dp,
                            subtitle = "Volver a mis equipos",
                            modifier = Modifier.weight(1f).clip(R12).clickable { vm.backToClubs() },
                        )
                        if (tab == AppTab.AGENDA || tab == AppTab.EQUIPO) {
                            CircleIconButton(
                                Ic.plus,
                                onClick = { modal = if (tab == AppTab.EQUIPO) TeamModal.PLAYER else TeamModal.EVENT },
                                bg = Slate950, tint = White, size = 40.dp, contentDescription = "Añadir",
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamBadge(team.sport, team.logo)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Txt(team.name, size = 16.sp, weight = FontWeight.Black, maxLines = 1)
                            Txt("${team.sport.label} · ${team.category}", size = 12.sp, color = Slate500, maxLines = 1)
                        }
                        Chip("Editar", Slate200, Slate600, icon = Ic.edit, onClick = { modal = TeamModal.EDIT_TEAM })
                    }
                }

                // ── Contenido de la pestaña ──
                Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp)) {
                    Txt(tab.label.uppercase(), size = 12.sp, weight = FontWeight.Bold, color = Slate400, letterSpacing = 1.5.sp)
                    Txt(
                        if (tab == AppTab.INICIO) "Hola, ${user.name.firstName()}" else tab.label,
                        Modifier.padding(top = 4.dp, bottom = 20.dp),
                        size = 24.sp, weight = FontWeight.Black,
                    )
                    when (tab) {
                        AppTab.INICIO -> DashboardTab(team, live, onGo = { vm.tab = it })
                        AppTab.PARTIDO -> LiveMatchTab(
                            team = team,
                            match = live,
                            onCreate = vm::createMatch,
                            onUpdate = vm::updateMatch,
                            onFinish = vm::finishMatch,
                            onReset = vm::resetMatch,
                        )
                        AppTab.AGENDA -> AgendaTab(
                            team, accent,
                            onAdd = { modal = TeamModal.EVENT },
                            onEdit = { editingId = it; modal = TeamModal.EDIT_EVENT },
                            onDelete = vm::deleteEvent,
                        )
                        AppTab.EQUIPO -> PlayersTab(
                            team = team,
                            accent = accent,
                            onAdd = { modal = TeamModal.PLAYER },
                            onPay = { paymentPlayerId = it; modal = TeamModal.PAYMENT },
                            onEdit = { editingId = it; modal = TeamModal.EDIT_PLAYER },
                            onToggle = vm::toggleAvailable,
                            onDelete = vm::deletePlayer,
                        )
                        AppTab.ESTADISTICAS -> StatsTab(team)
                    }
                }
            }
        }
    }

    when (modal) {
        TeamModal.EVENT -> EventFormSheet(
            accent = accent,
            onSubmit = { type, title, date, time, location ->
                vm.addEvent(type, title, date, time, location)
                modal = null
            },
            onClose = { modal = null },
        )
        TeamModal.EDIT_EVENT -> {
            val event = team.events.find { it.id == editingId }
            if (event != null) {
                EventFormSheet(
                    accent = accent,
                    initial = event,
                    onSubmit = { type, title, date, time, location ->
                        vm.editEvent(event.id, type, title, date, time, location)
                        modal = null
                    },
                    onClose = { modal = null },
                    onDelete = {
                        vm.deleteEvent(event.id)
                        modal = null
                    },
                )
            }
        }
        TeamModal.PLAYER -> PlayerFormSheet(
            sport = team.sport,
            accent = accent,
            takenNumbers = team.players.map { it.number }.toSet(),
            onSubmit = { name, number, position ->
                vm.addPlayer(name, number, position)
                modal = null
            },
            onClose = { modal = null },
        )
        TeamModal.EDIT_PLAYER -> {
            val player = team.players.find { it.id == editingId }
            if (player != null) {
                PlayerFormSheet(
                    sport = team.sport,
                    accent = accent,
                    initial = player,
                    takenNumbers = team.players.filter { it.id != player.id }.map { it.number }.toSet(),
                    onSubmit = { name, number, position ->
                        vm.editPlayer(player.id, name, number, position)
                        modal = null
                    },
                    onClose = { modal = null },
                    onDelete = {
                        vm.deletePlayer(player.id)
                        modal = null
                    },
                )
            }
        }
        TeamModal.EDIT_TEAM -> TeamFormSheet(
            title = "Editar equipo",
            initial = team,
            submitLabel = "Guardar cambios",
            onSubmit = { input ->
                vm.editTeam(input)
                modal = null
            },
            onClose = { modal = null },
            onDelete = {
                modal = null
                vm.deleteTeam()
            },
        )
        TeamModal.PAYMENT -> {
            val player = team.players.find { it.id == paymentPlayerId }
            if (player != null) {
                PaymentSheet(
                    player = player,
                    accent = accent,
                    onEnrollment = { vm.markEnrollment(player.id) },
                    onMonthly = { month -> vm.markMonthly(player.id, month) },
                    onClose = { modal = null; paymentPlayerId = null },
                )
            }
        }
        null -> Unit
    }
}

@Composable
private fun BottomNav(
    selected: AppTab,
    accent: Color,
    liveDot: Boolean,
    paymentDot: Boolean,
    onSelect: (AppTab) -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(Stone50)) {
        HorizontalDivider(color = Slate200)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            AppTab.entries.forEach { item ->
                val isSelected = item == selected
                Column(
                    Modifier
                        .weight(1f)
                        .clip(R12)
                        .clickable { onSelect(item) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) accent else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(item.icon(), item.label, Modifier.size(20.dp), tint = if (isSelected) Slate950 else Slate400)
                        }
                        val dotColor = when {
                            item == AppTab.PARTIDO && liveDot -> Rose500
                            item == AppTab.EQUIPO && paymentDot -> Amber500
                            else -> null
                        }
                        if (dotColor != null) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Stone50)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Txt(
                        item.label,
                        size = 10.sp,
                        weight = FontWeight.Bold,
                        color = if (isSelected) Slate950 else Slate400,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
