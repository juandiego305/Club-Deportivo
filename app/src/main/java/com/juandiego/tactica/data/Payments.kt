package com.juandiego.tactica.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val SPANISH: Locale = Locale.forLanguageTag("es-CO")

fun currentMonth(): String = YearMonth.now().toString()

fun todayIso(): String = LocalDate.now().toString()

/** Últimos [count] meses en formato AAAA-MM, del más antiguo al actual. */
fun monthsRange(count: Int): List<String> {
    val now = YearMonth.now()
    return (count - 1 downTo 0).map { now.minusMonths(it.toLong()).toString() }
}

fun paymentStatus(p: PlayerPayments): PaymentStatus {
    val month = currentMonth()
    val day = LocalDate.now().dayOfMonth
    if (!p.enrollmentPaid) return PaymentStatus.VENCIDO
    if (p.monthlyFees.any { it.month < month && !it.paid }) return PaymentStatus.VENCIDO
    val fee = p.monthlyFees.find { it.month == month }
    if (fee == null || !fee.paid) return if (day >= 20) PaymentStatus.PROXIMO else PaymentStatus.PENDIENTE
    return PaymentStatus.AL_DIA
}

/** Deuda de los últimos 6 meses más la matrícula si no está pagada. */
fun calcDebt(p: PlayerPayments): Int {
    val unpaid = monthsRange(6).count { m -> p.monthlyFees.find { it.month == m }?.paid != true }
    return unpaid * p.monthlyAmount + if (p.enrollmentPaid) 0 else p.enrollmentAmount
}

fun formatMonth(month: String): String =
    YearMonth.parse(month)
        .format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", SPANISH))
        .replaceFirstChar { it.uppercase() }

fun formatClock(totalSeconds: Int): String = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

/** Formato de pesos colombianos: 60000 -> "$60.000". */
fun money(value: Int): String = "\$" + java.text.NumberFormat.getIntegerInstance(SPANISH).format(value)
