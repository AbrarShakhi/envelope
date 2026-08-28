package com.abrarshakhi.envelope.engine.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CryptoUtilsTest {

    @Test
    fun `generateNumericOtp should generate 6 digit string`() {
        val otp = CryptoUtils.generateNumericOtp(6)
        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
    }

    @Test
    fun `generateSecureRandomToken should generate valid string`() {
        val token = CryptoUtils.generateSecureRandomToken(32)
        assertNotNull(token)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `sha256 should be deterministic`() {
        val hash1 = CryptoUtils.sha256("123456")
        val hash2 = CryptoUtils.sha256("123456")
        assertEquals(hash1, hash2)
    }

    @Test
    fun `constantTimeEquals should verify matching hashes`() {
        val hash1 = CryptoUtils.sha256("password123")
        val hash2 = CryptoUtils.sha256("password123")
        val hash3 = CryptoUtils.sha256("different")

        assertTrue(CryptoUtils.constantTimeEquals(hash1, hash2))
        assertFalse(CryptoUtils.constantTimeEquals(hash1, hash3))
        assertFalse(CryptoUtils.constantTimeEquals(null, hash1))
    }

    @Test
    fun `generatePseudoSalt should be deterministic for same identifier and key`() {
        val salt1 = CryptoUtils.generatePseudoSalt("alice@example.com", "secret-hmac-key")
        val salt2 = CryptoUtils.generatePseudoSalt("ALICE@EXAMPLE.COM", "secret-hmac-key")
        val salt3 = CryptoUtils.generatePseudoSalt("bob@example.com", "secret-hmac-key")

        assertEquals(salt1, salt2)
        assertTrue(salt1 != salt3)
    }
}
