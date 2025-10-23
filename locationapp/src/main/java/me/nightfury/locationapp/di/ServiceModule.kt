package me.nightfury.locationapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nightfury.locationapp.service.ServiceSchedulerImpl
import me.nightfury.locationdomain.ServiceScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideServiceScheduler(impl: ServiceSchedulerImpl): ServiceScheduler {
        return impl
    }
}