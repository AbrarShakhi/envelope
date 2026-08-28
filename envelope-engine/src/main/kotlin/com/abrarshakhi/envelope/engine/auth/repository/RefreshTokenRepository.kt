package com.abrarshakhi.envelope.engine.auth.repository

import com.abrarshakhi.envelope.engine.auth.entity.RefreshToken
import com.abrarshakhi.envelope.engine.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByTokenHashAndIsRevokedFalse(tokenHash: String): Optional<RefreshToken>

    @Modifying
    @Query(
        """
        UPDATE RefreshToken r
        SET r.isRevoked = true
        WHERE r.user = :user AND r.isRevoked = false
        """,
    )
    fun revokeAllUserTokens(@Param("user") user: User)

    @Modifying
    @Query(
        """
        UPDATE RefreshToken r
        SET r.isRevoked = true
        WHERE r.tokenHash = :tokenHash
        """,
    )
    fun revokeByTokenHash(@Param("tokenHash") tokenHash: String)

    @Modifying
    @Query(
        """
        DELETE FROM RefreshToken r
        WHERE r.expiresAt < :cutoff OR r.isRevoked = true
        """,
    )
    fun deleteExpiredOrRevokedBefore(@Param("cutoff") cutoff: java.time.Instant): Int
}
