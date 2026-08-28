package com.abrarshakhi.envelope.engine.auth.dto.response

data class UserKeysResponse(
    val publicKey: String,
    val encryptedMasterKey: String? = null,
    val encryptedPrivateKey: String,
    val encryptedRecoveryKey: String,
    val keyVersion: Int,
)
