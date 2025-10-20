package me.nightfury.locationpresentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedmodels.LocationRecord

class FakeLocationRepository : LocationRepository {
    // Use a MutableStateFlow to simulate the real database flow
    private val locationFlow = MutableStateFlow<List<LocationRecord>>(emptyList())

    // Expose the Flow as required by the interface
    override fun getLocationsFlow(): Flow<List<LocationRecord>> = locationFlow.asStateFlow()

    // Function to set the fake data list for testing
    fun setLocations(locations: List<LocationRecord>) {
        locationFlow.value = locations
    }

    override suspend fun saveLocation(location: LocationRecord) {
        val current = locationFlow.value.toMutableList()
        current.add(location)
        locationFlow.value = current
    }
}