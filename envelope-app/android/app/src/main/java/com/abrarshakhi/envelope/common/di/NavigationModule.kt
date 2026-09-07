package com.abrarshakhi.envelope.common.di

import com.abrarshakhi.envelope.auth.presentation.LoginScreen
import com.abrarshakhi.envelope.common.navigation.AppNavigator
import com.abrarshakhi.envelope.common.navigation.AppRoute
import com.abrarshakhi.envelope.home.presentation.HomeScreen
import com.abrarshakhi.envelope.onboarding.OnboardingScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
    single { AppNavigator(startDestination = AppRoute.Home) }

    navigation<AppRoute.Onboarding> {
        val navigator = get<AppNavigator>()
        OnboardingScreen(
            onFinish = { navigator.navigateTo(AppRoute.Login) },
        )
    }

    navigation<AppRoute.Login> {
        val navigator = get<AppNavigator>()
        LoginScreen(
            onLoginSuccess = { navigator.replaceAll(AppRoute.Home) },
        )
    }

    navigation<AppRoute.Home> {
        HomeScreen()
    }
}
