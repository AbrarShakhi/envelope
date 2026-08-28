package com.abrarshakhi.envelope.engine.common.exception

class InvalidOtpException(
    message: String,
    val remainingAttempts: Int? = null,
) : RuntimeException(message)
