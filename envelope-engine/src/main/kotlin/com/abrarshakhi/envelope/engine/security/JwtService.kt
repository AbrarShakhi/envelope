package com.abrarshakhi.envelope.engine.security

import com.abrarshakhi.envelope.engine.config.AppProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey

@Service
class JwtService(
    private val appProperties: AppProperties,
) {
    private val signingKey: SecretKey by lazy {
        val secretBytes = appProperties.jwt.secretKey.toByteArray(StandardCharsets.UTF_8)
        if (secretBytes.size < 32) {
            val padded = ByteArray(32)
            System.arraycopy(secretBytes, 0, padded, 0, secretBytes.size.coerceAtMost(32))
            Keys.hmacShaKeyFor(padded)
        } else {
            Keys.hmacShaKeyFor(secretBytes)
        }
    }

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    fun extractUserId(token: String): Long? {
        val claims = extractAllClaims(token)
        return (claims["userId"] as? Number)?.toLong()
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun generateAccessToken(userPrincipal: UserPrincipal): String {
        val now = Date()
        val expiryDate = Date(now.time + appProperties.jwt.expiration)

        return Jwts.builder()
            .subject(userPrincipal.username)
            .claim("userId", userPrincipal.id)
            .claim("email", userPrincipal.email)
            .claim("role", userPrincipal.role.name)
            .issuer(appProperties.jwt.issuer)
            .audience().add(appProperties.jwt.audience).and()
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(signingKey)
            .compact()
    }

    fun getAccessTokenExpirationSeconds(): Long = appProperties.jwt.expiration / 1000

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        return try {
            val username = extractUsername(token)
            username.equals(userDetails.username, ignoreCase = true) && !isTokenExpired(token)
        } catch (ex: Exception) {
            false
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = extractClaim(token, Claims::getExpiration)
            expiration.before(Date())
        } catch (ex: Exception) {
            true
        }
    }
}
