package com.abrarshakhi.envelope.common.di

import com.abrarshakhi.envelope.common.MainViewModel
import com.abrarshakhi.envelope.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { OnboardingViewModel() }
}
