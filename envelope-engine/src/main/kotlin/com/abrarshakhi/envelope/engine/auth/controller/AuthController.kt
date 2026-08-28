package com.abrarshakhi.envelope.engine.auth.controller

import com.abrarshakhi.envelope.engine.auth.dto.request.*
import com.abrarshakhi.envelope.engine.auth.service.AuthService
import com.abrarshakhi.envelope.engine.common.api.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/sign-up/init")
    fun initiateSignUp(
        @Valid @RequestBody request: SignUpInitRequest,
    ) = ResponseEntity.status(HttpStatus.OK)
        .body(
            ApiResponse.success(
                "Verification code sent to email successfully.",
                authService.initiateSignUp(request),
            ),
        )


    @PostMapping("/sign-up/complete")
    fun completeSignUp(
        @Valid @RequestBody request: SignUpCompleteRequest,
    ) = ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Account registered and verified successfully.",
                authService.completeSignUp(request),
            ),
        )

    @PostMapping("/pre-login")
    fun preLogin(
        @Valid @RequestBody request: PreLoginRequest,
    ) = ResponseEntity.ok(
        ApiResponse.success(
            "Key attributes retrieved successfully.",
            authService.preLogin(request),
        ),
    )


    @PostMapping("/sign-in")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
    ) = ResponseEntity.ok(
        ApiResponse.success(
            "Signed in successfully.",
            authService.signIn(request),
        ),
    )


    @PostMapping("/refresh-token")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest,
    ) = ResponseEntity.ok(
        ApiResponse.success(
            "Tokens refreshed successfully.",
            authService.refreshToken(request),
        ),
    )


    @PostMapping("/sign-out")
    fun signOut(
        @Valid @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.signOut(request.refreshToken)
        return ResponseEntity.ok(ApiResponse.success("Signed out successfully."))
    }

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails,
    ) = ResponseEntity.ok(
        ApiResponse.success(
            "User profile retrieved successfully.",
            authService.getCurrentUserProfile(userDetails.username),
        ),
    )


    @GetMapping("/keys")
    fun getCurrentUserKeys(
        @AuthenticationPrincipal userDetails: UserDetails,
    ) = ResponseEntity.ok(
        ApiResponse.success(
            "User zero-knowledge keys retrieved successfully.",
            authService.getCurrentUserKeys(userDetails.username),
        ),
    )
}

