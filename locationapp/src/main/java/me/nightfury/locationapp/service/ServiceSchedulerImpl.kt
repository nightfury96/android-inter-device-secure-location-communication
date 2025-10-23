package me.nightfury.locationapp.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import me.nightfury.locationapp.worker.BootWorker
import me.nightfury.locationdomain.ServiceScheduler
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedlogger.AppLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ServiceSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository
) : ServiceScheduler {
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }
    private val workTag = "ServiceRecoveryWorkTag"
    private val logSource = "ServiceSchedulerImpl"

    override suspend fun startService() {
        try {
            LocationForegroundService.start(context)
            // Schedule a persistent work request for recovery after boot/system kill
            scheduleBootRecoveryWork()
            AppLogger.i(logSource, "Foreground Service started. Recovery work scheduled.")
        } catch (e: Exception) {
            AppLogger.e(
                logSource,
                "Failed to start service (e.g., missing permissions or OS restrictions).",
                e
            )
            locationRepository.setServiceStatus(false)
            throw IllegalStateException("Failed to start service, check logs for details.", e)
        }
    }

    override suspend fun stopService() {
        LocationForegroundService.stop(context)
        // Service itself sets the status to false in its onDestroy

        // Cancel the pending recovery work as the service is deliberately stopped
        workManager.cancelUniqueWork(workTag)

        AppLogger.i(
            logSource,
            "Foreground Service stopped. Recovery work cancelled."
        )
    }

    // Schedules a OneTimeWorkRequest
    private fun scheduleBootRecoveryWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val recoveryRequest = OneTimeWorkRequest.Builder(BootWorker::class.java)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .addTag(workTag)
            .build()

        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.KEEP,
            recoveryRequest
        )
    }
}