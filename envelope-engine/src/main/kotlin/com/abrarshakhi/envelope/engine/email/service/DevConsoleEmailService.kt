package com.abrarshakhi.envelope.engine.email.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * TODO: Later need to replace with actual email service
 */

@Service
class DevConsoleEmailService : EmailService {

    private val logger = LoggerFactory.getLogger(DevConsoleEmailService::class.java)

    override fun sendOtpEmail(toEmail: String, otp: String, purpose: String) {
        logger.info(
            """

            ======================================================================
            [EMAIL SERVICE - DEV CONSOLE]
            Recipient: $toEmail
            Purpose:   $purpose
            OTP Code:  $otp
            ======================================================================
            """.trimIndent(),
        )
    }
}
