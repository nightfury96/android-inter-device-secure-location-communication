package me.nightfury.internetapp.data.repo

import android.content.Context
import me.nightfury.internetapp.domain.repo.RemoteLocationRepository
import me.nightfury.sharedmodels.LocationRecord

class RemoteLocationRepositoryImpl(private val context: Context) : RemoteLocationRepository {
    override suspend fun startService(): Result<String> {
        context.contentResolver
        TODO("Not yet implemented")
    }

    override suspend fun stopService(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllLocations(): Result<List<LocationRecord>> {
        TODO("Not yet implemented")
    }
}