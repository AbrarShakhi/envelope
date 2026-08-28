package com.abrarshakhi.envelope.engine.auth.dto.response

data class OtpResponse(
    val email: String,
    val expiresInSeconds: Long,
)
