package com.abrarshakhi.envelope.engine.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PasswordResetVerifyRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "OTP is required")
    @field:Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit numeric code")
    val otp: String,
)
