package com.abrarshakhi.envelope.engine.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "envelope")
class AppProperties {
    var jwt: JwtProperties = JwtProperties()
    var otp: OtpProperties = OtpProperties()
    var passwordReset: PasswordResetProperties = PasswordResetProperties()

    class JwtProperties {
        var secretKey: String =
            "default-secret-key-that-is-at-least-32-bytes-long-for-testing-123456"
        var expiration: Long = 900000 // 15 minutes in ms
        var refreshTokenExpiration: Long = 604800000 // 7 days in ms
        var issuer: String = "envelope-engine"
        var audience: String = "envelope-mobile"
    }

    class OtpProperties {
        var expirationSeconds: Long = 600 // 10 minutes
        var maxAttempts: Int = 5
        var resendCooldownSeconds: Long = 60 // 60 seconds
    }

    class PasswordResetProperties {
        var expirationSeconds: Long = 900 // 15 minutes
    }
}
