package com.juandiego.tactica.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.juandiego.tactica.data.Sport

// Paleta tomada del diseño de Figma (colores de Tailwind).
val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val Stone50 = Color(0xFFFAFAF9)

val Lime300 = Color(0xFFBEF264)
val Lime100 = Color(0xFFECFCCB)
val Lime50 = Color(0xFFF7FEE7)
val Lime800 = Color(0xFF3F6212)

val Orange600 = Color(0xFFEA580C)
val Orange300 = Color(0xFFFDBA74)
val Orange200 = Color(0xFFFED7AA)
val Orange100 = Color(0xFFFFEDD5)

val Rose50 = Color(0xFFFFF1F2)
val Rose100 = Color(0xFFFFE4E6)
val Rose200 = Color(0xFFFECDD3)
val Rose400 = Color(0xFFFB7185)
val Rose500 = Color(0xFFF43F5E)
val Rose600 = Color(0xFFE11D48)
val Rose700 = Color(0xFFBE123C)

val Amber50 = Color(0xFFFFFBEB)
val Amber100 = Color(0xFFFEF3C7)
val Amber200 = Color(0xFFFDE68A)
val Amber300 = Color(0xFFFCD34D)
val Amber500 = Color(0xFFF59E0B)
val Amber600 = Color(0xFFD97706)
val Amber700 = Color(0xFFB45309)
val Amber800 = Color(0xFF92400E)
val Amber900 = Color(0xFF78350F)

val Emerald50 = Color(0xFFECFDF5)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857)

val Sky50 = Color(0xFFF0F9FF)
val Sky200 = Color(0xFFBAE6FD)
val Sky800 = Color(0xFF075985)

val White = Color.White
val White5 = Color.White.copy(alpha = 0.05f)
val White10 = Color.White.copy(alpha = 0.10f)
val White15 = Color.White.copy(alpha = 0.15f)
val White20 = Color.White.copy(alpha = 0.20f)
val White30 = Color.White.copy(alpha = 0.30f)

/** Color de acento por deporte: lima para fútbol, naranja para voleibol. */
val Sport.accent: Color get() = if (this == Sport.FUTBOL) Lime300 else Orange300
val Sport.badgeBg: Color get() = if (this == Sport.FUTBOL) Lime100 else Orange100
val Sport.pitch: Color get() = if (this == Sport.FUTBOL) Emerald700 else Orange600

@Composable
fun TacticaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Slate950,
            onPrimary = White,
            secondary = Lime300,
            onSecondary = Slate950,
            background = Stone50,
            onBackground = Slate950,
            surface = White,
            onSurface = Slate950,
            surfaceContainerHigh = White,
            error = Rose600,
        ),
        content = content,
    )
}
