package com.abrarshakhi.envelope.common.di

import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    dataStoreModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
    navigationModule
)
