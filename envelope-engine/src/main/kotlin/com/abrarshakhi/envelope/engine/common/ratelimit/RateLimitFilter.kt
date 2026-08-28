package com.abrarshakhi.envelope.engine.common.ratelimit

import com.abrarshakhi.envelope.engine.common.api.ApiResponse
import com.abrarshakhi.envelope.engine.config.AppProperties
import tools.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    private val rateLimiterRegistry: RateLimiterRegistry,
    private val rateLimitKeyResolver: RateLimitKeyResolver,
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!appProperties.rateLimit.enabled || isBypassedPath(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val policy = resolvePolicy(request)
        val bucketKey = rateLimitKeyResolver.resolveKey(request, policy.keyType, policy.category)
        val bucket = rateLimiterRegistry.getOrCreateBucket(bucketKey, policy)
        val result = bucket.tryConsume(1)

        if (result.isAllowed) {
            response.setHeader("X-RateLimit-Limit", result.limit.toString())
            response.setHeader("X-RateLimit-Remaining", result.remaining.toString())
            response.setHeader("X-RateLimit-Reset", result.resetSeconds.toString())
            filterChain.doFilter(request, response)
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.setHeader(HttpHeaders.RETRY_AFTER, result.retryAfterSeconds.toString())
            response.setHeader("X-RateLimit-Limit", result.limit.toString())
            response.setHeader("X-RateLimit-Remaining", "0")
            response.setHeader("X-RateLimit-Reset", result.retryAfterSeconds.toString())

            val errorResponse = ApiResponse.error<Map<String, Any>>(
                message = "Rate limit exceeded. Please try again in ${result.retryAfterSeconds} seconds.",
                data = mapOf(
                    "retryAfterSeconds" to result.retryAfterSeconds,
                    "limit" to result.limit,
                    "windowSeconds" to policy.windowSeconds,
                    "category" to policy.category.name,
                ),
            )
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }

    private fun resolvePolicy(request: HttpServletRequest): RateLimitPolicy {
        val uri = request.requestURI

        // 1. Strict OTP Endpoints (Email/SMS dispatch, password reset requests)
        if (pathMatcher.match("/api/v1/auth/otp/**", uri) ||
            pathMatcher.match("/api/v1/auth/password-reset/request", uri) ||
            pathMatcher.match("/api/v1/auth/password-reset/verify", uri)
        ) {
            return appProperties.rateLimit.getPolicy(RateLimitCategory.OTP_STRICT)
        }

        // 2. Strict Authentication Endpoints (Sign-In, Sign-Up, Pre-Login, Password Reset Confirm)
        if (pathMatcher.match("/api/v1/auth/sign-in", uri) ||
            pathMatcher.match("/api/v1/auth/pre-login", uri) ||
            pathMatcher.match("/api/v1/auth/sign-up/**", uri) ||
            pathMatcher.match("/api/v1/auth/password-reset/confirm", uri)
        ) {
            return appProperties.rateLimit.getPolicy(RateLimitCategory.AUTH_STRICT)
        }

        // 3. Token Maintenance Endpoints
        if (pathMatcher.match("/api/v1/auth/refresh-token", uri) ||
            pathMatcher.match("/api/v1/auth/sign-out", uri)
        ) {
            return appProperties.rateLimit.getPolicy(RateLimitCategory.TOKEN_REFRESH)
        }

        // 4. Authenticated API Endpoints
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal != "anonymousUser") {
            return appProperties.rateLimit.getPolicy(RateLimitCategory.AUTHENTICATED)
        }

        // 5. Global Fallback for Public Endpoints
        return appProperties.rateLimit.getPolicy(RateLimitCategory.GLOBAL_PUBLIC)
    }

    private fun isBypassedPath(uri: String): Boolean {
        return pathMatcher.match("/actuator/**", uri) ||
            pathMatcher.match("/error", uri) ||
            pathMatcher.match("/favicon.ico", uri)
    }
}
