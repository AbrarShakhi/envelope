package com.abrarshakhi.envelope.engine.auth.dto.response

data class PreLoginResponse(
    val salt: String,
    val kdfAlgorithm: String,
    val kdfIterations: Int,
    val kdfMemoryKb: Int,
    val kdfParallelism: Int,
)
