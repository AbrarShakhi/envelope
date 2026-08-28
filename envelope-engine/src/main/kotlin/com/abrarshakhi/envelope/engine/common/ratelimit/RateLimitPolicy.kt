package com.abrarshakhi.envelope.engine.common.ratelimit

data class RateLimitPolicy(
    val category: RateLimitCategory,
    val limit: Long,
    val windowSeconds: Long,
    val keyType: RateLimitKeyType = RateLimitKeyType.IP_OR_USER,
)
