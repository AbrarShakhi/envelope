package com.abrarshakhi.envelope.engine.config

import com.abrarshakhi.envelope.engine.common.ratelimit.RateLimitCategory
import com.abrarshakhi.envelope.engine.common.ratelimit.RateLimitKeyType
import com.abrarshakhi.envelope.engine.common.ratelimit.RateLimitPolicy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "envelope")
class AppProperties {
    var jwt: JwtProperties = JwtProperties()
    var otp: OtpProperties = OtpProperties()
    var passwordReset: PasswordResetProperties = PasswordResetProperties()
    var preLogin: PreLoginProperties = PreLoginProperties()
    var rateLimit: RateLimitProperties = RateLimitProperties()
    var accountLockout: AccountLockoutProperties = AccountLockoutProperties()

    class JwtProperties {
        var secretKey: String = ""
        var expiration: Long = 900000 // 15 minutes in ms
        var refreshTokenExpiration: Long = 604800000 // 7 days in ms
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

    class AccountLockoutProperties {
        var maxFailedAttempts: Int = 10
        var lockDurationSeconds: Long = 900 // 15 minutes
    }

    class RateLimitProperties {
        var enabled: Boolean = true
        var authStrict: LimitConfig = LimitConfig(10, 60, RateLimitKeyType.IP)
        var otpStrict: LimitConfig = LimitConfig(3, 60, RateLimitKeyType.IP)
        var tokenRefresh: LimitConfig = LimitConfig(30, 60, RateLimitKeyType.IP_OR_USER)
        var authenticated: LimitConfig = LimitConfig(120, 60, RateLimitKeyType.USER_ID)
        var globalPublic: LimitConfig = LimitConfig(60, 60, RateLimitKeyType.IP)

        class LimitConfig(
            var limit: Long = 60,
            var windowSeconds: Long = 60,
            var keyType: RateLimitKeyType = RateLimitKeyType.IP_OR_USER,
        )

        fun getPolicy(category: RateLimitCategory): RateLimitPolicy {
            val config = when (category) {
                RateLimitCategory.AUTH_STRICT -> authStrict
                RateLimitCategory.OTP_STRICT -> otpStrict
                RateLimitCategory.TOKEN_REFRESH -> tokenRefresh
                RateLimitCategory.AUTHENTICATED -> authenticated
                RateLimitCategory.GLOBAL_PUBLIC -> globalPublic
                RateLimitCategory.CUSTOM -> LimitConfig(60, 60, RateLimitKeyType.IP_OR_USER)
            }
            return RateLimitPolicy(
                category = category,
                limit = config.limit,
                windowSeconds = config.windowSeconds,
                keyType = config.keyType,
            )
        }
    }
}
