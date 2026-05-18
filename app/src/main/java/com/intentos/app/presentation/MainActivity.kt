package com.intentos.app.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.intentos.app.BuildConfig
import com.intentos.app.core.theme.IntentOsTheme
import com.intentos.app.presentation.navigation.IntentOsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // PRIVACY HARDENING: Block screenshots and screen recordings of the IntentOS UI
        // In Debug builds, this is disabled to allow hackathon demo recordings.
        if (!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        
        enableEdgeToEdge()
        setContent {
            IntentOsTheme {
                IntentOsNavHost()
            }
        }
    }
}
