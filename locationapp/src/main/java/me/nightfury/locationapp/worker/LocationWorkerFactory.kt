package me.nightfury.locationapp.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import me.nightfury.locationdomain.ServiceScheduler
import me.nightfury.locationdomain.repo.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationWorkerFactory @Inject constructor(
    private val locationRepository: LocationRepository,
    private val serviceScheduler: ServiceScheduler
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            BootWorker::class.java.name ->
                BootWorker(appContext, workerParameters, locationRepository, serviceScheduler)

            else -> null
        }
    }
}