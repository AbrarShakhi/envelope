package com.abrarshakhi.envelope.common

import android.app.Application
import com.abrarshakhi.envelope.common.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class EnvelopeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@EnvelopeApplication)
            modules(appModules)
        }
    }
}
