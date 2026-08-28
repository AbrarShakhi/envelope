package com.abrarshakhi.envelope.engine.auth.dto.request

import jakarta.validation.constraints.*

data class PasswordResetConfirmRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Reset token is required")
    @field:Size(max = 255, message = "Reset token must not exceed 255 characters")
    val resetToken: String,

    @field:NotBlank(message = "New auth hash is required")
    @field:Size(max = 512, message = "New auth hash must not exceed 512 characters")
    val newAuthHash: String,

    @field:NotBlank(message = "New salt is required")
    @field:Size(max = 255, message = "New salt must not exceed 255 characters")
    val newSalt: String,

    @field:NotBlank(message = "KDF algorithm is required")
    @field:Pattern(regexp = "^(ARGON2ID|ARGON2I|ARGON2D)$", message = "Unsupported KDF algorithm. Must be ARGON2ID")
    val kdfAlgorithm: String = "ARGON2ID",

    @field:NotNull(message = "KDF iterations is required")
    @field:Min(value = 1, message = "KDF iterations must be at least 1")
    @field:Max(value = 100, message = "KDF iterations must not exceed 100")
    val kdfIterations: Int = 3,

    @field:NotNull(message = "KDF memory is required")
    @field:Min(value = 1024, message = "KDF memory must be at least 1024 KB")
    @field:Max(value = 1048576, message = "KDF memory must not exceed 1048576 KB")
    val kdfMemoryKb: Int = 65536,

    @field:NotNull(message = "KDF parallelism is required")
    @field:Min(value = 1, message = "KDF parallelism must be at least 1")
    @field:Max(value = 32, message = "KDF parallelism must not exceed 32")
    val kdfParallelism: Int = 4,

    @field:NotBlank(message = "New public key is required")
    @field:Size(max = 10000, message = "New public key must not exceed 10000 characters")
    val newPublicKey: String,

    @field:Size(max = 10000, message = "New encrypted master key must not exceed 10000 characters")
    val newEncryptedMasterKey: String? = null,

    @field:NotBlank(message = "New encrypted private key is required")
    @field:Size(max = 10000, message = "New encrypted private key must not exceed 10000 characters")
    val newEncryptedPrivateKey: String,

    @field:NotBlank(message = "New encrypted recovery key is required")
    @field:Size(max = 10000, message = "New encrypted recovery key must not exceed 10000 characters")
    val newEncryptedRecoveryKey: String,
)
