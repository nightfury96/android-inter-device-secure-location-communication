package me.nightfury.locationdomain.repo

import kotlinx.coroutines.flow.Flow
import me.nightfury.sharedmodels.LocationRecord

interface LocationRepository {
    fun getLocationsFlow(): Flow<List<LocationRecord>>
    suspend fun saveLocation(location: LocationRecord)
}