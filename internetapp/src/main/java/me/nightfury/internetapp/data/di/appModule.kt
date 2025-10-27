package me.nightfury.internetapp.data.di

import me.nightfury.internetapp.data.repo.RemoteLocationRepositoryImpl
import me.nightfury.internetapp.domain.repo.RemoteLocationRepository
import me.nightfury.internetapp.presentation.LocationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module definitions for the Internet App.
 */
val appModule = module {

    // Singletons for Data Layer (Repository)
    single<RemoteLocationRepository> {
        RemoteLocationRepositoryImpl(context = get())
    }

    // ViewModel for Presentation Layer (MVI)
    viewModel {
        LocationViewModel(remoteLocationRepository = get())
    }
}