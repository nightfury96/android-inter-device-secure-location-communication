package me.nightfury.locationdomain

interface ServiceScheduler {
    suspend fun startService()
    suspend fun stopService()
}