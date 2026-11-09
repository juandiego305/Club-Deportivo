package com.juandiego.tactica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.juandiego.tactica.ui.TacticaApp
import com.juandiego.tactica.ui.theme.TacticaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TacticaTheme {
                TacticaApp()
            }
        }
    }
}
