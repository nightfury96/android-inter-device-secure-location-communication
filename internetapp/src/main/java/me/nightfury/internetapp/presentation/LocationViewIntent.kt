package me.nightfury.internetapp.presentation

/**
 * Sealed class representing all possible user actions or external triggers
 * that the ViewModel needs to process.
 */
sealed class LocationViewIntent {
    data object StartService : LocationViewIntent()
    data object StopService : LocationViewIntent()
    data object RetrieveLocationHistory : LocationViewIntent()
    data object ClearError : LocationViewIntent()
}