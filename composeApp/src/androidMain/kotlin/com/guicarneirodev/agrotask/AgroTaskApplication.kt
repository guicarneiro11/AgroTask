package com.guicarneirodev.agrotask

import android.app.Application
import com.google.firebase.FirebaseApp
import com.guicarneirodev.agrotask.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AgroTaskApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@AgroTaskApplication)
            modules(appModule)
        }
    }
}