package me.nightfury.internetapp.data.repo

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import me.nightfury.internetapp.domain.repo.RemoteLocationRepository
import me.nightfury.sharedmodels.LocationRecord

class RemoteLocationRepositoryImpl(private val context: Context) : RemoteLocationRepository {
    private val AUTHORITY = "me.nightfury.locationapp.provider"
    private val BASE_URI = "content://$AUTHORITY".toUri()
    private val QUERY_ALL_URI = Uri.withAppendedPath(BASE_URI, "locations")
    private val QUERY_LATEST_URI = Uri.withAppendedPath(BASE_URI, "latest")

    override suspend fun startService(): Result<String> {
        return runCatching {
            val resultBundle = context.contentResolver.call(
                BASE_URI,
                "startService",
                null,
                null
            )
            resultBundle?.getString("result") ?: "No result"
        }
    }

    override suspend fun stopService(): Result<String> {
        return runCatching {
            val resultBundle = context.contentResolver.call(
                BASE_URI,
                "stopService",
                null,
                null
            )
            resultBundle?.getString("result") ?: "No result"
        }
    }

    override suspend fun getAllLocations(): Result<List<LocationRecord>> {
        return runCatching {
            val cursor = context.contentResolver.query(QUERY_ALL_URI, null, null, null, null)
            val result = mutableListOf<LocationRecord>()
            cursor?.use {
                val latIdx = it.getColumnIndex("latitude")
                val lonIdx = it.getColumnIndex("longitude")
                val timeIdx = it.getColumnIndex("timestamp")
                val idIdx = it.getColumnIndex("id")
                while (it.moveToNext()) {
                    result.add(
                        LocationRecord(
                            latitude = it.getDouble(latIdx),
                            longitude = it.getDouble(lonIdx),
                            timestamp = it.getLong(timeIdx),
                            id = it.getLong(idIdx)
                        )
                    )
                }
            }
            result
        }
    }

    override suspend fun getLatestLocation(): Result<LocationRecord?> {
        return runCatching {
            val cursor = context.contentResolver.query(QUERY_LATEST_URI, null, null, null, null)
            val result = mutableListOf<LocationRecord>()
            cursor?.use {
                val latIdx = it.getColumnIndex("latitude")
                val lonIdx = it.getColumnIndex("longitude")
                val timeIdx = it.getColumnIndex("timestamp")
                val idIdx = it.getColumnIndex("id")
                while (it.moveToNext()) {
                    result.add(
                        LocationRecord(
                            latitude = it.getDouble(latIdx),
                            longitude = it.getDouble(lonIdx),
                            timestamp = it.getLong(timeIdx),
                            id = it.getLong(idIdx)
                        )
                    )
                }
            }
            result.firstOrNull()
        }
    }
}