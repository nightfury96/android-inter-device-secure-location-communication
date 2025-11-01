package me.nightfury.locationapp.worker

import android.content.Context
import androidx.startup.Initializer
import me.nightfury.locationapp.di.InitializerEntryPoint

class DependencyGraphInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        if (isTestProcess()) {
            android.util.Log.i(
                "DependencyGraphInitializer",
                "Skipping initialization in test process."
            )
            return
        }
        InitializerEntryPoint.Companion.resolve(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
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