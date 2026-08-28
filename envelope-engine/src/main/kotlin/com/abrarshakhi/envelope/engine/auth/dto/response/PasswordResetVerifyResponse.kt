package com.abrarshakhi.envelope.engine.auth.dto.response

data class PasswordResetVerifyResponse(
    val resetToken: String,
    val expiresInSeconds: Long,
)
