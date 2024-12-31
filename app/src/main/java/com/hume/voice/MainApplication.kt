package com.hume.voice

import android.app.Application
import com.hume.voice.common.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@MainApplication)
            modules(
                listOf(
                    viewModelModule,
                )
            )
        }
    }
}