package com.abrarshakhi.envelope.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.abrarshakhi.envelope.common.navigation.AppRoot
import com.abrarshakhi.envelope.common.ui.theme.EnvelopeTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnvelopeTheme {
                AppRoot()
            }
        }
    }

    private fun splashScreen() {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.mainState.value is MainState.Loading
            }
        }
    }
}
