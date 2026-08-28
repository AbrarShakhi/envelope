package com.abrarshakhi.envelope.engine.auth.dto.response

import com.abrarshakhi.envelope.engine.auth.entity.Role
import java.time.Instant

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String?,
    val role: Role,
    val isEmailVerified: Boolean,
    val createdAt: Instant,
)
