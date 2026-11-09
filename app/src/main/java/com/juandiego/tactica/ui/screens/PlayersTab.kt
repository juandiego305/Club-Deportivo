package com.juandiego.tactica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.PaymentStatus
import com.juandiego.tactica.data.Player
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.data.calcDebt
import com.juandiego.tactica.data.money
import com.juandiego.tactica.data.paymentStatus
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

@Composable
fun PlayersTab(
    team: Team,
    accent: Color,
    onAdd: () -> Unit,
    onPay: (Long) -> Unit,
    onToggle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Long) -> Unit,
) {
    val summary = team.players.groupingBy { paymentStatus(it.payments) }.eachCount()
    val overdue = summary[PaymentStatus.VENCIDO] ?: 0
    val upcoming = summary[PaymentStatus.PROXIMO] ?: 0

    Column {
        if (team.players.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(IntrinsicSize.Min)
                    .card()
            ) {
                listOf(PaymentStatus.AL_DIA, PaymentStatus.PENDIENTE, PaymentStatus.PROXIMO, PaymentStatus.VENCIDO)
                    .forEachIndexed { index, status ->
                        if (index > 0) VerticalDivider(color = Slate200)
                        val style = status.style()
                        val count = summary[status] ?: 0
                        val color = if (count > 0) style.text else Slate300
                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (count > 0) style.bg else Color.Transparent)
                                .padding(horizontal = 4.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Txt("$count", size = 18.sp, weight = FontWeight.Black, color = color)
                            Txt(style.short, size = 12.sp, weight = FontWeight.SemiBold, color = color, align = TextAlign.Center, maxLines = 1)
                        }
                    }
            }
        }

        if (overdue > 0 || upcoming > 0) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp).card(R16, bg = Amber50, border = Amber200).padding(16.dp),
            ) {
                Icon(Ic.bell, null, Modifier.padding(top = 2.dp).size(16.dp), tint = Amber600)
                Spacer(Modifier.width(12.dp))
                val text = buildString {
                    append("Atención: ")
                    if (overdue > 0) append("$overdue jugador${if (overdue > 1) "es tienen" else " tiene"} pagos vencidos. ")
                    if (upcoming > 0) append("$upcoming jugador${if (upcoming > 1) "es tienen" else " tiene"} el pago del mes por registrar. ")
                    append("Toca el icono de la tarjeta para gestionar.")
                }
                Txt(text, size = 12.sp, color = Amber800, lineHeight = 18.sp)
            }
        }

        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt("${team.players.size} integrantes", Modifier.weight(1f), size = 14.sp, color = Slate500)
            Txt(
                "+ Añadir jugador",
                Modifier.clip(R8).clickable(onClick = onAdd).padding(4.dp),
                size = 14.sp, weight = FontWeight.Black,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            team.players.forEach { player ->
                PlayerCard(
                    player = player,
                    accent = accent,
                    onPay = { onPay(player.id) },
                    onToggle = { onToggle(player.id) },
                    onDelete = { onDelete(player.id) },
                    onEdit = { onEdit(player.id) },
                )
            }
            if (team.players.isEmpty()) Empty("Añade jugadores para crear tu plantilla.")
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    accent: Color,
    onPay: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val status = paymentStatus(player.payments)
    val style = status.style()
    val debt = calcDebt(player.payments)
    val borderColor = when (status) {
        PaymentStatus.VENCIDO -> Rose200
        PaymentStatus.PROXIMO -> Amber200
        else -> Slate200
    }

    Column(Modifier.fillMaxWidth().card(R16, border = borderColor)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberCircle(
                player.number,
                bg = if (player.available) accent else Slate200,
                fg = if (player.available) Slate950 else Slate500,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Txt(player.name, size = 14.sp, weight = FontWeight.Black, maxLines = 1)
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Txt(player.position, size = 12.sp, color = Slate500, maxLines = 1)
                    Spacer(Modifier.width(6.dp))
                    PaymentBadge(status)
                }
                if (debt > 0) {
                    Txt(
                        "Debe ${money(debt)}",
                        Modifier.padding(top = 4.dp),
                        size = 12.sp, weight = FontWeight.Black,
                        color = if (status == PaymentStatus.VENCIDO) Rose600 else Amber600,
                    )
                }
            }
            CircleIconButton(Ic.card, onPay, bg = style.bg, tint = style.text, iconSize = 16.dp, contentDescription = "Gestionar pagos")
        }
        HorizontalDivider(color = Slate100)
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt(
                if (player.available) "Disponible" else "Ausente",
                Modifier
                    .clip(CircleShape)
                    .background(if (player.available) Emerald50 else Slate100)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                size = 12.sp, weight = FontWeight.Bold,
                color = if (player.available) Emerald700 else Slate500,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.clip(CircleShape).clickable(onClick = onEdit).padding(6.dp)) {
                Icon(Ic.edit, "Editar jugador", Modifier.size(18.dp), tint = Slate400)
            }
            Spacer(Modifier.width(4.dp))
            Box(Modifier.clip(CircleShape).clickable(onClick = onDelete).padding(6.dp)) {
                Icon(Ic.trash, "Eliminar jugador", Modifier.size(18.dp), tint = Slate300)
            }
        }
    }
}
