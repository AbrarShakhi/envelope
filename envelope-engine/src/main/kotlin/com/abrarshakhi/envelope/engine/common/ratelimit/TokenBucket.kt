package com.abrarshakhi.envelope.engine.common.ratelimit

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class TokenBucket(
    val capacity: Long,
    val windowSeconds: Long,
) {
    private val lock = ReentrantLock()
    private val windowNanos: Long = windowSeconds * 1_000_000_000L
    private val refillRatePerNano: Double = capacity.toDouble() / windowNanos.toDouble()

    private var availableTokens: Double = capacity.toDouble()
    private var lastRefillNanos: Long = System.nanoTime()

    @Volatile
    var lastAccessTimeMs: Long = System.currentTimeMillis()
        private set

    fun tryConsume(tokens: Long = 1, nowNanos: Long = System.nanoTime()): RateLimitResult {
        lock.withLock {
            lastAccessTimeMs = System.currentTimeMillis()

            // Calculate elapsed time and refill tokens
            val elapsedNanos = max(0L, nowNanos - lastRefillNanos)
            val tokensToAdd = elapsedNanos.toDouble() * refillRatePerNano
            availableTokens = min(capacity.toDouble(), availableTokens + tokensToAdd)
            lastRefillNanos = nowNanos

            if (availableTokens >= tokens.toDouble()) {
                availableTokens -= tokens.toDouble()
                val remaining = availableTokens.toLong()
                val usedTokens = capacity.toDouble() - availableTokens
                val nanosToFull = if (usedTokens > 0) (usedTokens / refillRatePerNano).toLong() else 0L
                val resetSeconds = max(1L, ceil(nanosToFull / 1_000_000_000.0).toLong())

                return RateLimitResult(
                    isAllowed = true,
                    limit = capacity,
                    remaining = remaining,
                    resetSeconds = resetSeconds,
                    retryAfterSeconds = 0,
                )
            } else {
                val missingTokens = tokens.toDouble() - availableTokens
                val nanosToWait = (missingTokens / refillRatePerNano).toLong()
                val retryAfterSeconds = max(1L, ceil(nanosToWait / 1_000_000_000.0).toLong())

                return RateLimitResult(
                    isAllowed = false,
                    limit = capacity,
                    remaining = 0,
                    resetSeconds = retryAfterSeconds,
                    retryAfterSeconds = retryAfterSeconds,
                )
            }
        }
    }
}
