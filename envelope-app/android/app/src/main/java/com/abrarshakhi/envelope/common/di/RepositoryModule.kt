package com.abrarshakhi.envelope.common.di

import com.abrarshakhi.envelope.auth.data.local.AuthLocalDataSource
import com.abrarshakhi.envelope.auth.data.repository.AuthRepositoryImpl
import com.abrarshakhi.envelope.auth.domain.repository.AuthRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { AuthLocalDataSource(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
