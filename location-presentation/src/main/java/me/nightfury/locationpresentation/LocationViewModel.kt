package me.nightfury.locationpresentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedmodels.LocationRecord
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {

    val locations: StateFlow<List<LocationRecord>> = repository.getLocationsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTestLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveLocation(LocationRecord(0, 220.2, 1000.0))
        }
    }
}