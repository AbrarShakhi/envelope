package com.abrarshakhi.envelope.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.abrarshakhi.envelope.auth.presentation.AuthGate
import com.abrarshakhi.envelope.common.navigation.AppRoot
import com.abrarshakhi.envelope.common.navigation.AppRoute
import com.abrarshakhi.envelope.common.ui.theme.EnvelopeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnvelopeTheme {
                AuthGate(
                    onAuthenticated = { AppRoot(AppRoute.Home) },
                    onUnauthenticated = { AppRoot(AppRoute.Onboarding) },
                )
            }
        }
    }
}
