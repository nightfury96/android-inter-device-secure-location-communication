package me.nightfury.locationpresentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.nightfury.locationdomain.usecases.LocationDataUseCase
import me.nightfury.locationdomain.usecases.ManageLocationWorkerUseCase
import me.nightfury.sharedlogger.Logger
import me.nightfury.sharedmodels.LocationRecord
import javax.inject.Inject

data class LocationUiState(
    val locations: List<LocationRecord> = emptyList(),
    val isServiceRunning: Boolean = false,
    val statusMessage: String = "Service Idle"
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationDataUseCase: LocationDataUseCase,
    private val manageLocationWorkerUseCase: ManageLocationWorkerUseCase,
    private val logger: Logger
) : ViewModel() {
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState
    private val logSource = "LocationViewModel"

    init {
        locationDataUseCase.getAll()
            .onStart { emit(emptyList()) }
            .onEach { locations ->
                // Fetch the current service status
                val isRunning = manageLocationWorkerUseCase.isServiceRunning()

                // Update the StateFlow with all collected data points
                _uiState.value = LocationUiState(
                    locations = locations,
                    isServiceRunning = isRunning,
                    statusMessage = if (isRunning) "Service ACTIVE (1 min sampling)" else "Service STOPPED"
                )
                logger.d(
                    logSource,
                    "UI State updated. Locations: ${locations.size}, Running: $isRunning"
                )
            }
            .launchIn(viewModelScope)
    }

    fun startLocationService() {
        viewModelScope.launch {
            try {
                manageLocationWorkerUseCase.startPeriodicWork()
                updateServiceStatus(true)
                logger.i(logSource, "Start service command issued.")
            } catch (e: Exception) {
                logger.e(logSource, "Failed to start service.", e)
                _uiState.value = _uiState.value.copy(statusMessage = "ERROR: Start failed.")
            }
        }
    }

    fun stopLocationService() {
        viewModelScope.launch {
            try {
                manageLocationWorkerUseCase.stopPeriodicWork()
                updateServiceStatus(false)
                logger.i(logSource, "Stop service command issued.")
            } catch (e: Exception) {
                logger.e(logSource, "Failed to stop service.", e)
                _uiState.value = _uiState.value.copy(statusMessage = "ERROR: Stop failed.")
            }
        }
    }

    private fun updateServiceStatus(isRunning: Boolean) {
        _uiState.value = _uiState.value.copy(
            isServiceRunning = isRunning,
            statusMessage = if (isRunning) "Service ACTIVE (1 min sampling)" else "Service STOPPED"
        )
    }

    fun clearLocations() {
        viewModelScope.launch {
            locationDataUseCase.clear()
            logger.i(logSource, "Clear locations command.")
        }
    }
}