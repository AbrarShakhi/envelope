package com.abrarshakhi.envelope.engine.auth.dto.request

import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class OtpResendRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotNull(message = "OTP purpose is required")
    val purpose: OtpPurpose,
)
