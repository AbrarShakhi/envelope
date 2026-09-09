package com.abrarshakhi.envelope.auth.domain.model

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val tokens: AuthTokens) : AuthState
    data class RequiresRefresh(val tokens: AuthTokens) : AuthState
}

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiryEpochMillis: Long,
    val refreshTokenExpiryEpochMillis: Long,
)
