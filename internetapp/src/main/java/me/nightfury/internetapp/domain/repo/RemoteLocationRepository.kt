package me.nightfury.internetapp.domain.repo

import me.nightfury.sharedmodels.LocationRecord

interface RemoteLocationRepository {

    /**
     * Sends a command to the Location App to start its foreground service.
     */
    suspend fun startService(): Result<String>

    /**
     * Sends a command to the Location App to stop its foreground service.
     */
    suspend fun stopService(): Result<String>

    /**
     * Retrieves the list of all stored location records from the Location App.
     */
    suspend fun getAllLocations(): Result<List<LocationRecord>>
}