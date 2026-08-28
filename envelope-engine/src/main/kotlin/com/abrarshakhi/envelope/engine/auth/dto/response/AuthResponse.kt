package com.abrarshakhi.envelope.engine.auth.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // in seconds
    val user: UserProfileResponse,
    val keys: UserKeysResponse? = null,
)
