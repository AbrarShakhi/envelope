package com.abrarshakhi.envelope.common.di

import com.abrarshakhi.envelope.common.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
}
