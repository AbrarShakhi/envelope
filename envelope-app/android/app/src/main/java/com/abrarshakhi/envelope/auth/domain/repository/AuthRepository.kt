package com.abrarshakhi.envelope.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun setLoggedIn(isLoggedIn: Boolean)
}