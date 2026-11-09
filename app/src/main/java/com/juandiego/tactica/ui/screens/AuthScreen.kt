package com.juandiego.tactica.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juandiego.tactica.ui.components.*
import com.juandiego.tactica.ui.theme.*

@Composable
fun AuthScreen(
    loading: Boolean,
    serverError: String?,
    info: String?,
    onResetPassword: (email: String) -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (name: String, email: String, password: String) -> Unit,
    onClearError: () -> Unit,
) {
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    fun submit() {
        val cleanEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() || password.length < 6) {
            error = "Escribe un email válido y una contraseña de al menos 6 caracteres."
            return
        }
        error = ""
        if (register) onRegister(name.trim().ifEmpty { "Entrenador" }, cleanEmail, password)
        else onLogin(cleanEmail, password)
    }

    fun forgotPassword() {
        val cleanEmail = email.trim().lowercase()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            error = "Escribe tu correo arriba y vuelve a tocar \"¿Olvidaste tu contraseña?\"."
            return
        }
        error = ""
        onResetPassword(cleanEmail)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Slate950)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        AppLogo(onDark = true)
        Spacer(Modifier.height(40.dp))

        Txt(
            "GESTIÓN DEPORTIVA",
            Modifier
                .border(1.dp, White15, CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            size = 12.sp,
            weight = FontWeight.Bold,
            color = Lime300,
        )
        Spacer(Modifier.height(12.dp))
        Txt("Tu equipo, siempre en juego.", size = 36.sp, weight = FontWeight.Black, color = White, lineHeight = 40.sp)
        Spacer(Modifier.height(12.dp))
        Txt(
            "Organiza convocatorias, plantilla, partidos y estadísticas de fútbol o voleibol.",
            size = 14.sp,
            color = Slate400,
            lineHeight = 21.sp,
        )
        Spacer(Modifier.height(32.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .shadow(24.dp, R24)
                .clip(R24)
                .background(White)
                .padding(24.dp)
        ) {
            Segmented(
                options = listOf(false to "Entrar", true to "Crear cuenta"),
                selected = register,
                onSelect = { register = it; error = ""; onClearError() },
            )
            Spacer(Modifier.height(20.dp))
            if (register) Field("Tu nombre", name, { name = it }, placeholder = "Ej. Laura García")
            Field("Correo electrónico", email, { email = it }, placeholder = "tu@email.com", keyboardType = KeyboardType.Email)
            Field("Contraseña", password, { password = it }, placeholder = "Mínimo 6 caracteres", password = true)
            if (!register) {
                Txt(
                    "¿Olvidaste tu contraseña?",
                    Modifier
                        .align(Alignment.End)
                        .padding(bottom = 12.dp)
                        .clip(R8)
                        .clickable(enabled = !loading) { forgotPassword() }
                        .padding(4.dp),
                    size = 12.sp,
                    weight = FontWeight.Bold,
                    color = Slate600,
                )
            }
            val shownError = error.ifEmpty { serverError.orEmpty() }
            if (shownError.isNotEmpty()) Notice(shownError, Rose50, Rose700)
            if (shownError.isEmpty() && !info.isNullOrEmpty()) Notice(info, Emerald50, Emerald700)
            AppButton(
                when {
                    loading -> "Conectando…"
                    register -> "Crear mi espacio"
                    else -> "Entrar a mi equipo"
                },
                onClick = { submit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading,
            )
        }

        Spacer(Modifier.height(40.dp))
        Txt(
            "Diseñado para entrenadores y coordinadores",
            Modifier.fillMaxWidth(),
            size = 12.sp,
            color = Slate600,
            align = TextAlign.Center,
        )
    }
}
