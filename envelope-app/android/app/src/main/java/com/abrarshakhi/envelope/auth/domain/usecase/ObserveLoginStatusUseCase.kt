package com.abrarshakhi.envelope.auth.domain.usecase

import com.abrarshakhi.envelope.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveLoginStatusUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = authRepository.isLoggedIn
}