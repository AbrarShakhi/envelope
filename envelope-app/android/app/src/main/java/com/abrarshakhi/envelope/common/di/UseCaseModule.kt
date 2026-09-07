package com.abrarshakhi.envelope.common.di

import com.abrarshakhi.envelope.auth.domain.usecase.ObserveLoginStatusUseCase
import com.abrarshakhi.envelope.auth.domain.usecase.SetLoginStatusUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { ObserveLoginStatusUseCase(get()) }
    factory { SetLoginStatusUseCase(get()) }
}
