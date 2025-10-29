package me.nightfury.locationdata.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocationEntity::class], version = 2, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationsDao(): LocationDao
}