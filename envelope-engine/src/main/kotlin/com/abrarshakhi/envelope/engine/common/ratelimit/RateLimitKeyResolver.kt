package com.abrarshakhi.envelope.engine.common.ratelimit

import com.abrarshakhi.envelope.engine.security.UserPrincipal
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class RateLimitKeyResolver {

    private val ipHeaders = listOf(
        "X-Forwarded-For",
        "CF-Connecting-IP",
        "X-Real-IP",
        "Forwarded",
    )

    fun resolveKey(request: HttpServletRequest, keyType: RateLimitKeyType, category: RateLimitCategory): String {
        val clientIdentifier = when (keyType) {
            RateLimitKeyType.IP -> resolveClientIp(request)
            RateLimitKeyType.USER_ID -> resolveUserId() ?: resolveClientIp(request)
            RateLimitKeyType.IP_OR_USER -> resolveUserId() ?: resolveClientIp(request)
            RateLimitKeyType.GLOBAL -> "GLOBAL"
        }

        // Scope key by category and endpoint prefix to ensure isolation between distinct endpoints
        val sanitizedUri = request.requestURI.trimEnd('/')
        return "rl:${category.name}:$clientIdentifier:$sanitizedUri"
    }

    fun resolveClientIp(request: HttpServletRequest): String {
        for (header in ipHeaders) {
            val value = request.getHeader(header)
            if (!value.isNullOrBlank() && !"unknown".equals(value, ignoreCase = true)) {
                // Return first IP in comma-separated list
                val clientIp = value.split(",")[0].trim()
                if (clientIp.isNotBlank()) {
                    return clientIp
                }
            }
        }
        return request.remoteAddr ?: "unknown-ip"
    }

    fun resolveUserId(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated) {
            val principal = authentication.principal
            if (principal is UserPrincipal) {
                return "user:${principal.id}"
            }
            if (principal is String && principal != "anonymousUser") {
                return "user:$principal"
            }
        }
        return null
    }
}
