//package me.nightfury.locationapp.worker
//
//import android.content.Context
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.rule.GrantPermissionRule
//import androidx.work.Configuration
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.testing.SynchronousExecutor
//import androidx.work.testing.WorkManagerTestInitHelper
//import dagger.hilt.android.testing.HiltAndroidRule
//import dagger.hilt.android.testing.HiltAndroidTest
//import dagger.hilt.android.testing.UninstallModules
//import io.mockk.coVerify
//import kotlinx.coroutines.runBlocking
//import me.nightfury.locationapp.di.ServiceModule
//import me.nightfury.locationapp.service.ServiceSchedulerImpl
//import me.nightfury.locationdomain.repo.LocationRepository
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import javax.inject.Inject
//
//@HiltAndroidTest
//@RunWith(AndroidJUnit4::class)
//@UninstallModules(ServiceModule::class)
//class BootRecoveryScenarioTest {
//
//    @get:Rule
//    val hiltRule = HiltAndroidRule(this)
//
//    @get:Rule
//    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
//        android.Manifest.permission.ACCESS_FINE_LOCATION,
//        android.Manifest.permission.ACCESS_COARSE_LOCATION,
//        android.Manifest.permission.FOREGROUND_SERVICE,
//        android.Manifest.permission.FOREGROUND_SERVICE_LOCATION,
//        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//        android.Manifest.permission.POST_NOTIFICATIONS,
//    )
//    private lateinit var context: Context
//
//    @Inject
//    lateinit var hiltWorkerFactory: androidx.hilt.work.HiltWorkerFactory
//
//    @Inject
//    lateinit var repository: LocationRepository
//
//    @Inject
//    lateinit var scheduler: ServiceSchedulerImpl
//
//    @Before
//    fun setup() {
//        hiltRule.inject()
//        context = ApplicationProvider.getApplicationContext()
//
//        val config = Configuration.Builder()
//            .setWorkerFactory(hiltWorkerFactory)
//            .setExecutor(SynchronousExecutor())
//            .build()
//        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
//    }
//
//    // --------------------------------------------------------------------
//    // TEST 1 — Worker restarts service if it was running before reboot
//    // --------------------------------------------------------------------
//    @Test
//    fun bootWorker_restarts_service_if_running_before_reboot() = runBlocking {
//        repository.setServiceStatus(true)
//
//        val request = OneTimeWorkRequestBuilder<BootWorker>().build()
//        val result = WorkManagerTestInitHelper.getTestDriver(context)!!
//        val workManager = androidx.work.WorkManager.getInstance(context)
//
//        workManager.enqueue(request).result.get()
//        result.setAllConstraintsMet(request.id)
//        result.setInitialDelayMet(request.id)
//
//        // The worker should start the service again
//        coVerify(timeout = 10000) { scheduler.startService() }
//    }
//
//    // --------------------------------------------------------------------
//    // TEST 2 — Worker does NOT restart service if it was stopped before reboot
//    // --------------------------------------------------------------------
//    @Test
//    fun bootWorker_does_not_restart_if_service_stopped() = runBlocking {
//        repository.setServiceStatus(false)
//
//        val request = OneTimeWorkRequestBuilder<BootWorker>().build()
//        val result = WorkManagerTestInitHelper.getTestDriver(context)!!
//        val workManager = androidx.work.WorkManager.getInstance(context)
//
//        workManager.enqueue(request).result.get()
//        result.setAllConstraintsMet(request.id)
//        result.setInitialDelayMet(request.id)
//
//        // The worker should NOT start the service
//        coVerify(exactly = 0) { scheduler.startService() }
//    }
//}