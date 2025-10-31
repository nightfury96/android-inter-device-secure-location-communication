package me.nightfury.locationapp.provider

import android.database.MatrixCursor
import me.nightfury.sharedmodels.LocationRecord

class LocationRecordCursor(list: List<LocationRecord>) : MatrixCursor(
    arrayOf("latitude", "longitude", "timestamp", "id")
) {
    init {
        list.forEach {
            addRow(arrayOf(it.latitude, it.longitude, it.timestamp, it.id))
        }
    }
}