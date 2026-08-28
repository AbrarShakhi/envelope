package com.abrarshakhi.envelope.engine.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PasswordResetConfirmRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Reset token is required")
    val resetToken: String,

    @field:NotBlank(message = "New auth hash is required")
    val newAuthHash: String,

    @field:NotBlank(message = "New salt is required")
    val newSalt: String,

    @field:NotBlank(message = "KDF algorithm is required")
    val kdfAlgorithm: String = "ARGON2ID",

    @field:NotNull(message = "KDF iterations is required")
    @field:Min(value = 1, message = "KDF iterations must be at least 1")
    val kdfIterations: Int = 3,

    @field:NotNull(message = "KDF memory is required")
    @field:Min(value = 1024, message = "KDF memory must be at least 1024 KB")
    val kdfMemoryKb: Int = 65536,

    @field:NotNull(message = "KDF parallelism is required")
    @field:Min(value = 1, message = "KDF parallelism must be at least 1")
    val kdfParallelism: Int = 4,

    @field:NotBlank(message = "New public key is required")
    val newPublicKey: String,

    @field:NotBlank(message = "New encrypted private key is required")
    val newEncryptedPrivateKey: String,

    @field:NotBlank(message = "New encrypted recovery key is required")
    val newEncryptedRecoveryKey: String,
)
