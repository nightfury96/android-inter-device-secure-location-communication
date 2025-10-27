package me.nightfury.internetapp.presentation

import me.nightfury.sharedmodels.LocationRecord

/**
 * Represents the current state of the Location View.
 * This is immutable and dictates what the UI displays.
 */
data class LocationViewState(
    val serviceStatus: String = "Service Status: Unknown",
    val locationHistory: List<LocationRecord> = emptyList(),
    val isLoading: Boolean = false,
    val lastCommandResponse: String? = null,
    val error: String? = null
) {
    val isServiceActive: Boolean
        get() = serviceStatus.contains("ACTIVE", ignoreCase = true)
}