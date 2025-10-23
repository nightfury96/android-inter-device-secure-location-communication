package me.nightfury.locationapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.nightfury.sharedlogger.AppLogger

@HiltAndroidApp
class LocationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.i("LocationApp", "App started")
    }
}