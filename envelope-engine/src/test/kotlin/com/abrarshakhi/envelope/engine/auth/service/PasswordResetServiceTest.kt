package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetConfirmRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetInitRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.PasswordResetVerifyRequest
import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import com.abrarshakhi.envelope.engine.auth.entity.PasswordResetToken
import com.abrarshakhi.envelope.engine.auth.entity.Role
import com.abrarshakhi.envelope.engine.auth.entity.User
import com.abrarshakhi.envelope.engine.auth.entity.UserKeyAttributes
import com.abrarshakhi.envelope.engine.auth.repository.PasswordResetTokenRepository
import com.abrarshakhi.envelope.engine.auth.repository.RefreshTokenRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserKeyAttributesRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserRepository
import com.abrarshakhi.envelope.engine.common.util.CryptoUtils
import com.abrarshakhi.envelope.engine.config.AppProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.Optional

class PasswordResetServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userKeyAttributesRepository: UserKeyAttributesRepository
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var otpService: OtpService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var appProperties: AppProperties
    private lateinit var passwordResetService: PasswordResetService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userKeyAttributesRepository = mock(UserKeyAttributesRepository::class.java)
        passwordResetTokenRepository = mock(PasswordResetTokenRepository::class.java)
        refreshTokenRepository = mock(RefreshTokenRepository::class.java)
        otpService = mock(OtpService::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        appProperties = AppProperties().apply {
            passwordReset.expirationSeconds = 900
        }
        passwordResetService = PasswordResetService(
            userRepository,
            userKeyAttributesRepository,
            passwordResetTokenRepository,
            refreshTokenRepository,
            otpService,
            passwordEncoder,
            appProperties,
        )
    }

    @Test
    fun `requestPasswordReset should send OTP when user exists and verified`() {
        val email = "alice@envelope.app"
        val user = User(
            id = 1L,
            username = "alice",
            email = email,
            passwordHash = "hash",
            isEmailVerified = true,
        )
        `when`(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user))
        `when`(otpService.generateAndSendOtp(email, OtpPurpose.PASSWORD_RESET)).thenReturn(600)

        val expiry = passwordResetService.requestPasswordReset(PasswordResetInitRequest(email))

        assertEquals(600, expiry)
        verify(otpService).generateAndSendOtp(email, OtpPurpose.PASSWORD_RESET)
    }

    @Test
    fun `verifyPasswordResetOtp should issue reset token when OTP valid`() {
        val email = "alice@envelope.app"
        val user = User(
            id = 1L,
            username = "alice",
            email = email,
            passwordHash = "hash",
            isEmailVerified = true,
        )
        `when`(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user))
        `when`(otpService.verifyOtp(email, "123456", OtpPurpose.PASSWORD_RESET)).thenReturn(true)

        val response = passwordResetService.verifyPasswordResetOtp(PasswordResetVerifyRequest(email, "123456"))

        assertNotNull(response.resetToken)
        assertEquals(900, response.expiresInSeconds)
        verify(passwordResetTokenRepository).save(any(PasswordResetToken::class.java))
    }

    @Test
    fun `confirmPasswordReset should update password hash, keys, and revoke sessions`() {
        val email = "alice@envelope.app"
        val rawToken = "sample-reset-token"
        val tokenHash = CryptoUtils.sha256(rawToken)

        val user = User(
            id = 1L,
            username = "alice",
            email = email,
            passwordHash = "oldHash",
            isEmailVerified = true,
        )
        val resetToken = PasswordResetToken(
            id = 1L,
            user = user,
            tokenHash = tokenHash,
            expiresAt = Instant.now().plusSeconds(900),
            isUsed = false,
        )
        val keyAttributes = UserKeyAttributes(
            userId = 1L,
            user = user,
            salt = "oldSalt",
            publicKey = "oldPub",
            encryptedPrivateKey = "oldPriv",
            encryptedRecoveryKey = "oldRec",
            keyVersion = 1,
        )

        `when`(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user))
        `when`(passwordResetTokenRepository.findByTokenHashAndIsUsedFalse(tokenHash)).thenReturn(Optional.of(resetToken))
        `when`(passwordEncoder.encode("newAuthHash")).thenReturn("newBcryptHash")
        `when`(userKeyAttributesRepository.findByUserId(1L)).thenReturn(Optional.of(keyAttributes))

        val request = PasswordResetConfirmRequest(
            email = email,
            resetToken = rawToken,
            newAuthHash = "newAuthHash",
            newSalt = "newSalt",
            kdfAlgorithm = "ARGON2ID",
            kdfIterations = 3,
            kdfMemoryKb = 65536,
            kdfParallelism = 4,
            newPublicKey = "newPubKey",
            newEncryptedPrivateKey = "newPrivKey",
            newEncryptedRecoveryKey = "newRecKey",
        )

        passwordResetService.confirmPasswordReset(request)

        assertTrue(resetToken.isUsed)
        assertEquals("newBcryptHash", user.passwordHash)
        assertEquals("newSalt", keyAttributes.salt)
        assertEquals("newPubKey", keyAttributes.publicKey)
        assertEquals(2, keyAttributes.keyVersion)
        verify(refreshTokenRepository).revokeAllUserTokens(user)
    }
}
