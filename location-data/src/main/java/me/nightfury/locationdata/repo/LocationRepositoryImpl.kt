package me.nightfury.locationdata.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.local.LocationEntity
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedlogger.AppLogger
import me.nightfury.sharedmodels.LocationRecord

class LocationRepositoryImpl(private val dao: LocationDao) : LocationRepository {

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
}