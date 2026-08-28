package com.abrarshakhi.envelope.engine.common.ratelimit

enum class RateLimitKeyType {
    /**
     * Rate limit by client IP address (ideal for unauthenticated routes like sign-in, OTP).
     */
    IP,

    /**
     * Rate limit by authenticated User ID (ideal for user-specific operations).
     */
    USER_ID,

    /**
     * Prefers authenticated User ID if logged in; falls back to client IP address if unauthenticated.
     */
    IP_OR_USER,

    /**
     * Global key across all clients.
     */
    GLOBAL,
}
