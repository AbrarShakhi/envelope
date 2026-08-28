package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.entity.Otp
import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import com.abrarshakhi.envelope.engine.auth.repository.OtpRepository
import com.abrarshakhi.envelope.engine.common.exception.BadRequestException
import com.abrarshakhi.envelope.engine.common.exception.InvalidOtpException
import com.abrarshakhi.envelope.engine.common.util.CryptoUtils
import com.abrarshakhi.envelope.engine.config.AppProperties
import com.abrarshakhi.envelope.engine.email.service.EmailService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.Optional

class OtpServiceTest {

    private lateinit var otpRepository: OtpRepository
    private lateinit var emailService: EmailService
    private lateinit var appProperties: AppProperties
    private lateinit var otpService: OtpService

    @BeforeEach
    fun setUp() {
        otpRepository = mock(OtpRepository::class.java)
        emailService = mock(EmailService::class.java)
        appProperties = AppProperties().apply {
            otp.expirationSeconds = 600
            otp.maxAttempts = 5
            otp.resendCooldownSeconds = 60
        }
        otpService = OtpService(otpRepository, emailService, appProperties)
    }

    @Test
    fun `generateAndSendOtp should create and save OTP and dispatch email`() {
        val email = "test@envelope.app"
        val purpose = OtpPurpose.SIGNUP

        val expiry = otpService.generateAndSendOtp(email, purpose)

        assertEquals(600, expiry)
        verify(otpRepository).invalidateActiveOtps(email.lowercase(), purpose)

        val otpCaptor = ArgumentCaptor.forClass(Otp::class.java)
        verify(otpRepository).save(otpCaptor.capture())

        val savedOtp = otpCaptor.value
        assertEquals(email.lowercase(), savedOtp.identifier)
        assertEquals(purpose, savedOtp.purpose)
        assertNotNull(savedOtp.otpHash)

        verify(emailService).sendOtpEmail(org.mockito.ArgumentMatchers.eq(email.lowercase()), any(), org.mockito.ArgumentMatchers.eq(purpose.name))
    }

    @Test
    fun `resendOtp should throw BadRequestException if cooldown period has not elapsed`() {
        val email = "test@envelope.app"
        val purpose = OtpPurpose.SIGNUP

        val recentOtp = Otp(
            identifier = email,
            otpHash = "somehash",
            purpose = purpose,
            expiresAt = Instant.now().plusSeconds(600),
        )
        `when`(otpRepository.findTopByIdentifierIgnoreCaseAndPurposeOrderByCreatedAtDesc(email.lowercase(), purpose))
            .thenReturn(Optional.of(recentOtp))

        assertThrows(BadRequestException::class.java) {
            otpService.resendOtp(email, purpose)
        }
    }

    @Test
    fun `verifyOtp should throw InvalidOtpException when OTP does not match and decrement remaining attempts`() {
        val email = "test@envelope.app"
        val purpose = OtpPurpose.SIGNUP
        val realCode = "123456"
        val realHash = CryptoUtils.sha256(realCode)

        val activeOtp = Otp(
            identifier = email,
            otpHash = realHash,
            purpose = purpose,
            expiresAt = Instant.now().plusSeconds(600),
            attemptCount = 0,
            maxAttempts = 5,
        )

        `when`(otpRepository.findTopByIdentifierIgnoreCaseAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(email.lowercase(), purpose))
            .thenReturn(Optional.of(activeOtp))

        val exception = assertThrows(InvalidOtpException::class.java) {
            otpService.verifyOtp(email, "654321", purpose)
        }

        assertEquals(4, exception.remainingAttempts)
        assertEquals(1, activeOtp.attemptCount)
        verify(otpRepository).save(activeOtp)
    }

    @Test
    fun `verifyOtp should succeed and mark OTP as used when valid code provided`() {
        val email = "test@envelope.app"
        val purpose = OtpPurpose.SIGNUP
        val realCode = "123456"
        val realHash = CryptoUtils.sha256(realCode)

        val activeOtp = Otp(
            identifier = email,
            otpHash = realHash,
            purpose = purpose,
            expiresAt = Instant.now().plusSeconds(600),
            attemptCount = 0,
            maxAttempts = 5,
        )

        `when`(otpRepository.findTopByIdentifierIgnoreCaseAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(email.lowercase(), purpose))
            .thenReturn(Optional.of(activeOtp))

        val result = otpService.verifyOtp(email, realCode, purpose)

        assertTrue(result)
        assertTrue(activeOtp.isUsed)
        verify(otpRepository).save(activeOtp)
    }
}
