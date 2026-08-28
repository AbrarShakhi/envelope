package com.abrarshakhi.envelope.engine.auth.dto.request

import jakarta.validation.constraints.*

data class SignUpCompleteRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username can only contain alphanumeric characters and underscores",
    )
    val username: String,

    @field:NotBlank(message = "OTP is required")
    @field:Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit numeric code")
    val otp: String,

    @field:NotBlank(message = "Auth hash is required")
    @field:Size(max = 512, message = "Auth hash must not exceed 512 characters")
    val authHash: String,

    @field:NotBlank(message = "Salt is required")
    @field:Size(max = 255, message = "Salt must not exceed 255 characters")
    val salt: String,

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

    @field:NotBlank(message = "Public key is required")
    @field:Size(max = 10000, message = "Public key must not exceed 10000 characters")
    val publicKey: String,

    @field:Size(max = 10000, message = "Encrypted master key must not exceed 10000 characters")
    val encryptedMasterKey: String? = null,

    @field:NotBlank(message = "Encrypted private key is required")
    @field:Size(max = 10000, message = "Encrypted private key must not exceed 10000 characters")
    val encryptedPrivateKey: String,

    @field:NotBlank(message = "Encrypted recovery key is required")
    @field:Size(max = 10000, message = "Encrypted recovery key must not exceed 10000 characters")
    val encryptedRecoveryKey: String,
)
