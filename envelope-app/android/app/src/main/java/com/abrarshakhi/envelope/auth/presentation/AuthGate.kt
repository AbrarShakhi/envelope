package com.abrarshakhi.envelope.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.envelope.common.ui.components.LoadingScreen
import org.koin.compose.koinInject

@Composable
fun AuthGate(
    onAuthenticated: @Composable () -> Unit,
    onUnauthenticated: @Composable () -> Unit,
    authGateViewModel: AuthGateViewModel = koinInject(),
) {
    val uiState by authGateViewModel.uiState.collectAsStateWithLifecycle()
    var showLoading by remember { mutableStateOf(true) }

    when {
        showLoading -> LoadingScreen(
            isLoading = uiState is AuthUiState.Loading,
            onFinished = { showLoading = false },
        )

        uiState is AuthUiState.Authenticated -> onAuthenticated()

        uiState is AuthUiState.Unauthenticated -> onUnauthenticated()
    }
}
