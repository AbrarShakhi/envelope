package com.abrarshakhi.envelope.engine.common.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object CryptoUtils {

    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure 6-digit numeric OTP.
     */
    fun generateNumericOtp(length: Int = 6): String {
        val min = 10.0.pow((length - 1).toDouble()).toInt()
        val max = 10.0.pow(length.toDouble()).toInt() - 1
        val number = secureRandom.nextInt(max - min + 1) + min
        return number.toString()
    }

    /**
     * Generates a cryptographically secure URL-safe random token (e.g. for password resets).
     */
    fun generateSecureRandomToken(byteLength: Int = 32): String {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Hashes an input (e.g. OTP, refresh token, reset token) using SHA-256.
     */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    /**
     * Constant-time comparison between two strings/hashes to prevent timing attacks.
     */
    fun constantTimeEquals(a: String?, b: String?): Boolean {
        if (a == null || b == null) {
            return false
        }
        val aBytes = a.toByteArray(StandardCharsets.UTF_8)
        val bBytes = b.toByteArray(StandardCharsets.UTF_8)
        return MessageDigest.isEqual(aBytes, bBytes)
    }

    /**
     * Generates a deterministic pseudo-random salt for unknown users in pre-login.
     * Prevents user enumeration and timing analysis.
     */
    fun generatePseudoSalt(identifier: String, serverHmacKey: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec =
            SecretKeySpec(serverHmacKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        mac.init(secretKeySpec)
        val hmacBytes = mac.doFinal(identifier.lowercase().toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(hmacBytes)
    }
}
