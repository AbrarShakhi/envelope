package com.abrarshakhi.envelope.auth.domain.usecase

import com.abrarshakhi.envelope.auth.domain.repository.AuthRepository

class SetLoginStatusUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(isLoggedIn: Boolean) {
        authRepository.setLoggedIn(isLoggedIn)
    }
}