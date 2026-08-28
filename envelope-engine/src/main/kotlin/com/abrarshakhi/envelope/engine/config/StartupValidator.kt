package com.abrarshakhi.envelope.engine.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.nio.charset.StandardCharsets
import java.util.Base64

@Configuration
class StartupValidator(
    private val appProperties: AppProperties,
) {
    private val logger = LoggerFactory.getLogger(StartupValidator::class.java)

    @PostConstruct
    fun validateConfiguration() {
        logger.info("Validating application security configuration...")

        // 1. Validate JWT Secret Key
        val jwtSecret = appProperties.jwt.secretKey.trim()
        if (jwtSecret.isBlank()) {
            throw IllegalStateException(
                "CRITICAL SECURITY CONFIGURATION ERROR: 'envelope.jwt.secret-key' (JWT_SECRET_KEY) must not be blank!",
            )
        }

        val secretBytes = try {
            val decoded = Base64.getDecoder().decode(jwtSecret)
            if (decoded.size >= 32) decoded else jwtSecret.toByteArray(StandardCharsets.UTF_8)
        } catch (_: Exception) {
            jwtSecret.toByteArray(StandardCharsets.UTF_8)
        }

        if (secretBytes.size < 32) {
            throw IllegalStateException(
                "CRITICAL SECURITY CONFIGURATION ERROR: 'envelope.jwt.secret-key' must be at least 256 bits (32 bytes). Found: ${secretBytes.size} bytes.",
            )
        }

        // 2. Validate Pre-login Pseudo Salt Secret
        val pseudoSaltSecret = appProperties.preLogin.pseudoSaltSecret.trim()
        if (pseudoSaltSecret.isBlank()) {
            logger.warn(
                "SECURITY WARNING: 'envelope.pre-login.pseudo-salt-secret' (PRELOGIN_PSEUDO_SALT_SECRET) is blank. Pseudo-salts will be generated with a fallback empty key.",
            )
        }

        logger.info("Security configuration validated successfully.")
    }
}
