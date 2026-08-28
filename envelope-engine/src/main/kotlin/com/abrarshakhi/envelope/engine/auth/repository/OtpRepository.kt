package com.abrarshakhi.envelope.engine.auth.repository

import com.abrarshakhi.envelope.engine.auth.entity.Otp
import com.abrarshakhi.envelope.engine.auth.entity.OtpPurpose
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OtpRepository : JpaRepository<Otp, Long> {

    fun findTopByIdentifierIgnoreCaseAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
        identifier: String,
        purpose: OtpPurpose,
    ): Optional<Otp>

    @Modifying
    @Query(
        """
        UPDATE Otp o
        SET o.isUsed = true
        WHERE LOWER(o.identifier) = LOWER(:identifier) AND o.purpose = :purpose AND o.isUsed = false
        """,
    )
    fun invalidateActiveOtps(
        @Param("identifier") identifier: String,
        @Param("purpose") purpose: OtpPurpose,
    )

    fun findTopByIdentifierIgnoreCaseAndPurposeOrderByCreatedAtDesc(
        identifier: String,
        purpose: OtpPurpose,
    ): Optional<Otp>
}
