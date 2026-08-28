package com.abrarshakhi.envelope.engine.auth.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false, length = 50)
    var username: String,

    @Column(unique = true, nullable = false, length = 255)
    var email: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @Column(length = 100)
    var name: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var role: Role = Role.USER,

    @Column(name = "is_email_verified", nullable = false)
    var isEmailVerified: Boolean = false,

    @Column(name = "is_account_non_locked", nullable = false)
    var isAccountNonLocked: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "last_login_at")
    var lastLoginAt: Instant? = null,

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = true)
    var keyAttributes: UserKeyAttributes? = null,
)
