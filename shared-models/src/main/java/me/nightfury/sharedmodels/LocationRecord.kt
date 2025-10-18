package me.nightfury.sharedmodels

data class LocationRecord(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float? = null
)
