package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.entity.Otp
import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import com.abrarshakhi.envelope.engine.auth.repository.OtpRepository
import com.abrarshakhi.envelope.engine.common.exception.BadRequestException
import com.abrarshakhi.envelope.engine.common.exception.InvalidOtpException
import com.abrarshakhi.envelope.engine.common.util.CryptoUtils
import com.abrarshakhi.envelope.engine.config.AppProperties
import com.abrarshakhi.envelope.engine.email.service.EmailService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class OtpService(
    private val otpRepository: OtpRepository,
    private val emailService: EmailService,
    private val appProperties: AppProperties,
) {

    @Transactional
    fun generateAndSendOtp(email: String, purpose: OtpPurpose): Long {
        val normalizedEmail = email.trim().lowercase()

        otpRepository.invalidateActiveOtps(normalizedEmail, purpose)

        val otpCode = CryptoUtils.generateNumericOtp(6)
        val otpHash = CryptoUtils.sha256(otpCode)
        val expirationSeconds = appProperties.otp.expirationSeconds
        val expiresAt = Instant.now().plusSeconds(expirationSeconds)

        val otpEntity = Otp(
            identifier = normalizedEmail,
            otpHash = otpHash,
            purpose = purpose,
            expiresAt = expiresAt,
            maxAttempts = appProperties.otp.maxAttempts,
        )

        otpRepository.save(otpEntity)

        emailService.sendOtpEmail(normalizedEmail, otpCode, purpose.name)

        return expirationSeconds
    }

    @Transactional
    fun resendOtp(email: String, purpose: OtpPurpose): Long {
        val normalizedEmail = email.trim().lowercase()

        val latestOtpOpt =
            otpRepository.findTopByIdentifierIgnoreCaseAndPurposeOrderByCreatedAtDesc(
                normalizedEmail,
                purpose,
            )

        if (latestOtpOpt.isPresent) {
            val latestOtp = latestOtpOpt.get()
            val timeSinceCreation = Duration.between(latestOtp.createdAt, Instant.now()).seconds
            val cooldown = appProperties.otp.resendCooldownSeconds

            if (timeSinceCreation < cooldown) {
                val remainingSeconds = cooldown - timeSinceCreation
                throw BadRequestException("Please wait $remainingSeconds seconds before requesting another OTP.")
            }
        }

        return generateAndSendOtp(normalizedEmail, purpose)
    }

    @Transactional
    fun verifyOtp(email: String, otpCode: String, purpose: OtpPurpose): Boolean {
        val normalizedEmail = email.trim().lowercase()

        val otp =
            otpRepository.findTopByIdentifierIgnoreCaseAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
                normalizedEmail,
                purpose,
            ).orElseThrow {
                InvalidOtpException("No active OTP found. Please request a new OTP.")
            }

        if (Instant.now().isAfter(otp.expiresAt)) {
            otp.isUsed = true
            otpRepository.save(otp)
            throw InvalidOtpException("OTP has expired. Please request a new OTP.")
        }

        if (otp.attemptCount >= otp.maxAttempts) {
            otp.isUsed = true
            otpRepository.save(otp)
            throw InvalidOtpException("Maximum OTP verification attempts exceeded. Please request a new OTP.")
        }

        val inputHash = CryptoUtils.sha256(otpCode.trim())
        val isMatch = CryptoUtils.constantTimeEquals(inputHash, otp.otpHash)

        if (!isMatch) {
            otp.attemptCount += 1
            val remainingAttempts = otp.maxAttempts - otp.attemptCount
            if (otp.attemptCount >= otp.maxAttempts) {
                otp.isUsed = true
            }
            otpRepository.save(otp)

            if (remainingAttempts <= 0) {
                throw InvalidOtpException(
                    "Invalid OTP. Maximum attempts reached. Please request a new code.",
                    0,
                )
            } else {
                throw InvalidOtpException(
                    "Invalid OTP. $remainingAttempts attempt(s) remaining.",
                    remainingAttempts,
                )
            }
        }

        otp.isUsed = true
        otpRepository.save(otp)
        return true
    }
}
