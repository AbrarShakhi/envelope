package com.abrarshakhi.envelope.common.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val AUTH_PREFERENCES_NAME = "auth_preferences"

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AUTH_PREFERENCES_NAME,
)

val dataStoreModule = module {
    single<DataStore<Preferences>> { androidContext().authDataStore }
}
