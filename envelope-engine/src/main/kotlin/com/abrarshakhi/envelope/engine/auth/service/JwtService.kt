package com.abrarshakhi.envelope.engine.auth.service

import com.abrarshakhi.envelope.engine.auth.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import java.util.function.Function
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value($$"${spring.security.jwt.secret-key}") private val secretKey: String,
    @Value($$"${spring.security.jwt.expiration}") private val jwtExpiration: Long,
    @Value($$"${spring.security.jwt.refresh-token-expiration}") private val refreshExpiration: Long,
    @Value($$"${spring.security.jwt.issuer}") private val issuer: String,
    @Value($$"${spring.security.jwt.audience}") private val audience: String,
) {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun extractUsername(token: String): String = extractClaim(token, Claims::getSubject)

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser().verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun generateToken(userDetails: UserDetails): String =
        Jwts.builder()
            .subject(userDetails.username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtExpiration))
            .issuer(issuer)
            .audience().add(audience).and()
            .claim("userId", (userDetails as User).id)
            .claim("email", userDetails.username)
            .signWith(signingKey)
            .compact()

    fun generateRefreshToken(userDetails: UserDetails): String =
        Jwts.builder()
            .subject(userDetails.username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshExpiration))
            .issuer(issuer)
            .signWith(signingKey)
            .compact()

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = extractClaim(token, Claims::getExpiration)
        return expiration.before(Date())
    }
}
