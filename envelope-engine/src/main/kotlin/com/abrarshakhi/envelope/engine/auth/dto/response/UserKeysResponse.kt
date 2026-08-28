package com.abrarshakhi.envelope.engine.auth.dto.response

data class UserKeysResponse(
    val publicKey: String,
    val encryptedPrivateKey: String,
    val encryptedRecoveryKey: String,
    val keyVersion: Int,
)
