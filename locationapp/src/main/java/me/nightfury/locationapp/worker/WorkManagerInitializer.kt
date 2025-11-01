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
        if (isTestProcess()) {
            android.util.Log.i(
                "WorkManagerInitializer",
                "Skipping WorkManager init in test process."
            )
            val testConfig = Configuration.Builder().build()
            WorkManager.initialize(context, testConfig)
            return WorkManager.getInstance(context)
        }
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

    private fun isTestProcess(): Boolean {
        // 1️⃣ Check for Android instrumented tests (has androidx.test runtime)
        val isInstrumentedTest = try {
            Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            true
        } catch (_: ClassNotFoundException) {
            false
        }

        // 2️⃣ Check for local (Robolectric / JVM) tests
        val isUnitTest = "true" == System.getProperty("robolectric") ||
                (System.getProperty("java.vendor")?.contains("JetBrains") == true)

        return isInstrumentedTest || isUnitTest
    }
}