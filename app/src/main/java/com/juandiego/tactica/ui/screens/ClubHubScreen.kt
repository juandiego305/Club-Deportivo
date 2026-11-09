package com.juandiego.tactica.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.PaymentStatus
import com.juandiego.tactica.data.Sport
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.User
import com.juandiego.tactica.data.firstName
import com.juandiego.tactica.data.paymentStatus
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

@Composable
fun ClubHubScreen(
    user: User,
    teams: List<Team>,
    loading: Boolean,
    onEnter: (Long) -> Unit,
    onCreate: (String, Sport, String, String?) -> Unit,
    onLogout: () -> Unit,
) {
    var showCreate by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Stone50)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 40.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppLogo(onDark = false, modifier = Modifier.weight(1f))
            Chip("Salir", Slate200, Slate600, icon = Ic.logout, onClick = onLogout)
        }
        Spacer(Modifier.height(40.dp))

        Txt("TU ESPACIO DEPORTIVO", size = 12.sp, weight = FontWeight.Bold, color = Slate400, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        Txt("Hola, ${user.name.firstName()}", size = 30.sp, weight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Txt(
            if (teams.isNotEmpty()) "Elige el equipo que quieres gestionar o crea uno nuevo."
            else "Crea tu primer equipo para empezar a organizar la temporada.",
            size = 14.sp,
            color = Slate500,
            lineHeight = 21.sp,
        )
        Spacer(Modifier.height(32.dp))

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Slate950)
            }
        } else if (teams.isEmpty()) {
            Column(Modifier.fillMaxWidth().shadow(12.dp, R24).clip(R24).background(Slate950).padding(24.dp)) {
                Box(Modifier.size(56.dp).clip(R16).background(Lime300), contentAlignment = Alignment.Center) {
                    Icon(Ic.plus, null, Modifier.size(28.dp), tint = Slate950)
                }
                Spacer(Modifier.height(32.dp))
                Txt("Crea tu primer equipo", size = 24.sp, weight = FontWeight.Black, color = White)
                Spacer(Modifier.height(8.dp))
                Txt(
                    "Configura el deporte, nombre y categoría. Después podrás añadir jugadores, agenda y resultados.",
                    size = 14.sp, color = Slate400, lineHeight = 21.sp,
                )
                AppButton(
                    "Crear mi equipo", { showCreate = true },
                    Modifier.fillMaxWidth().padding(top = 24.dp), bg = Lime300, fg = Slate950,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt("Mis equipos", Modifier.weight(1f), size = 18.sp, weight = FontWeight.Black)
                    Chip("${teams.size} ${if (teams.size == 1) "equipo" else "equipos"}", Slate200, Slate500)
                }
                teams.forEach { team -> TeamCard(team) { onEnter(team.id) } }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(R16)
                        .background(White)
                        .dashedBorder(Slate300, 16.dp)
                        .clickable { showCreate = true }
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Ic.plus, null, Modifier.size(16.dp), tint = Slate600)
                    Spacer(Modifier.width(8.dp))
                    Txt("Crear otro equipo", size = 14.sp, weight = FontWeight.Black, color = Slate600)
                }
            }
        }
    }

    if (showCreate) {
        TeamFormSheet(
            title = "Crear equipo",
            initial = null,
            submitLabel = "Crear y entrar al equipo",
            onSubmit = { name, sport, category, logo ->
                showCreate = false
                onCreate(name, sport, category, logo)
            },
            onClose = { showCreate = false },
        )
    }
}

@Composable
private fun TeamCard(team: Team, onEnter: () -> Unit) {
    val alerts = team.players.count {
        val s = paymentStatus(it.payments)
        s == PaymentStatus.VENCIDO || s == PaymentStatus.PROXIMO
    }
    Column(Modifier.fillMaxWidth().shadow(1.dp, R24).card(R24).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamBadge(team.sport, team.logo)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Txt(team.name, size = 16.sp, weight = FontWeight.Black, maxLines = 1)
                Txt("${team.sport.label} · ${team.category}", Modifier.padding(top = 2.dp), size = 12.sp, color = Slate500, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Txt("${team.players.size} jugadores", size = 12.sp, color = Slate400)
                if (alerts > 0) {
                    Txt(
                        "$alerts pago${if (alerts > 1) "s" else ""} pendiente${if (alerts > 1) "s" else ""}",
                        Modifier.padding(top = 4.dp),
                        size = 12.sp, weight = FontWeight.Black, color = Amber600, align = TextAlign.End,
                    )
                }
            }
        }
        AppButton(
            "Entrar en mi equipo", onEnter,
            Modifier.fillMaxWidth().padding(top = 16.dp),
            bg = team.sport.accent, fg = Slate950, trailingIcon = Ic.chevron, vertical = 12.dp,
        )
    }
}
