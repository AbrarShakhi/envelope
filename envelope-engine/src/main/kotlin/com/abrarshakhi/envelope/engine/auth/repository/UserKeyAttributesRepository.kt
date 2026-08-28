package com.abrarshakhi.envelope.engine.auth.repository

import com.abrarshakhi.envelope.engine.auth.entity.UserKeyAttributes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserKeyAttributesRepository : JpaRepository<UserKeyAttributes, Long> {
    fun findByUserId(userId: Long): Optional<UserKeyAttributes>
}
