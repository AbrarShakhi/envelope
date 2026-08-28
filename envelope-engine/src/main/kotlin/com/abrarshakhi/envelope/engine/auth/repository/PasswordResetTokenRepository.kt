package com.abrarshakhi.envelope.engine.auth.repository

import com.abrarshakhi.envelope.engine.auth.entity.PasswordResetToken
import com.abrarshakhi.envelope.engine.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByTokenHashAndIsUsedFalse(tokenHash: String): Optional<PasswordResetToken>

    @Modifying
    @Query(
        """
        UPDATE PasswordResetToken p
        SET p.isUsed = true
        WHERE p.user = :user AND p.isUsed = false
        """,
    )
    fun invalidateAllUserTokens(@Param("user") user: User)
}
