package com.abrarshakhi.envelope.engine.auth.dto.request

import jakarta.validation.constraints.NotBlank

data class PreLoginRequest(
    @field:NotBlank(message = "Identifier (username or email) is required")
    val identifier: String,
)
