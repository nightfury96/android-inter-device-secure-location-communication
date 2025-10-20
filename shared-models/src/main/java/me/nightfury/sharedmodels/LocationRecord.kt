package me.nightfury.sharedmodels

data class LocationRecord(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
