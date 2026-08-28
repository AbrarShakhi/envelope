package com.abrarshakhi.envelope.engine.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "user_key_attributes")
class UserKeyAttributes(
    @Id
    var userId: Long? = null,

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(nullable = false, length = 255)
    var salt: String,

    @Column(name = "kdf_algorithm", nullable = false, length = 50)
    var kdfAlgorithm: String = "ARGON2ID",

    @Column(name = "kdf_iterations", nullable = false)
    var kdfIterations: Int = 3,

    @Column(name = "kdf_memory_kb", nullable = false)
    var kdfMemoryKb: Int = 65536,

    @Column(name = "kdf_parallelism", nullable = false)
    var kdfParallelism: Int = 4,

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    var publicKey: String,

    @Column(name = "encrypted_master_key", columnDefinition = "TEXT")
    var encryptedMasterKey: String? = null,

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    var encryptedPrivateKey: String,

    @Column(name = "encrypted_recovery_key", nullable = false, columnDefinition = "TEXT")
    var encryptedRecoveryKey: String,

    @Column(name = "key_version", nullable = false)
    var keyVersion: Int = 1,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
