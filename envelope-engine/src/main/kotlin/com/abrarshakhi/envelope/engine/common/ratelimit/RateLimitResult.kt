package com.abrarshakhi.envelope.engine.common.ratelimit

data class RateLimitResult(
    val isAllowed: Boolean,
    val limit: Long,
    val remaining: Long,
    val resetSeconds: Long,
    val retryAfterSeconds: Long = 0,
)
