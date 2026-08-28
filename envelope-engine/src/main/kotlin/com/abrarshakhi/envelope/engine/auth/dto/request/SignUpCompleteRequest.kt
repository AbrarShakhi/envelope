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

    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String? = null,

    @field:NotBlank(message = "OTP is required")
    @field:Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit numeric code")
    val otp: String,

    @field:NotBlank(message = "Auth hash is required")
    val authHash: String,

    @field:NotBlank(message = "Salt is required")
    val salt: String,

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

    @field:NotBlank(message = "Public key is required")
    val publicKey: String,

    @field:NotBlank(message = "Encrypted private key is required")
    val encryptedPrivateKey: String,

    @field:NotBlank(message = "Encrypted recovery key is required")
    val encryptedRecoveryKey: String,
)
