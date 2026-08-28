package com.abrarshakhi.envelope.engine.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class SignInRequest(
    @field:NotBlank(message = "Username or email is required")
    val username: String,

    @field:NotBlank(message = "Auth hash is required")
    val authHash: String,
)
