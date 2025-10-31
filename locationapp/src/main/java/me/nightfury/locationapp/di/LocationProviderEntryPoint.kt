package me.nightfury.locationapp.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.nightfury.locationdomain.usecases.LocationDataUseCase
import me.nightfury.locationdomain.usecases.ManageLocationWorkerUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocationProviderEntryPoint {
    fun locationDataUseCase(): LocationDataUseCase
    fun manageLocationWorkerUseCase(): ManageLocationWorkerUseCase
}