package me.nightfury.locationdomain.usecases

import kotlinx.coroutines.flow.Flow
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.sharedmodels.LocationRecord
import javax.inject.Inject

class GetAllLocationsUseCase @Inject constructor(
    private val repo: LocationRepository
) {
    fun invoke(): Flow<List<LocationRecord>> {
        return repo.getLocationsFlow()
    }
}