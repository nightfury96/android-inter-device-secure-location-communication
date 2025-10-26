package me.nightfury.locationapp.worker

import android.content.Context
import androidx.startup.Initializer
import me.nightfury.locationapp.di.InitializerEntryPoint

class DependencyGraphInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        InitializerEntryPoint.Companion.resolve(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

}