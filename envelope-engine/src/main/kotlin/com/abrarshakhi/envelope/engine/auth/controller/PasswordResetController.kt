package com.abrarshakhi.envelope.engine.auth.controller

import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetConfirmRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetInitRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetVerifyRequest
import com.abrarshakhi.envelope.engine.auth.dto.response.OtpResponse
import com.abrarshakhi.envelope.engine.auth.dto.response.PasswordResetVerifyResponse
import com.abrarshakhi.envelope.engine.auth.service.PasswordResetService
import com.abrarshakhi.envelope.engine.common.api.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/password-reset")
class PasswordResetController(
    private val passwordResetService: PasswordResetService,
) {

    @PostMapping("/request")
    fun requestPasswordReset(
        @Valid @RequestBody request: PasswordResetInitRequest,
    ): ResponseEntity<ApiResponse<OtpResponse>> {
        val expiresInSeconds = passwordResetService.requestPasswordReset(request)
        val response = OtpResponse(email = request.email.trim().lowercase(), expiresInSeconds = expiresInSeconds)
        return ResponseEntity.ok(
            ApiResponse.success("Password reset code sent to email.", response)
        )
    }

    @PostMapping("/verify")
    fun verifyPasswordResetOtp(
        @Valid @RequestBody request: PasswordResetVerifyRequest,
    ): ResponseEntity<ApiResponse<PasswordResetVerifyResponse>> {
        val response = passwordResetService.verifyPasswordResetOtp(request)
        return ResponseEntity.ok(
            ApiResponse.success("OTP verified. Use reset token to set new credentials.", response)
        )
    }

    @PostMapping("/confirm")
    fun confirmPasswordReset(
        @Valid @RequestBody request: PasswordResetConfirmRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        passwordResetService.confirmPasswordReset(request)
        return ResponseEntity.ok(
            ApiResponse.success("Password and zero-knowledge keys reset successfully. Please log in.")
        )
    }
}
