package com.abrarshakhi.envelope.engine.common.ratelimit

import com.abrarshakhi.envelope.engine.common.exception.RateLimitExceededException
import com.abrarshakhi.envelope.engine.config.AppProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val rateLimiterRegistry: RateLimiterRegistry,
    private val rateLimitKeyResolver: RateLimitKeyResolver,
    private val appProperties: AppProperties,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (!appProperties.rateLimit.enabled || handler !is HandlerMethod) {
            return true
        }

        // Check method-level or class-level @RateLimit annotation
        val annotation = handler.getMethodAnnotation(RateLimit::class.java)
            ?: handler.beanType.getAnnotation(RateLimit::class.java)
            ?: return true // If no explicit annotation, handled by RateLimitFilter

        val policy = if (annotation.category != RateLimitCategory.CUSTOM) {
            appProperties.rateLimit.getPolicy(annotation.category)
        } else {
            RateLimitPolicy(
                category = RateLimitCategory.CUSTOM,
                limit = annotation.limit,
                windowSeconds = annotation.windowSeconds,
                keyType = annotation.keyType,
            )
        }

        val bucketKey = rateLimitKeyResolver.resolveKey(request, policy.keyType, policy.category)
        val bucket = rateLimiterRegistry.getOrCreateBucket(bucketKey, policy)
        val result = bucket.tryConsume(1)

        response.setHeader("X-RateLimit-Limit", result.limit.toString())
        response.setHeader("X-RateLimit-Remaining", result.remaining.toString())
        response.setHeader("X-RateLimit-Reset", result.resetSeconds.toString())

        if (!result.isAllowed) {
            throw RateLimitExceededException(
                message = "Rate limit exceeded for endpoint. Please try again in ${result.retryAfterSeconds} seconds.",
                retryAfterSeconds = result.retryAfterSeconds,
                limit = result.limit,
                windowSeconds = policy.windowSeconds,
            )
        }

        return true
    }
}
