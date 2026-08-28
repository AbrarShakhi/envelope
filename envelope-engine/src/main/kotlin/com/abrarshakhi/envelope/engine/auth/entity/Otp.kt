package com.abrarshakhi.envelope.engine.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "otps")
class Otp(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    var identifier: String,

    @Column(name = "otp_hash", nullable = false, length = 255)
    var otpHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var purpose: OtpPurpose,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,

    @Column(name = "max_attempts", nullable = false)
    var maxAttempts: Int = 5,

    @Column(name = "is_used", nullable = false)
    var isUsed: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
