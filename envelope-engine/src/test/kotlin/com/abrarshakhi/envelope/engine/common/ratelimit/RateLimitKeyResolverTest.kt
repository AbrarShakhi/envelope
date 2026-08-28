package com.abrarshakhi.envelope.engine.common.ratelimit

import com.abrarshakhi.envelope.engine.auth.entity.Role
import com.abrarshakhi.envelope.engine.security.UserPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class RateLimitKeyResolverTest {

    private lateinit var keyResolver: RateLimitKeyResolver

    @BeforeEach
    fun setUp() {
        keyResolver = RateLimitKeyResolver()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `resolveClientIp should extract first IP from X-Forwarded-For header`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178")

        val ip = keyResolver.resolveClientIp(request)
        assertEquals("203.0.113.195", ip)
    }

    @Test
    fun `resolveKey should use User ID when authenticated with IP_OR_USER`() {
        val principal = UserPrincipal(
            id = 42L,
            username = "alice",
            email = "alice@envelope.app",
            passwordHash = "hash",
            role = Role.USER,
            isEmailVerified = true,
            isAccountNonLocked = true,
            authorities = emptyList(),
        )
        val auth = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
        SecurityContextHolder.getContext().authentication = auth

        val request = MockHttpServletRequest("GET", "/api/v1/auth/me")
        val key = keyResolver.resolveKey(request, RateLimitKeyType.IP_OR_USER, RateLimitCategory.AUTHENTICATED)

        assertTrue(key.contains("user:42"))
        assertTrue(key.contains("/api/v1/auth/me"))
    }
}
