package com.abrarshakhi.envelope.engine.auth.dto

import jakarta.validation.constraints.NotBlank

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 900,  // seconds
    val userId: Long,
    val email: String,
    val name: String,
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String,
)
