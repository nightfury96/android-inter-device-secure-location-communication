package me.nightfury.locationapp.service

import kotlinx.coroutines.flow.flowOf
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedmodels.LocationRecord

class FakeLocationRepository : LocationRepository {
    private var isRunning = false

    override suspend fun setServiceStatus(isRunning: Boolean) {
        this.isRunning = isRunning
    }

    override suspend fun isServiceRunning(): Boolean = isRunning

    override fun getLocationsFlow() = flowOf(emptyList<LocationRecord>())
    override suspend fun saveLocation(location: LocationRecord) {}
    override suspend fun clearLocations() {}
}