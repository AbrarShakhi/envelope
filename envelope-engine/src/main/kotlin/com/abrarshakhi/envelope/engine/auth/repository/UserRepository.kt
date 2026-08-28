package com.abrarshakhi.envelope.engine.auth.repository

import com.abrarshakhi.envelope.engine.auth.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailIgnoreCase(email: String): Optional<User>
    fun findByUsernameIgnoreCase(username: String): Optional<User>

    @Query(
        """
        SELECT u
        FROM User u
        WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)
        """,
    )
    fun findByUsernameOrEmailIgnoreCase(@Param("identifier") identifier: String): Optional<User>

    fun existsByEmailIgnoreCase(email: String): Boolean
    fun existsByUsernameIgnoreCase(username: String): Boolean
}
