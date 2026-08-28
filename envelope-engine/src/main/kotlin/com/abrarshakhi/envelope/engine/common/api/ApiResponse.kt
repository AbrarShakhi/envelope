package com.abrarshakhi.envelope.engine.common.api

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: Instant = Instant.now(),
) {
    companion object {
        fun <T> success(message: String, data: T? = null): ApiResponse<T> = ApiResponse(
            success = true,
            message = message,
            data = data,
        )

        fun <T> error(message: String, data: T? = null): ApiResponse<T> = ApiResponse(
            success = false,
            message = message,
            data = data,
        )
    }
}
