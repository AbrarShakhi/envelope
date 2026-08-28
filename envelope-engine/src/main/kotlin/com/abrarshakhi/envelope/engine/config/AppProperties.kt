package com.abrarshakhi.envelope.engine.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "envelope")
class AppProperties {
    var jwt: JwtProperties = JwtProperties()
    var otp: OtpProperties = OtpProperties()
    var passwordReset: PasswordResetProperties = PasswordResetProperties()
    var preLogin: PreLoginProperties = PreLoginProperties()

    class JwtProperties {
        var secretKey: String = ""
        var expiration: Long = 900000
        var refreshTokenExpiration: Long = 604800000
        var issuer: String = "envelope-engine"
        var audience: String = "envelope-mobile"
    }

    class OtpProperties {
        var expirationSeconds: Long = 600
        var maxAttempts: Int = 5
        var resendCooldownSeconds: Long = 60
    }

    class PreLoginProperties {
        var pseudoSaltSecret: String = ""
    }

    class PasswordResetProperties {
        var expirationSeconds: Long = 900
    }
}


