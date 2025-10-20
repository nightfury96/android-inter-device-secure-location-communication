package me.nightfury.locationpresentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.nightfury.sharedmodels.LocationRecord
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    // Rule to handle the Main Dispatcher replacement
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Rule to swap the main thread executor with one that executes tasks synchronously
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // The fake repository instance
    private val fakeRepository = FakeLocationRepository()

    // The ViewModel instance under test
    private lateinit var viewModel: LocationViewModel

    @Test
    fun locations_isCorrectlyInitializedWithDataFromRepository() = runTest {
        // fake data
        val fakeLocations = listOf(
            LocationRecord(id = 1, latitude = 40.71, longitude = -74.01, timestamp = 1L),
            LocationRecord(id = 2, latitude = 34.05, longitude = -118.24, timestamp = 2L)
        )
        // add fake data to the repository
        fakeRepository.setLocations(fakeLocations)

        viewModel = LocationViewModel(fakeRepository)

        // collect the StateFlow's current value (or use .first() for the initial value)
        val result = viewModel.locations.first()

        assertEquals(2, result.size)
        assertEquals(fakeLocations, result)
    }

    @Test
    fun locations_updatesWhenRepositoryDataChanges() = runTest {
        // start with an empty list
        fakeRepository.setLocations(emptyList())
        viewModel = LocationViewModel(fakeRepository)

        // simulate a change in the underlying repository data
        val newLocation = LocationRecord(id = 3, latitude = 51.5, longitude = 0.12, timestamp = 3L)
        fakeRepository.saveLocation(newLocation)

        // verify the ViewModel's StateFlow has been updated
        val result = viewModel.locations.first()

        assertEquals(1, result.size)
        assertEquals(newLocation, result.first())
    }
}