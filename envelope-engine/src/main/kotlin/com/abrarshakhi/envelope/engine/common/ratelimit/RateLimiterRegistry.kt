package com.abrarshakhi.envelope.engine.common.ratelimit

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class RateLimiterRegistry {

    private val logger = LoggerFactory.getLogger(RateLimiterRegistry::class.java)
    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    fun getOrCreateBucket(key: String, policy: RateLimitPolicy): TokenBucket {
        return buckets.computeIfAbsent(key) {
            TokenBucket(capacity = policy.limit, windowSeconds = policy.windowSeconds)
        }
    }

    /**
     * Periodically clean up stale buckets (idle for > 10 minutes) to prevent memory leaks.
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    fun cleanupStaleBuckets() {
        val now = System.currentTimeMillis()
        val ttlMs = TimeUnit.MINUTES.toMillis(10)
        var removedCount = 0

        val iterator = buckets.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value.lastAccessTimeMs > ttlMs) {
                iterator.remove()
                removedCount++
            }
        }

        if (removedCount > 0) {
            logger.debug("Cleaned up $removedCount idle rate-limit buckets from registry.")
        }
    }

    fun size(): Int = buckets.size

    fun clear() {
        buckets.clear()
    }
}
