package com.abrarshakhi.envelope.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppRoot() {
    val navigator: AppNavigator = koinInject()

    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = koinEntryProvider()
    )
}
