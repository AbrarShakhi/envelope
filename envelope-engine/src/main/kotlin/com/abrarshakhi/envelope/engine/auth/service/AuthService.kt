package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.dto.request.*
import com.abrarshakhi.envelope.engine.auth.dto.response.*
import com.abrarshakhi.envelope.engine.auth.entity.*
import com.abrarshakhi.envelope.engine.auth.repository.RefreshTokenRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserKeyAttributesRepository
import com.abrarshakhi.envelope.engine.auth.repository.UserRepository
import com.abrarshakhi.envelope.engine.common.exception.ConflictException
import com.abrarshakhi.envelope.engine.common.exception.ForbiddenException
import com.abrarshakhi.envelope.engine.common.exception.ResourceNotFoundException
import com.abrarshakhi.envelope.engine.common.exception.UnauthorizedException
import com.abrarshakhi.envelope.engine.common.util.CryptoUtils
import com.abrarshakhi.envelope.engine.config.AppProperties
import com.abrarshakhi.envelope.engine.security.JwtService
import com.abrarshakhi.envelope.engine.security.UserPrincipal
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userKeyAttributesRepository: UserKeyAttributesRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val otpService: OtpService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val appProperties: AppProperties,
) {

    @Transactional
    fun initiateSignUp(request: SignUpInitRequest): OtpResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val normalizedUsername = request.username.trim().lowercase()

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw ConflictException("An account with this email already exists.")
        }

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw ConflictException("Username is already taken.")
        }

        val expiresInSeconds = otpService.generateAndSendOtp(normalizedEmail, OtpPurpose.SIGNUP)
        return OtpResponse(email = normalizedEmail, expiresInSeconds = expiresInSeconds)
    }

    @Transactional
    fun completeSignUp(request: SignUpCompleteRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val normalizedUsername = request.username.trim()

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw ConflictException("An account with this email already exists.")
        }

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw ConflictException("Username is already taken.")
        }

        otpService.verifyOtp(normalizedEmail, request.otp, OtpPurpose.SIGNUP)

        val serverPasswordHash = passwordEncoder.encode(request.authHash)
            ?: throw BadCredentialsException("Invalid password.")

        val user = User(
            username = normalizedUsername,
            email = normalizedEmail,
            passwordHash = serverPasswordHash,
            name = request.name?.trim(),
            role = Role.USER,
            isEmailVerified = true,
            isAccountNonLocked = true,
        )
        val savedUser = userRepository.save(user)

        val keyAttributes = UserKeyAttributes(
            userId = savedUser.id,
            user = savedUser,
            salt = request.salt,
            kdfAlgorithm = request.kdfAlgorithm,
            kdfIterations = request.kdfIterations,
            kdfMemoryKb = request.kdfMemoryKb,
            kdfParallelism = request.kdfParallelism,
            publicKey = request.publicKey,
            encryptedPrivateKey = request.encryptedPrivateKey,
            encryptedRecoveryKey = request.encryptedRecoveryKey,
            keyVersion = 1,
        )
        userKeyAttributesRepository.save(keyAttributes)

        return generateAuthResponse(savedUser, keyAttributes)
    }

    @Transactional(readOnly = true)
    fun preLogin(request: PreLoginRequest): PreLoginResponse {
        val identifier = request.identifier.trim()
        val userOpt = userRepository.findByUsernameOrEmailIgnoreCase(identifier)

        if (userOpt.isPresent) {
            val user = userOpt.get()
            val keyAttributesOpt = userKeyAttributesRepository.findByUserId(user.id!!)
            if (keyAttributesOpt.isPresent) {
                val keys = keyAttributesOpt.get()
                return PreLoginResponse(
                    salt = keys.salt,
                    kdfAlgorithm = keys.kdfAlgorithm,
                    kdfIterations = keys.kdfIterations,
                    kdfMemoryKb = keys.kdfMemoryKb,
                    kdfParallelism = keys.kdfParallelism,
                )
            }
        }

        val pseudoSalt = CryptoUtils.generatePseudoSalt(identifier, appProperties.jwt.secretKey)
        return PreLoginResponse(
            salt = pseudoSalt,
            kdfAlgorithm = "ARGON2ID",
            kdfIterations = 3,
            kdfMemoryKb = 65536,
            kdfParallelism = 4,
        )
    }

    @Transactional
    fun signIn(request: SignInRequest): AuthResponse {
        val identifier = request.username.trim()
        val user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
            .orElseThrow {
                BadCredentialsException("Invalid username or password")
            }

        if (!user.isEmailVerified) {
            throw ForbiddenException("Account is not verified. Please complete email OTP verification.")
        }

        if (!user.isAccountNonLocked) {
            throw ForbiddenException("Account has been suspended. Please contact support.")
        }

        if (!passwordEncoder.matches(request.authHash, user.passwordHash)) {
            throw BadCredentialsException("Invalid username or password")
        }

        user.lastLoginAt = Instant.now()
        userRepository.save(user)

        val keyAttributes = userKeyAttributesRepository.findByUserId(user.id!!)
            .orElseThrow {
                ResourceNotFoundException("Zero-knowledge key attributes not found for user.")
            }

        return generateAuthResponse(user, keyAttributes)
    }

    @Transactional
    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val incomingHash = CryptoUtils.sha256(request.refreshToken.trim())
        val tokenEntity = refreshTokenRepository.findByTokenHashAndIsRevokedFalse(incomingHash)
            .orElseThrow {
                UnauthorizedException("Invalid or revoked refresh token. Please sign in again.")
            }

        if (Instant.now().isAfter(tokenEntity.expiresAt)) {
            tokenEntity.isRevoked = true
            refreshTokenRepository.save(tokenEntity)
            throw UnauthorizedException("Refresh token has expired. Please sign in again.")
        }

        tokenEntity.isRevoked = true
        refreshTokenRepository.save(tokenEntity)

        val user = tokenEntity.user
        val keyAttributes = userKeyAttributesRepository.findByUserId(user.id!!).orElse(null)

        return generateAuthResponse(user, keyAttributes)
    }

    @Transactional
    fun signOut(refreshToken: String) {
        val tokenHash = CryptoUtils.sha256(refreshToken.trim())
        refreshTokenRepository.revokeByTokenHash(tokenHash)
    }

    @Transactional(readOnly = true)
    fun getCurrentUserProfile(identifier: String): UserProfileResponse {
        val user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
            .orElseThrow { ResourceNotFoundException("User not found: $identifier") }
        return toUserProfileResponse(user)
    }

    @Transactional(readOnly = true)
    fun getCurrentUserKeys(identifier: String): UserKeysResponse {
        val user = userRepository.findByUsernameOrEmailIgnoreCase(identifier)
            .orElseThrow { ResourceNotFoundException("User not found: $identifier") }
        val keyAttributes = userKeyAttributesRepository.findByUserId(user.id!!)
            .orElseThrow { ResourceNotFoundException("Keys not found for user: $identifier") }
        return toUserKeysResponse(keyAttributes)
    }

    private fun generateAuthResponse(user: User, keyAttributes: UserKeyAttributes?): AuthResponse {
        val userPrincipal = UserPrincipal.create(user)
        val accessToken = jwtService.generateAccessToken(userPrincipal)

        val rawRefreshToken = CryptoUtils.generateSecureRandomToken(32)
        val refreshTokenHash = CryptoUtils.sha256(rawRefreshToken)
        val refreshExpiresAt = Instant.now().plusMillis(appProperties.jwt.refreshTokenExpiration)

        val refreshTokenEntity = RefreshToken(
            user = user,
            tokenHash = refreshTokenHash,
            expiresAt = refreshExpiresAt,
        )
        refreshTokenRepository.save(refreshTokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            expiresIn = jwtService.getAccessTokenExpirationSeconds(),
            user = toUserProfileResponse(user),
            keys = keyAttributes?.let { toUserKeysResponse(it) },
        )
    }

    private fun toUserProfileResponse(user: User): UserProfileResponse =
        UserProfileResponse(
            id = user.id ?: 0L,
            username = user.username,
            email = user.email,
            name = user.name,
            role = user.role,
            isEmailVerified = user.isEmailVerified,
            createdAt = user.createdAt,
        )

    private fun toUserKeysResponse(keys: UserKeyAttributes): UserKeysResponse =
        UserKeysResponse(
            publicKey = keys.publicKey,
            encryptedPrivateKey = keys.encryptedPrivateKey,
            encryptedRecoveryKey = keys.encryptedRecoveryKey,
            keyVersion = keys.keyVersion,
        )
}
