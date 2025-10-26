package me.nightfury.locationapp.worker

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import me.nightfury.locationapp.di.InitializerEntryPoint
import javax.inject.Inject

class WorkManagerInitializer : Initializer<WorkManager>, Configuration.Provider {

    @Inject
    lateinit var locationWorkerFactory: LocationWorkerFactory

    override fun create(context: Context): WorkManager {
        InitializerEntryPoint.Companion.resolve(context).inject(this)
        WorkManager.Companion.initialize(context, workManagerConfiguration)
        return WorkManager.Companion.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(DependencyGraphInitializer::class.java)

    override val workManagerConfiguration: Configuration
        get() {
            val workerFactory = DelegatingWorkerFactory()
            workerFactory.addFactory(locationWorkerFactory)
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

}