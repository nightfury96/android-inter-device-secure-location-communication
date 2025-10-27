package me.nightfury

import android.app.Application
import me.nightfury.internetapp.data.di.appModule
import me.nightfury.sharedlogger.AppLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InternetApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@InternetApp)
            modules(appModule)
        }

        AppLogger.i("InternetApp", "App started")
    }
}