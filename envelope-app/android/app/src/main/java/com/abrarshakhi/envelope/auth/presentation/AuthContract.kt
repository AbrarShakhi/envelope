package com.abrarshakhi.envelope.auth.presentation

interface AuthAction {
    object CheckAuth : AuthAction
    object SignOut : AuthAction
}

sealed interface AuthUiState {
    object Loading : AuthUiState
    object Authenticated : AuthUiState
    object Unauthenticated : AuthUiState
}
