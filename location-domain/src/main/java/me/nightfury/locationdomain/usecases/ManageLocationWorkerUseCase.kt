package me.nightfury.locationdomain.usecases

import me.nightfury.locationdomain.ServiceScheduler
import me.nightfury.locationdomain.repo.LocationRepository
import javax.inject.Inject


/**
 * Manages the Foreground Service lifecycle and ensures post-boot recovery via WorkManager.
 */
class ManageLocationWorkerUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val serviceScheduler: ServiceScheduler,
) {

    suspend fun startPeriodicWork() {
        serviceScheduler.startService()
    }

    suspend fun stopPeriodicWork() {
        serviceScheduler.stopService()
    }

    suspend fun isServiceRunning(): Boolean {
        return locationRepository.isServiceRunning()
    }
}