package me.nightfury.locationdomain.repo

import kotlinx.coroutines.flow.Flow

interface SecureStorage {
    suspend fun setServiceStatus(isRunning: Boolean)
    fun isServiceRunning(): Flow<Boolean>
    suspend fun getOrCreateDbPassphrase(): ByteArray
}