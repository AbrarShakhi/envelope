package com.abrarshakhi.envelope.common.di

import com.abrarshakhi.envelope.auth.presentation.AuthGateViewModel
import com.abrarshakhi.envelope.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { AuthGateViewModel() }
    viewModel { OnboardingViewModel() }
}
