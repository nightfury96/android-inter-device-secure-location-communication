package me.nightfury.locationdata.di

import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.repo.LocationRepositoryImpl
import me.nightfury.locationdomain.repo.LocationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLocationRepository(
        dao: LocationDao,
        sharePref: SharedPreferences
    ): LocationRepository {
        return LocationRepositoryImpl(dao, sharePref)
    }
}