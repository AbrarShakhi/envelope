package com.abrarshakhi.envelope.engine.common.ratelimit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RateLimiterRegistryTest {

    private lateinit var registry: RateLimiterRegistry

    @BeforeEach
    fun setUp() {
        registry = RateLimiterRegistry()
    }

    @Test
    fun `getOrCreateBucket should return same bucket instance for identical key`() {
        val policy = RateLimitPolicy(RateLimitCategory.AUTH_STRICT, limit = 10, windowSeconds = 60)
        val bucket1 = registry.getOrCreateBucket("key-1", policy)
        val bucket2 = registry.getOrCreateBucket("key-1", policy)

        assertNotNull(bucket1)
        assertSame(bucket1, bucket2)
        assertEquals(1, registry.size())
    }

    @Test
    fun `getOrCreateBucket should return distinct buckets for different keys`() {
        val policy = RateLimitPolicy(RateLimitCategory.AUTH_STRICT, limit = 10, windowSeconds = 60)
        val bucket1 = registry.getOrCreateBucket("key-1", policy)
        val bucket2 = registry.getOrCreateBucket("key-2", policy)

        assertNotNull(bucket1)
        assertNotNull(bucket2)
        assertEquals(2, registry.size())
    }

    @Test
    fun `clear should remove all active buckets`() {
        val policy = RateLimitPolicy(RateLimitCategory.AUTH_STRICT, limit = 10, windowSeconds = 60)
        registry.getOrCreateBucket("key-1", policy)
        registry.getOrCreateBucket("key-2", policy)

        assertEquals(2, registry.size())
        registry.clear()
        assertEquals(0, registry.size())
    }
}
