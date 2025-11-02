package me.nightfury.locationapp.service

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import me.nightfury.locationdata.di.RepositoryModule
import me.nightfury.locationdomain.repo.LocationRepository
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object FakeRepositoryModule {

    @Provides
    @Singleton
    fun provideFakeRepository(): LocationRepository {
        return FakeLocationRepository()
    }
}