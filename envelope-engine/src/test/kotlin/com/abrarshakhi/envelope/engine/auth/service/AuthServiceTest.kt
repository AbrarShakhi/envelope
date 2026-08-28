package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.dto.request.PreLoginRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.SignInRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.SignUpCompleteRequest
import com.abrarshakhi.envelope.engine.auth.dto.request.SignUpInitRequest
import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import com.abrarshakhi.envelope.engine.auth.entity.Role
import com.abrarshakhi.envelope.engine.auth.entity.User
import com.abrarshakhi.envelope.engine.auth.entity.UserKeyAttributes
import com.abrarshakhi.envelope.engine.auth.repository.RefreshTokenRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserKeyAttributesRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserRepository
import com.abrarshakhi.envelope.engine.common.exception.ConflictException
import com.abrarshakhi.envelope.engine.config.AppProperties
import com.abrarshakhi.envelope.engine.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userKeyAttributesRepository: UserKeyAttributesRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var otpService: OtpService
    private lateinit var jwtService: JwtService
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var appProperties: AppProperties
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userKeyAttributesRepository = mock(UserKeyAttributesRepository::class.java)
        refreshTokenRepository = mock(RefreshTokenRepository::class.java)
        otpService = mock(OtpService::class.java)
        jwtService = mock(JwtService::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        appProperties = AppProperties().apply {
            jwt.secretKey = "test-secret-key-at-least-32-bytes-long"
            jwt.expiration = 900000
            jwt.refreshTokenExpiration = 604800000
        }
        authService = AuthService(
            userRepository,
            userKeyAttributesRepository,
            refreshTokenRepository,
            otpService,
            jwtService,
            passwordEncoder,
            appProperties,
        )
    }

    @Test
    fun `initiateSignUp should throw ConflictException if email exists`() {
        val request = SignUpInitRequest(email = "existing@envelope.app", username = "newuser")
        `when`(userRepository.existsByEmailIgnoreCase(request.email.lowercase())).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            authService.initiateSignUp(request)
        }
    }

    @Test
    fun `initiateSignUp should throw ConflictException if username exists`() {
        val request = SignUpInitRequest(email = "new@envelope.app", username = "existinguser")
        `when`(userRepository.existsByEmailIgnoreCase(request.email.lowercase())).thenReturn(false)
        `when`(userRepository.existsByUsernameIgnoreCase(request.username.lowercase())).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            authService.initiateSignUp(request)
        }
    }

    @Test
    fun `completeSignUp should succeed when valid request and OTP provided`() {
        val request = SignUpCompleteRequest(
            email = "alice@envelope.app",
            username = "alice",
            name = "Alice",
            otp = "123456",
            authHash = "clientAuthHash",
            salt = "randomSaltBase64",
            kdfAlgorithm = "ARGON2ID",
            kdfIterations = 3,
            kdfMemoryKb = 65536,
            kdfParallelism = 4,
            publicKey = "pubKeyBase64",
            encryptedPrivateKey = "encPrivKeyBase64",
            encryptedRecoveryKey = "encRecKeyBase64",
        )

        `when`(userRepository.existsByEmailIgnoreCase(request.email.lowercase())).thenReturn(false)
        `when`(userRepository.existsByUsernameIgnoreCase(request.username.lowercase())).thenReturn(false)
        `when`(otpService.verifyOtp(request.email.lowercase(), request.otp, OtpPurpose.SIGNUP)).thenReturn(true)
        `when`(passwordEncoder.encode(request.authHash)).thenReturn("bcryptHashedAuthHash")

        val savedUser = User(
            id = 1L,
            username = request.username,
            email = request.email.lowercase(),
            passwordHash = "bcryptHashedAuthHash",
            name = request.name,
            role = Role.USER,
            isEmailVerified = true,
        )
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val keyAttributes = UserKeyAttributes(
            userId = 1L,
            user = savedUser,
            salt = request.salt,
            publicKey = request.publicKey,
            encryptedPrivateKey = request.encryptedPrivateKey,
            encryptedRecoveryKey = request.encryptedRecoveryKey,
        )
        `when`(userKeyAttributesRepository.save(any(UserKeyAttributes::class.java))).thenReturn(keyAttributes)
        `when`(jwtService.generateAccessToken(any())).thenReturn("mock.jwt.token")
        `when`(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900)

        val response = authService.completeSignUp(request)

        assertNotNull(response)
        assertEquals("mock.jwt.token", response.accessToken)
        assertNotNull(response.refreshToken)
        assertEquals("alice", response.user.username)
        assertEquals("alice@envelope.app", response.user.email)
        assertTrue(response.user.isEmailVerified)
        assertEquals("pubKeyBase64", response.keys?.publicKey)
    }

    @Test
    fun `preLogin should return pseudo-salt for unknown user`() {
        val request = PreLoginRequest(identifier = "unknown_user")
        `when`(userRepository.findByUsernameOrEmailIgnoreCase(request.identifier)).thenReturn(Optional.empty())

        val response = authService.preLogin(request)

        assertNotNull(response.salt)
        assertEquals("ARGON2ID", response.kdfAlgorithm)
        assertEquals(3, response.kdfIterations)
        assertEquals(65536, response.kdfMemoryKb)
        assertEquals(4, response.kdfParallelism)
    }

    @Test
    fun `signIn should succeed with correct credentials and verified account`() {
        val request = SignInRequest(username = "alice", authHash = "clientAuthHash")
        val user = User(
            id = 1L,
            username = "alice",
            email = "alice@envelope.app",
            passwordHash = "bcryptHashedAuthHash",
            role = Role.USER,
            isEmailVerified = true,
        )
        val keyAttributes = UserKeyAttributes(
            userId = 1L,
            user = user,
            salt = "salt123",
            publicKey = "pubKey",
            encryptedPrivateKey = "encPrivKey",
            encryptedRecoveryKey = "encRecKey",
        )

        `when`(userRepository.findByUsernameOrEmailIgnoreCase(request.username)).thenReturn(Optional.of(user))
        `when`(passwordEncoder.matches(request.authHash, user.passwordHash)).thenReturn(true)
        `when`(userRepository.save(any(User::class.java))).thenReturn(user)
        `when`(userKeyAttributesRepository.findByUserId(1L)).thenReturn(Optional.of(keyAttributes))
        `when`(jwtService.generateAccessToken(any())).thenReturn("mock.jwt.token")
        `when`(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900)

        val response = authService.signIn(request)

        assertNotNull(response)
        assertEquals("mock.jwt.token", response.accessToken)
        assertEquals("alice", response.user.username)
        assertNotNull(response.keys)
    }
}
