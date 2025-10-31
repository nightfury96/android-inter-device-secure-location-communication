package me.nightfury.locationdomain.usecases

import kotlinx.coroutines.flow.Flow
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedmodels.LocationRecord
import javax.inject.Inject

class LocationDataUseCase @Inject constructor(
    private val repo: LocationRepository
) {
    fun getAll(): Flow<List<LocationRecord>> {
        return repo.getLocationsFlow()
    }

    suspend fun clear() {
        return repo.clearLocations()
    }

    suspend fun isServiceRunning(): Boolean {
        return repo.isServiceRunning()
    }

}