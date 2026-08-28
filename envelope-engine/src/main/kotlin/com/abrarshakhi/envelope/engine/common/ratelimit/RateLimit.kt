package com.abrarshakhi.envelope.engine.common.ratelimit

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RateLimit(
    /**
     * Predefined category for rate limiting. Defaults to CUSTOM if limit & windowSeconds are explicitly specified.
     */
    val category: RateLimitCategory = RateLimitCategory.CUSTOM,

    /**
     * Maximum number of allowed requests in the time window.
     * Only used when category is CUSTOM.
     */
    val limit: Long = 60,

    /**
     * Duration of the rate limit window in seconds.
     * Only used when category is CUSTOM.
     */
    val windowSeconds: Long = 60,

    /**
     * Key strategy for identifying the client. Defaults to IP_OR_USER.
     */
    val keyType: RateLimitKeyType = RateLimitKeyType.IP_OR_USER,
)
