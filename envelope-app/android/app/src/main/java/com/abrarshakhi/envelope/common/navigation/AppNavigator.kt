package com.abrarshakhi.envelope.common.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator(startDestination: AppRoute) {

    val backStack: SnapshotStateList<AppRoute> = mutableStateListOf(startDestination)

    fun navigateTo(destination: AppRoute) {
        backStack.add(destination)
    }

    fun clearAndNavigateTo(destination: AppRoute) {
        backStack.clear()
        backStack.add(destination)
    }

    fun goBack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun navigateBackTo(destination: AppRoute): Boolean {
        val index = backStack.indexOf(destination)

        if (index == -1) return false

        while (backStack.last() != destination) {
            backStack.removeAt(backStack.lastIndex)
        }

        return true
    }
}
