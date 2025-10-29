package me.nightfury.locationdata.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.local.LocationEntity
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.locationdomain.repo.SecureStorage
import me.nightfury.sharedlogger.AppLogger
import me.nightfury.sharedmodels.LocationRecord

class LocationRepositoryImpl(
    private val dao: LocationDao,
    private val datastore: SecureStorage
) : LocationRepository {

    override fun getLocationsFlow(): Flow<List<LocationRecord>> {
        return dao.getLocations()
            .map { locationEntities -> locationEntities.map { it.toLocationRecord() } }
    }

    override suspend fun saveLocation(location: LocationRecord) {
        dao.insertLocation(
            LocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = location.timestamp
            )
        )
        AppLogger.i("LocationRepositoryImpl", "Location saved successfully")
    }

    override suspend fun clearLocations() {
        dao.clearAll()
    }

    override suspend fun setServiceStatus(isRunning: Boolean) {
        datastore.setServiceStatus(isRunning)
    }

    override suspend fun isServiceRunning(): Boolean {
        return datastore.isServiceRunning().first()
    }
}