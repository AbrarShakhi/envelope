package com.abrarshakhi.envelope.engine.common.exception

class RateLimitExceededException(
    message: String = "Rate limit exceeded. Please try again later.",
    val retryAfterSeconds: Long = 60,
    val limit: Long = 0,
    val windowSeconds: Long = 0,
) : RuntimeException(message)
