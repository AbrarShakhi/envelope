package com.abrarshakhi.envelope.common.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator(startDestination: AppRoute) {

    val backStack: SnapshotStateList<AppRoute> = mutableStateListOf(startDestination)

    fun navigateTo(destination: AppRoute) {
        backStack.add(destination)
    }

    fun replaceAll(destination: AppRoute) {
        backStack.clear()
        backStack.add(destination)
    }

    fun goBack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
