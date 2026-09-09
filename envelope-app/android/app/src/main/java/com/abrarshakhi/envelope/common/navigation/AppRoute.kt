package com.abrarshakhi.envelope.common.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object SignIn : AppRoute

    @Serializable
    data object SignUp: AppRoute

    @Serializable
    data object Home : AppRoute
}
