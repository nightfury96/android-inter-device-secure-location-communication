package me.nightfury.locationdata.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import me.nightfury.locationdata.local.LocationDao
import me.nightfury.locationdata.local.LocationDatabase
import me.nightfury.locationdomain.repo.SecureStorage
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        sharePref: SecureStorage
    ): LocationDatabase {
        val dbKey = runBlocking { sharePref.getOrCreateDbPassphrase() }
        val factory = SupportOpenHelperFactory(dbKey)

        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            "location_db"
        ).openHelperFactory(factory)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideLocationsDao(db: LocationDatabase): LocationDao = db.locationsDao()
}