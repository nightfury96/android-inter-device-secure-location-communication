package me.nightfury.locationdata.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.local.LocationDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LocationDatabase {
        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_db"
        ).build()
    }

    @Provides
    fun provideLocationsDao(db: LocationDatabase): LocationDao = db.locationsDao()
}