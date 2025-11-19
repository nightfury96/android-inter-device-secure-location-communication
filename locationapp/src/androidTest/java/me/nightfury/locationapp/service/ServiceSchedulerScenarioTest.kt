package me.nightfury.locationapp.service

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.nightfury.locationdata.di.LocalModule
import me.nightfury.locationdata.di.RepositoryModule
import me.nightfury.locationdomain.repo.LocationRepository
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(LocalModule::class, RepositoryModule::class)
@RunWith(AndroidJUnit4::class)
class ServiceSchedulerScenarioTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule: GrantPermissionRule = run {
        // 1. Start with the basic permissions needed on ALL versions
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        // 2. If we are on Android 10 (API 29) or newer, add Background Location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        // 2. Only add POST_NOTIFICATIONS if the device is Android 13 (API 33) or newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // 3. Create the rule
        GrantPermissionRule.grant(*permissions.toTypedArray())
    }

    @Inject
    lateinit var repository: LocationRepository

    @Inject
    lateinit var scheduler: ServiceSchedulerImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun start_and_stop_foreground_service_updates_repository_status() = runBlocking {
        // ─────────────── Start Service ───────────────
        scheduler.startService()

        val running = withTimeoutOrNull(10000) {
            while (!repository.isServiceRunning()) {
                delay(100) // Check every 100ms
            }
            repository.isServiceRunning()
        } ?: false

        Assert.assertTrue("Service should be running after startService()", running)

        // ─────────────── Stop Service ───────────────

        delay(5000)
        scheduler.stopService()
        val isStillRunning = withTimeoutOrNull(60000) {
            while (repository.isServiceRunning()) {
                delay(100)
            }
            false // It stopped!
        } ?: true // It timed out, so it's still running
        Assert.assertFalse("Service should not be running after stopService()", isStillRunning)
    }
}