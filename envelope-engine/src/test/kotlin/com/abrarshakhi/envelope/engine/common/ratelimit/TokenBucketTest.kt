package com.abrarshakhi.envelope.engine.common.ratelimit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TokenBucketTest {

    @Test
    fun `tryConsume should allow requests up to capacity`() {
        val bucket = TokenBucket(capacity = 3, windowSeconds = 60)
        val now = System.nanoTime()

        val r1 = bucket.tryConsume(1, now)
        assertTrue(r1.isAllowed)
        assertEquals(2, r1.remaining)
        assertEquals(3, r1.limit)

        val r2 = bucket.tryConsume(1, now)
        assertTrue(r2.isAllowed)
        assertEquals(1, r2.remaining)

        val r3 = bucket.tryConsume(1, now)
        assertTrue(r3.isAllowed)
        assertEquals(0, r3.remaining)

        // 4th request in the same instant should be blocked
        val r4 = bucket.tryConsume(1, now)
        assertFalse(r4.isAllowed)
        assertEquals(0, r4.remaining)
        assertTrue(r4.retryAfterSeconds > 0)
    }

    @Test
    fun `tryConsume should refill tokens as time advances`() {
        val bucket = TokenBucket(capacity = 10, windowSeconds = 10) // 1 token per second
        val t0 = 1_000_000_000_000L

        // Consume all 10 tokens
        for (i in 1..10) {
            assertTrue(bucket.tryConsume(1, t0).isAllowed)
        }
        assertFalse(bucket.tryConsume(1, t0).isAllowed)

        // Advance 2 seconds (2 * 10^9 ns) -> 2 tokens refilled
        val t1 = t0 + 2_000_000_000L
        val rAfter2Sec = bucket.tryConsume(1, t1)
        assertTrue(rAfter2Sec.isAllowed)

        val rAfter2SecSecond = bucket.tryConsume(1, t1)
        assertTrue(rAfter2SecSecond.isAllowed)

        // 3rd token should fail because only 2 were refilled
        val rAfter2SecThird = bucket.tryConsume(1, t1)
        assertFalse(rAfter2SecThird.isAllowed)
    }

    @Test
    fun `tryConsume should not exceed maximum capacity on long idle period`() {
        val bucket = TokenBucket(capacity = 5, windowSeconds = 60)
        val t0 = 1_000_000_000_000L

        // Advance 1 hour into future
        val t1 = t0 + 3600_000_000_000L
        val r = bucket.tryConsume(1, t1)
        assertTrue(r.isAllowed)
        assertEquals(4, r.remaining)
    }
}
