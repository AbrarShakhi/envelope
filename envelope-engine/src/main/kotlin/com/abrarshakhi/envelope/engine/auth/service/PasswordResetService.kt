package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetConfirmRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetInitRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetVerifyRequest
import com.abrarshakhi.envelope.engine.auth.dto.response.PasswordResetVerifyResponse
import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import com.abrarshakhi.envelope.engine.auth.entity.PasswordResetToken
import com.abrarshakhi.envelope.engine.auth.repository.PasswordResetTokenRepository
import com.abrarshakhi.envelope.engine.auth.repository.RefreshTokenRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserKeyAttributesRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserRepository
import com.abrarshakhi.envelope.engine.common.exception.BadRequestException
import com.abrarshakhi.envelope.engine.common.exception.ResourceNotFoundException
import com.abrarshakhi.envelope.engine.common.util.CryptoUtils
import com.abrarshakhi.envelope.engine.config.AppProperties
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val userKeyAttributesRepository: UserKeyAttributesRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val otpService: OtpService,
    private val passwordEncoder: PasswordEncoder,
    private val appProperties: AppProperties,
) {

    @Transactional
    fun requestPasswordReset(request: PasswordResetInitRequest): Long {
        val email = request.email.trim().lowercase()
        val userOpt = userRepository.findByEmailIgnoreCase(email)

        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user.isEmailVerified) {
                return otpService.generateAndSendOtp(email, OtpPurpose.PASSWORD_RESET)
            }
        }

        // Return expiration time without leaking whether the account exists (anti-enumeration)
        return appProperties.otp.expirationSeconds
    }

    @Transactional
    fun verifyPasswordResetOtp(request: PasswordResetVerifyRequest): PasswordResetVerifyResponse {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow {
                ResourceNotFoundException("Account not found with email: $email")
            }

        otpService.verifyOtp(email, request.otp, OtpPurpose.PASSWORD_RESET)

        passwordResetTokenRepository.invalidateAllUserTokens(user)

        val rawToken = CryptoUtils.generateSecureRandomToken(32)
        val tokenHash = CryptoUtils.sha256(rawToken)
        val expirationSeconds = appProperties.passwordReset.expirationSeconds
        val expiresAt = Instant.now().plusSeconds(expirationSeconds)

        val resetTokenEntity = PasswordResetToken(
            user = user,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
        )
        passwordResetTokenRepository.save(resetTokenEntity)

        return PasswordResetVerifyResponse(
            resetToken = rawToken,
            expiresInSeconds = expirationSeconds,
        )
    }

    @Transactional
    fun confirmPasswordReset(request: PasswordResetConfirmRequest) {
        val email = request.email.trim().lowercase()
        val user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow {
                ResourceNotFoundException("Account not found with email: $email")
            }

        val tokenHash = CryptoUtils.sha256(request.resetToken.trim())
        val resetTokenEntity = passwordResetTokenRepository.findByTokenHashAndIsUsedFalse(tokenHash)
            .orElseThrow {
                BadRequestException("Invalid or expired password reset token.")
            }

        if (resetTokenEntity.user.id != user.id) {
            throw BadRequestException("Reset token does not match user account.")
        }

        if (Instant.now().isAfter(resetTokenEntity.expiresAt)) {
            resetTokenEntity.isUsed = true
            passwordResetTokenRepository.save(resetTokenEntity)
            throw BadRequestException("Password reset token has expired. Please request a new one.")
        }

        resetTokenEntity.isUsed = true
        passwordResetTokenRepository.save(resetTokenEntity)

        user.passwordHash = passwordEncoder.encode(request.newAuthHash)!!
        user.failedLoginAttempts = 0
        user.lockedUntil = null
        user.updatedAt = Instant.now()
        userRepository.save(user)

        val keyAttributes = userKeyAttributesRepository.findByUserId(user.id!!)
            .orElseGet {
                com.abrarshakhi.envelope.engine.auth.entity.UserKeyAttributes(
                    userId = user.id,
                    user = user,
                    salt = request.newSalt,
                    publicKey = request.newPublicKey,
                    encryptedMasterKey = request.newEncryptedMasterKey,
                    encryptedPrivateKey = request.newEncryptedPrivateKey,
                    encryptedRecoveryKey = request.newEncryptedRecoveryKey,
                )
            }

        keyAttributes.salt = request.newSalt
        keyAttributes.kdfAlgorithm = request.kdfAlgorithm
        keyAttributes.kdfIterations = request.kdfIterations
        keyAttributes.kdfMemoryKb = request.kdfMemoryKb
        keyAttributes.kdfParallelism = request.kdfParallelism
        keyAttributes.publicKey = request.newPublicKey
        if (request.newEncryptedMasterKey != null) {
            keyAttributes.encryptedMasterKey = request.newEncryptedMasterKey
        }
        keyAttributes.encryptedPrivateKey = request.newEncryptedPrivateKey
        keyAttributes.encryptedRecoveryKey = request.newEncryptedRecoveryKey
        keyAttributes.keyVersion += 1
        keyAttributes.updatedAt = Instant.now()

        userKeyAttributesRepository.save(keyAttributes)

        refreshTokenRepository.revokeAllUserTokens(user)
    }
}
