package com.juandiego.tactica.ui

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.juandiego.tactica.ui.screens.AuthScreen
import com.juandiego.tactica.ui.screens.ClubHubScreen
import com.juandiego.tactica.ui.screens.TeamScreen

@Composable
fun TacticaApp(vm: AppViewModel = viewModel()) {
    val user = vm.user
    val team = vm.team

    // Iconos claros en la barra de estado sobre el login (fondo oscuro), oscuros en el resto.
    val activity = LocalContext.current as? ComponentActivity
    val darkScreen = user == null
    LaunchedEffect(darkScreen) {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (darkScreen) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
        activity?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    when {
        user == null -> AuthScreen(
            loading = vm.authLoading,
            serverError = vm.authError,
            info = vm.authInfo,
            onResetPassword = vm::resetPassword,
            onLogin = vm::login,
            onRegister = vm::register,
            onClearError = vm::clearAuthError,
        )
        !vm.inTeam || team == null -> ClubHubScreen(
            user = user,
            teams = vm.teams,
            loading = vm.loadingTeams,
            error = vm.teamsError,
            onRetry = vm::retryTeams,
            onEnter = vm::enterTeam,
            onCreate = vm::createTeam,
            onLogout = vm::logout,
        )
        else -> TeamScreen(vm, user, team)
    }
}
