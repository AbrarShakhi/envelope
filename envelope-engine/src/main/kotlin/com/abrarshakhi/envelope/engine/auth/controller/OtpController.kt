package com.abrarshakhi.envelope.engine.auth.controller

import com.abrarshakhi.envelope.engine.auth.dto.request.OtpResendRequest
import com.abrarshakhi.envelope.engine.auth.dto.response.OtpResponse
import com.abrarshakhi.envelope.engine.auth.service.OtpService
import com.abrarshakhi.envelope.engine.common.api.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/otp")
class OtpController(
    private val otpService: OtpService,
) {

    @PostMapping("/resend")
    fun resendOtp(
        @Valid @RequestBody request: OtpResendRequest,
    ): ResponseEntity<ApiResponse<OtpResponse>> {
        val expiresInSeconds = otpService.resendOtp(request.email, request.purpose)
        val response = OtpResponse(email = request.email.trim().lowercase(), expiresInSeconds = expiresInSeconds)
        return ResponseEntity.ok(ApiResponse.success("A new verification code has been sent to your email.", response))
    }
}
