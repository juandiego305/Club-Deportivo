package com.juandiego.tactica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.AppTab
import com.juandiego.tactica.data.LiveMatch
import com.juandiego.tactica.data.MatchStatus
import com.juandiego.tactica.data.PaymentStatus
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.formatClock
import com.juandiego.tactica.data.paymentStatus
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

@Composable
fun DashboardTab(team: Team, live: LiveMatch?, onGo: (AppTab) -> Unit) {
    val next = team.events.firstOrNull()
    val available = team.players.count { it.available }
    val wins = team.matches.count { it.ours > it.theirs }
    val statuses = team.players.map { paymentStatus(it.payments) }
    val overdue = statuses.count { it == PaymentStatus.VENCIDO }
    val upcoming = statuses.count { it == PaymentStatus.PROXIMO }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (live != null && live.status != MatchStatus.ENDED) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, R16, spotColor = Rose200)
                    .clip(R16)
                    .background(Rose500)
                    .clickable { onGo(AppTab.PARTIDO) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PulsingDot(White)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Txt("PARTIDO EN CURSO", size = 12.sp, weight = FontWeight.Bold, color = Rose100, letterSpacing = 1.sp)
                    Txt("vs. ${live.opponent}", size = 14.sp, weight = FontWeight.Black, color = White, maxLines = 1)
                }
                Txt(formatClock(live.elapsed), size = 18.sp, weight = FontWeight.Black, color = White, family = FontFamily.Monospace)
                Icon(Ic.chevron, null, Modifier.size(18.dp), tint = White)
            }
        }

        if (overdue + upcoming > 0) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .card(R16, bg = Amber50, border = Amber200)
                    .clickable { onGo(AppTab.EQUIPO) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Amber100), contentAlignment = Alignment.Center) {
                    Icon(Ic.bell, null, Modifier.size(16.dp), tint = Amber700)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Txt("ALERTA DE PAGOS", size = 12.sp, weight = FontWeight.Bold, color = Amber700, letterSpacing = 1.sp)
                    val parts = buildList {
                        if (overdue > 0) add("$overdue vencido${if (overdue > 1) "s" else ""}")
                        if (upcoming > 0) add("$upcoming próximo${if (upcoming > 1) "s" else ""} a pagar")
                    }
                    Txt(parts.joinToString(" · "), Modifier.padding(top = 2.dp), size = 14.sp, weight = FontWeight.Black, color = Amber900)
                }
                Icon(Ic.chevron, null, Modifier.size(18.dp), tint = Amber600)
            }
        }

        // Próxima actividad
        Column(Modifier.fillMaxWidth().shadow(10.dp, R24, spotColor = Slate300).clip(R24).background(Slate950)) {
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)) {
                Column(Modifier.weight(1f)) {
                    Txt("PRÓXIMA ACTIVIDAD", size = 12.sp, weight = FontWeight.Bold, color = Slate400, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Txt(next?.type?.label ?: "Sin actividad", size = 14.sp, weight = FontWeight.SemiBold, color = White)
                }
                if (next != null) Chip(next.time, team.sport.accent, Slate950)
            }
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
                Txt(next?.title ?: "Crea tu primera actividad", size = 24.sp, weight = FontWeight.Black, color = White)
                Spacer(Modifier.height(8.dp))
                Txt(
                    if (next != null) "${next.date} · ${next.location}" else "Tu agenda aparecerá aquí",
                    size = 14.sp, color = Slate400,
                )
            }
            HorizontalDivider(color = White10)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(White5)
                    .clickable { onGo(AppTab.AGENDA) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Txt("Ver agenda completa", Modifier.weight(1f), size = 14.sp, weight = FontWeight.Bold, color = White)
                Icon(Ic.chevron, null, tint = White)
            }
        }

        // Resumen
        Column {
            SectionTitle("Resumen del equipo")
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).card().padding(vertical = 16.dp)) {
                StatCell("${team.players.size}", "Plantilla", Modifier.weight(1f))
                VerticalDivider(color = Slate200)
                StatCell("$available/${team.players.size}", "Disponibles", Modifier.weight(1f))
                VerticalDivider(color = Slate200)
                StatCell("$wins/${team.matches.size}", "Victorias", Modifier.weight(1f))
            }
        }

        // Próximamente
        Column {
            SectionTitle("Próximamente") {
                Txt(
                    "Ver todo",
                    Modifier.clip(R8).clickable { onGo(AppTab.AGENDA) }.padding(4.dp),
                    size = 14.sp, weight = FontWeight.Bold, color = Slate500,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                team.events.take(3).forEach { EventRow(it) }
                if (team.events.isEmpty()) Empty("No tienes actividades programadas.")
            }
        }
    }
}

@Composable
fun AgendaTab(team: Team, accent: androidx.compose.ui.graphics.Color, onAdd: () -> Unit, onDelete: (Long) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppButton(
            "Nueva actividad", onAdd,
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            bg = accent, fg = Slate950, icon = Ic.plus, vertical = 12.dp,
        )
        team.events.forEach { event -> EventRow(event) { onDelete(event.id) } }
        if (team.events.isEmpty()) Empty("Crea un entrenamiento o partido para comenzar.")
    }
}
