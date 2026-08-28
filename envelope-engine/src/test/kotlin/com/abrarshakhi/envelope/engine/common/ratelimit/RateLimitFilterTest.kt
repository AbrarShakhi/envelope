package com.abrarshakhi.envelope.engine.common.ratelimit

import com.abrarshakhi.envelope.engine.config.AppProperties
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RateLimitFilterTest {

    private lateinit var registry: RateLimiterRegistry
    private lateinit var keyResolver: RateLimitKeyResolver
    private lateinit var appProperties: AppProperties
    private lateinit var objectMapper: ObjectMapper
    private lateinit var filter: RateLimitFilter

    @BeforeEach
    fun setUp() {
        registry = RateLimiterRegistry()
        keyResolver = RateLimitKeyResolver()
        appProperties = AppProperties().apply {
            rateLimit.enabled = true
            rateLimit.authStrict.limit = 2
            rateLimit.authStrict.windowSeconds = 60
            rateLimit.otpStrict.limit = 1
            rateLimit.otpStrict.windowSeconds = 60
        }
        objectMapper = ObjectMapper()
        filter = RateLimitFilter(registry, keyResolver, appProperties, objectMapper)
    }

    @Test
    fun `filter should allow requests within limit and attach headers`() {
        val request = MockHttpServletRequest("POST", "/api/v1/auth/sign-in")
        request.remoteAddr = "192.168.1.10"
        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        filter.doFilter(request, response, filterChain)

        assertEquals(200, response.status)
        assertEquals("2", response.getHeader("X-RateLimit-Limit"))
        assertEquals("1", response.getHeader("X-RateLimit-Remaining"))
        assertNotNull(response.getHeader("X-RateLimit-Reset"))
        verify(filterChain, times(1)).doFilter(request, response)
    }

    @Test
    fun `filter should block requests when limit exceeded and return 429`() {
        val filterChain = mock(FilterChain::class.java)

        // Request 1: allowed (limit = 2)
        val req1 = MockHttpServletRequest("POST", "/api/v1/auth/sign-in").apply { remoteAddr = "10.0.0.1" }
        val res1 = MockHttpServletResponse()
        filter.doFilter(req1, res1, filterChain)
        assertEquals(200, res1.status)

        // Request 2: allowed (remaining = 0)
        val req2 = MockHttpServletRequest("POST", "/api/v1/auth/sign-in").apply { remoteAddr = "10.0.0.1" }
        val res2 = MockHttpServletResponse()
        filter.doFilter(req2, res2, filterChain)
        assertEquals(200, res2.status)

        // Request 3: blocked (429)
        val req3 = MockHttpServletRequest("POST", "/api/v1/auth/sign-in").apply { remoteAddr = "10.0.0.1" }
        val res3 = MockHttpServletResponse()
        filter.doFilter(req3, res3, filterChain)

        assertEquals(429, res3.status)
        assertNotNull(res3.getHeader("Retry-After"))
        assertEquals("0", res3.getHeader("X-RateLimit-Remaining"))
        assertEquals("2", res3.getHeader("X-RateLimit-Limit"))

        // filterChain should only have been called twice
        verify(filterChain, times(2)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())
    }

    @Test
    fun `filter should isolate limits between different IP addresses`() {
        val filterChain = mock(FilterChain::class.java)

        // Consume all tokens for IP 10.0.0.1
        for (i in 1..2) {
            val req = MockHttpServletRequest("POST", "/api/v1/auth/sign-in").apply { remoteAddr = "10.0.0.1" }
            filter.doFilter(req, MockHttpServletResponse(), filterChain)
        }

        // IP 10.0.0.1 is now blocked
        val blockedRes = MockHttpServletResponse()
        filter.doFilter(MockHttpServletRequest("POST", "/api/v1/auth/sign-in").apply { remoteAddr = "10.0.0.1" }, blockedRes, filterChain)
        assertEquals(429, blockedRes.status)

        // Different IP 10.0.0.2 should be allowed
        val allowedRes = MockHttpServletResponse()
        filter.doFilter(MockHttpServletRequest("POST", "/api/v1/auth/sign-in").apply { remoteAddr = "10.0.0.2" }, allowedRes, filterChain)
        assertEquals(200, allowedRes.status)
    }
}
