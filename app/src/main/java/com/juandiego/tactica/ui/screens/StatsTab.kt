package com.juandiego.tactica.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.data.Team
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

private const val REGULAR_END = "Final reglamentario"

@Composable
fun StatsTab(team: Team) {
    val official = team.matches.filter { it.endReason == null || it.endReason == REGULAR_END }
    val wins = official.count { it.ours > it.theirs }
    val draws = official.count { it.ours == it.theirs }
    val losses = official.size - wins - draws

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryBox("$wins", "Victorias", Emerald50, Emerald700, Modifier.weight(1f))
            SummaryBox("$draws", "Empates", Amber50, Amber700, Modifier.weight(1f))
            SummaryBox("$losses", "Derrotas", Rose50, Rose700, Modifier.weight(1f))
        }

        Column {
            SectionTitle("Informes de partidos")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                team.matches.forEach { match ->
                    val suspended = match.endReason != null && match.endReason != REGULAR_END
                    val result = when {
                        suspended -> "Suspendido"
                        match.ours > match.theirs -> "Victoria"
                        match.ours == match.theirs -> "Empate"
                        else -> "Derrota"
                    }
                    val resultColor = when (result) {
                        "Victoria" -> Emerald600
                        "Derrota" -> Rose600
                        "Suspendido" -> Slate500
                        else -> Amber600
                    }
                    Column(Modifier.fillMaxWidth().card().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Txt("vs. ${match.opponent}", size = 14.sp, weight = FontWeight.Black)
                                Txt(match.date, Modifier.padding(top = 4.dp), size = 12.sp, color = Slate500)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Txt("${match.ours} - ${match.theirs}", size = 18.sp, weight = FontWeight.Black)
                                Txt(result, size = 12.sp, weight = FontWeight.Bold, color = resultColor)
                            }
                        }
                        if (match.endReason != null) {
                            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Slate100)
                            Txt("Finalización: ${match.endReason}", size = 12.sp, weight = FontWeight.SemiBold, color = Slate500)
                        }
                    }
                }
                if (team.matches.isEmpty()) Empty("Cuando termines un partido, su informe aparecerá aquí.")
            }
        }
    }
}

@Composable
private fun SummaryBox(value: String, label: String, bg: Color, fg: Color, modifier: Modifier) {
    Column(modifier.clip(R16).background(bg).padding(16.dp)) {
        Txt(value, size = 24.sp, weight = FontWeight.Black, color = fg)
        Txt(label, size = 12.sp, weight = FontWeight.Bold, color = fg, maxLines = 1)
    }
}
