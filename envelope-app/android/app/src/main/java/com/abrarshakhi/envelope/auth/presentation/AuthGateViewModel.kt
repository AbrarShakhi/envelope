package com.abrarshakhi.envelope.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AuthGateViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState = _uiState.onStart { checkAuth() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000.milliseconds),
        initialValue = AuthUiState.Loading,
    )

    private fun checkAuth() {
        viewModelScope.launch {
            delay(5.seconds)
            _uiState.update {
                AuthUiState.Unauthenticated
            }
        }
    }

    fun onAction(intent: AuthAction) {
        when (intent) {
            AuthAction.CheckAuth -> checkAuth()
            AuthAction.SignOut -> logout()
        }
    }

    private fun logout() {}
}
