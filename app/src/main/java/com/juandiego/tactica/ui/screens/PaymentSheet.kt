package com.juandiego.tactica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.Player
import com.juandiego.tactica.data.calcDebt
import com.juandiego.tactica.data.currentMonth
import com.juandiego.tactica.data.formatMonth
import com.juandiego.tactica.data.money
import com.juandiego.tactica.data.monthsRange
import com.juandiego.tactica.data.paymentStatus
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

@Composable
fun PaymentSheet(
    player: Player,
    accent: Color,
    onEnrollment: () -> Unit,
    onMonthly: (String) -> Unit,
    onClose: () -> Unit,
) {
    val payments = player.payments
    val status = paymentStatus(payments)
    val month = currentMonth()
    val totalPending = calcDebt(payments)

    Sheet("Gestión de pagos", onClose) {
        // Jugador
        Row(
            Modifier.fillMaxWidth().padding(bottom = 20.dp).clip(R16).background(Slate50).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NumberCircle(player.number, accent, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Txt(player.name, size = 16.sp, weight = FontWeight.Black, maxLines = 1)
                Txt(player.position, size = 12.sp, color = Slate500)
            }
            PaymentBadge(status, large = true)
        }

        if (totalPending > 0) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 20.dp).clip(R16).background(Amber50).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Ic.bell, null, Modifier.size(20.dp), tint = Amber600)
                Spacer(Modifier.width(12.dp))
                Column {
                    Txt("Saldo pendiente", size = 14.sp, weight = FontWeight.Black, color = Amber800)
                    Txt("${money(totalPending)} sin registrar en los últimos 6 meses", size = 12.sp, color = Amber700)
                }
            }
        }

        // Matrícula
        Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt("Matrícula", size = 14.sp, weight = FontWeight.Black)
            Txt("  (pago único)", size = 12.sp, weight = FontWeight.SemiBold, color = Slate400)
        }
        PaymentRow(
            title = money(payments.enrollmentAmount),
            subtitle = if (payments.enrollmentPaid) "Pagado el ${payments.enrollmentDate}" else "Sin pagar — se cobra una sola vez al inscribirse",
            paid = payments.enrollmentPaid,
            paidLabel = "✓ Pagada",
            actionLabel = "Registrar pago",
            bg = if (payments.enrollmentPaid) White else Rose50,
            border = if (payments.enrollmentPaid) Slate200 else Rose200,
            actionBg = Slate950,
            onAction = onEnrollment,
        )
        Spacer(Modifier.padding(top = 20.dp))

        // Mensualidades
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Txt("Mensualidades", Modifier.weight(1f), size = 14.sp, weight = FontWeight.Black)
            Txt("${money(payments.monthlyAmount)}/mes", size = 12.sp, weight = FontWeight.SemiBold, color = Slate400)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            monthsRange(6).forEach { m ->
                val fee = payments.monthlyFees.find { it.month == m }
                val paid = fee?.paid == true
                val isCurrent = m == month
                val overdue = m < month && !paid
                PaymentRow(
                    title = formatMonth(m),
                    subtitle = when {
                        paid -> "Pagado el ${fee?.paidDate}"
                        overdue -> "Vencido — pago no registrado"
                        isCurrent -> "Mes en curso"
                        else -> "Pendiente"
                    },
                    paid = paid,
                    paidLabel = "✓ Pagado",
                    actionLabel = "Registrar",
                    bg = when {
                        overdue -> Rose50
                        isCurrent && !paid -> Amber50
                        else -> White
                    },
                    border = when {
                        overdue -> Rose200
                        isCurrent && !paid -> Amber200
                        else -> Slate200
                    },
                    actionBg = if (overdue) Rose500 else Slate950,
                    onAction = { onMonthly(m) },
                    compact = true,
                )
            }
        }
    }
}

@Composable
private fun PaymentRow(
    title: String,
    subtitle: String,
    paid: Boolean,
    paidLabel: String,
    actionLabel: String,
    bg: Color,
    border: Color,
    actionBg: Color,
    onAction: () -> Unit,
    compact: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth().card(R16, bg = bg, border = border).padding(if (compact) 12.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Txt(title, size = 14.sp, weight = FontWeight.Black)
            Txt(subtitle, Modifier.padding(top = 2.dp), size = 12.sp, color = Slate500)
        }
        Spacer(Modifier.width(8.dp))
        if (paid) {
            Chip(paidLabel, Emerald100, Emerald700)
        } else {
            AppButton(actionLabel, onAction, bg = actionBg, fg = White, vertical = 8.dp, size = 12.sp, shape = CircleShape)
        }
    }
}
