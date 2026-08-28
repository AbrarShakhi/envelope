package com.abrarshakhi.envelope.engine.common.ratelimit

enum class RateLimitCategory {
    AUTH_STRICT,
    OTP_STRICT,
    TOKEN_REFRESH,
    AUTHENTICATED,
    GLOBAL_PUBLIC,
    CUSTOM,
}
