package me.nightfury.locationapp.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@UninstallModules(LocalModule::class, RepositoryModule::class)
@RunWith(AndroidJUnit4::class)
class ServiceSchedulerScenarioTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS,
    )

    @Inject
    lateinit var repository: LocationRepository

    @Inject
    lateinit var scheduler: ServiceSchedulerImpl

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun start_and_stop_foreground_service_updates_repository_status() = runTest {
        // ─────────────── Start Service ───────────────
        scheduler.startService()
        advanceUntilIdle()

        val running = withTimeoutOrNull(10000) {
            while (!repository.isServiceRunning()) {
            }
            repository.isServiceRunning()
        } ?: false

        Assert.assertTrue("Service should be running after startService()", running)

        // ─────────────── Stop Service ───────────────
        scheduler.stopService()

        val stopped = withTimeoutOrNull(10000) {
            while (repository.isServiceRunning()) {
            }
            repository.isServiceRunning()
        } ?: true
        Assert.assertFalse("Service should not be running after stopService()", stopped)
    }
}