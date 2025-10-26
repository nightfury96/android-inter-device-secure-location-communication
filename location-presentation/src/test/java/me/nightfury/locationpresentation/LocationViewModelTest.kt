package me.nightfury.locationpresentation

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.nightfury.locationdomain.usecases.LocationDataUseCase
import me.nightfury.locationdomain.usecases.ManageLocationWorkerUseCase
import me.nightfury.sharedlogger.NoOpLogger
import me.nightfury.sharedmodels.LocationRecord
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LocationViewModel
    private lateinit var locationDataUseCase: LocationDataUseCase
    private lateinit var manageLocationWorkerUseCase: ManageLocationWorkerUseCase

    private val locationFlow = MutableStateFlow<List<LocationRecord>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mocking dependencies
        locationDataUseCase = mockk(relaxed = true)
        manageLocationWorkerUseCase = mockk(relaxed = true)

        every { locationDataUseCase.getAll() } returns locationFlow
        coEvery { manageLocationWorkerUseCase.isServiceRunning() } returns false

        val fakeLogger = NoOpLogger()
        viewModel = LocationViewModel(locationDataUseCase, manageLocationWorkerUseCase, fakeLogger)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --------------------------------------------------------------------
    // TEST 1 — verify init() collects flow and updates UI state
    // --------------------------------------------------------------------
    @Test
    fun `uiState updates when new locations emitted`() = runTest {
        val mockLocations = listOf(
            LocationRecord(
                id = 666, latitude = 10.0, longitude = 20.0
            )
        )

        viewModel.uiState.test {
            // Default state first
            val initial = awaitItem()
            assertEquals(emptyList<LocationRecord>(), initial.locations)

            // Emit mock data after collector is active
            locationFlow.value = mockLocations
            runCurrent()

            // Collect the next emission
            val updated = awaitItem()
            assertEquals(mockLocations, updated.locations)
            assertEquals(false, updated.isServiceRunning)
            assertEquals("Service STOPPED", updated.statusMessage)
        }
    }

    // --------------------------------------------------------------------
    // TEST 2 — verify startLocationService calls use case and updates state
    // --------------------------------------------------------------------
    @Test
    fun `startLocationService starts service and updates state`() = runTest {
        coEvery { manageLocationWorkerUseCase.startPeriodicWork() } just runs

        viewModel.startLocationService()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.isServiceRunning)
        assertEquals("Service ACTIVE (1 min sampling)", state.statusMessage)
        coVerify { manageLocationWorkerUseCase.startPeriodicWork() }
    }

    // --------------------------------------------------------------------
    // TEST 3 — verify stopLocationService stops service and updates state
    // --------------------------------------------------------------------
    @Test
    fun `stopLocationService stops service and updates state`() = runTest {
        coEvery { manageLocationWorkerUseCase.stopPeriodicWork() } just runs

        viewModel.stopLocationService()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isServiceRunning)
        assertEquals("Service STOPPED", state.statusMessage)
        coVerify { manageLocationWorkerUseCase.stopPeriodicWork() }
    }

    // --------------------------------------------------------------------
    // TEST 4 — verify error handling in startLocationService
    // --------------------------------------------------------------------
    @Test
    fun `startLocationService sets error message when exception thrown`() = runTest {
        coEvery { manageLocationWorkerUseCase.startPeriodicWork() } throws RuntimeException("Boom")

        viewModel.startLocationService()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("ERROR: Start failed.", state.statusMessage)
    }
}