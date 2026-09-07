package com.abrarshakhi.envelope.common.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    data object Onboarding : AppRoute

    @Serializable
    data object Login : AppRoute

    @Serializable
    data object Home : AppRoute
}
