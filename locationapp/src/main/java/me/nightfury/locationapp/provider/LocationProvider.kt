package me.nightfury.locationapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.nightfury.locationapp.di.LocationProviderEntryPoint

class LocationProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "me.nightfury.locationapp.provider"
        private const val PATH_LOCATIONS = "locations"
        private const val PATH_LATEST = "latest"
        private const val CODE_LOCATIONS = 1
        private const val CODE_LATEST = 2

//        val CONTENT_URI: Uri = "content://$AUTHORITY/$PATH_LOCATIONS".toUri()

        private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_LOCATIONS, CODE_LOCATIONS)
            addURI(AUTHORITY, PATH_LATEST, CODE_LATEST)
        }
    }

    // Inject manually via Hilt EntryPoint
    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication(
            context!!.applicationContext,
            LocationProviderEntryPoint::class.java
        )
    }


    private val locationDataUseCase get() = entryPoint.locationDataUseCase()
    private val manageLocationWorkerUseCase get() = entryPoint.manageLocationWorkerUseCase()

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (matcher.match(uri)) {
            CODE_LOCATIONS -> {
                // Get the current list from Flow (blocking only here)
                val list = runBlocking { locationDataUseCase.getAll().first() }
                LocationRecordCursor(list)
            }

            CODE_LATEST -> {
                // Get the current list from Flow (blocking only here)
                val list = runBlocking { locationDataUseCase.getAll().first() }
                LocationRecordCursor(listOf(list.first()))
            }

            else -> null
        }
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val result = Bundle()
        when (method) {
            "startService" -> {
                runBlocking { manageLocationWorkerUseCase.startPeriodicWork() }
                result.putString("result", "Service started")
            }

            "stopService" -> {
                runBlocking { manageLocationWorkerUseCase.stopPeriodicWork() }
                result.putString("result", "Service stopped")
            }
        }
        return result
    }

    // Other required but unused overrides
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}