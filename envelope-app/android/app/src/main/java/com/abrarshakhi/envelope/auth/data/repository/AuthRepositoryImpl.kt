package com.abrarshakhi.envelope.auth.data.repository

import com.abrarshakhi.envelope.auth.data.local.AuthLocalDataSource
import com.abrarshakhi.envelope.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val localDataSource: AuthLocalDataSource,
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = localDataSource.isLoggedIn

    override suspend fun setLoggedIn(isLoggedIn: Boolean) {
        localDataSource.setLoggedIn(isLoggedIn)
    }
}
