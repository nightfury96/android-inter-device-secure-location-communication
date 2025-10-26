package me.nightfury.locationapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.nightfury.locationdomain.ServiceScheduler
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedlogger.AppLogger

@HiltWorker
class BootWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val serviceScheduler: ServiceScheduler
) : CoroutineWorker(appContext, workerParams) {
    private val logSource = "BootWorker"

    override suspend fun doWork(): Result {
        // Only restart the Foreground Service if it was running before the system kill/reboot.
        if (locationRepository.isServiceRunning()) {
            AppLogger.i(logSource, "Service was running. Re-starting Foreground Service.")
            try {
                serviceScheduler.startService()
                AppLogger.i(logSource, "Foreground Service successfully restarted.")
                return Result.success()
            } catch (e: Exception) {
                AppLogger.e(logSource, "Failed to start Foreground Service after boot/kill.", e)
                // If restarting fails (e.g., permissions lost), we set the status to false.
                locationRepository.setServiceStatus(false)
                return Result.failure()
            }
        }
        AppLogger.i(logSource, "Service was not active. No action taken.")
        return Result.success()
    }
}