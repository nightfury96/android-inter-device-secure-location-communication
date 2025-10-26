package me.nightfury.locationdata.repo

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.local.LocationEntity
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedlogger.AppLogger
import me.nightfury.sharedmodels.LocationRecord

class LocationRepositoryImpl(
    private val dao: LocationDao,
    private val datastore: SharedPreferences
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


    companion object {
        const val SERVICE_STATUS = "is_service_running"
    }

    override suspend fun setServiceStatus(isRunning: Boolean) {
        datastore.edit {
            putBoolean(SERVICE_STATUS, isRunning)
        }
    }

    override suspend fun isServiceRunning(): Boolean {
        return datastore.getBoolean(SERVICE_STATUS, false)
    }
}