package com.abrarshakhi.envelope.auth.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val AUTH_PREFERENCES_FILE_NAME = "auth_token_preferences"

val Context.authDataStore by preferencesDataStore(name = AUTH_PREFERENCES_FILE_NAME)
