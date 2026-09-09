package com.abrarshakhi.envelope.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.envelope.auth.presentation.SignInScreen
import com.abrarshakhi.envelope.auth.presentation.SignUpScreen
import com.abrarshakhi.envelope.home.presentation.HomeScreen
import com.abrarshakhi.envelope.onboarding.OnboardingScreen
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppRoot(startDestination: AppRoute) {
    val navigator = remember(startDestination) {
        AppNavigator(startDestination)
    }

    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = {
            when (val route = it) {
                AppRoute.Home -> NavEntry(route) {
                    HomeScreen()
                }

                AppRoute.Onboarding -> NavEntry(route) {
                    OnboardingScreen(
                        onFinish = {
                            navigator.clearAndNavigateTo(AppRoute.SignIn)
                        },
                    )
                }

                AppRoute.SignIn -> NavEntry(route) {
                    SignInScreen(
                        onSignInSuccess = {
                            navigator.clearAndNavigateTo(AppRoute.Home)
                        },
                    )
                }

                AppRoute.SignUp -> NavEntry(route) {
                    SignUpScreen(
                        onSignUpSuccess = {
                        },
                    )
                }
            }
        },
    )
}

