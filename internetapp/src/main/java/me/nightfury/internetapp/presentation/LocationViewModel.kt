package me.nightfury.internetapp.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.nightfury.internetapp.domain.repo.RemoteLocationRepository

/**
 * ViewModel responsible for processing LocationViewIntents and updating the LocationViewState.
 * It communicates with the RemoteLocationRepository to interact with the Location App.
 */
class LocationViewModel(
    private val remoteLocationRepository: RemoteLocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LocationViewState())
    val state: StateFlow<LocationViewState> = _state

    /**
     * Processes incoming user actions (Intents).
     */
    fun processIntent(intent: LocationViewIntent) {
        viewModelScope.launch {
            when (intent) {
                LocationViewIntent.StartService -> executeCommand { remoteLocationRepository.startService() }
                LocationViewIntent.StopService -> executeCommand { remoteLocationRepository.stopService() }
                LocationViewIntent.RetrieveLocationHistory -> retrieveLocationHistory()
                LocationViewIntent.RetrieveLatestLocation -> retrieveLatestLocation()
                LocationViewIntent.ClearError -> _state.update { it.copy(error = null) }
            }
        }
    }

    private suspend fun executeCommand(command: suspend () -> Result<String>) {
        _state.update { it.copy(isLoading = true, error = null, lastCommandResponse = null) }
        val result = command()

        result.onSuccess { status ->
            // Update service status based on command success
            val newStatus = if (status.contains("started", ignoreCase = true)) {
                "Service Status: ACTIVE"
            } else {
                status
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    serviceStatus = newStatus,
                    lastCommandResponse = status
                )
            }
        }.onFailure { exception ->
            exception.printStackTrace()
            _state.update {
                it.copy(
                    isLoading = false,
                    error = exception.message ?: "Unknown command error"
                )
            }
        }
    }

    private suspend fun retrieveLocationHistory() {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = remoteLocationRepository.getAllLocations()

        result.onSuccess { history ->
            _state.update {
                it.copy(
                    isLoading = false,
                    locationHistory = history
                )
            }
        }.onFailure { exception ->
            _state.update {
                it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to retrieve location history"
                )
            }
        }
    }

    private suspend fun retrieveLatestLocation() {
        _state.update { it.copy(isLoading = true, error = null) }
        val result = remoteLocationRepository.getLatestLocation()

        result.onSuccess { latest ->
            _state.update {
                it.copy(
                    isLoading = false,
                    latestLocation = latest
                )
            }
        }.onFailure { exception ->
            _state.update {
                it.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to retrieve latest location"
                )
            }
        }
    }
}