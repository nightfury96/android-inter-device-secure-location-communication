package me.nightfury.internetapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import me.nightfury.internetapp.databinding.ActivityMainBinding
import me.nightfury.internetapp.presentation.LocationViewIntent
import me.nightfury.internetapp.presentation.LocationViewModel
import me.nightfury.internetapp.presentation.LocationViewState
import me.nightfury.internetapp.presentation.adapter.LocationHistoryAdapter
import me.nightfury.sharedlogger.AppLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val locationAdapter = LocationHistoryAdapter()

    private val viewModel: LocationViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        AppLogger.d("MainActivity", "InternetApp: this is logger test onCreate called")
        setupRecyclerView()
        setupListeners()
        observeViewState()
    }

    private fun observeViewState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    AppLogger.d("InternetAppUIState", "New State received: $state")
                    updateUI(state)
                    handleErrors(state)
                }
            }
        }
    }

    private val dateFormat = SimpleDateFormat("HH:mm:ss dd/MMM", Locale.getDefault())

    @SuppressLint("SetTextI18n")
    private fun updateUI(state: LocationViewState) {
        binding.statusTV.apply {
            text = state.serviceStatus
            val statusColor = if (state.isServiceActive) {
                ContextCompat.getColor(this@MainActivity, R.color.black)
            } else {
                ContextCompat.getColor(this@MainActivity, R.color.gray)
            }
            setTextColor(statusColor)
        }

        binding.progressPB.isVisible = state.isLoading

        binding.startServiceBT.isEnabled = !state.isLoading && !state.isServiceActive
        binding.stopServiceBT.isEnabled = !state.isLoading && state.isServiceActive
        binding.getHistoryBT.isEnabled = !state.isLoading


        locationAdapter.submitList(state.locationHistory)
        binding.historyTitleTV.text = "Location History (${state.locationHistory.size}):"

        state.latestLocation?.let { latestLocation ->
            binding.latestLocationTitleTV.text = "Latest Location: ${
                String.format(
                    Locale.getDefault(),
                    "Lat: %.4f, Lon: %.4f",
                    latestLocation.latitude,
                    latestLocation.longitude
                )
            } ,Date: ${dateFormat.format(Date(latestLocation.timestamp))}"
        }

        state.lastCommandResponse?.let { response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleErrors(state: LocationViewState) {
        state.error?.let { errorMessage ->
            AppLogger.e("InternetAppError", "UI Error: $errorMessage")
            Toast.makeText(this, "ERROR: $errorMessage", Toast.LENGTH_LONG).show()
            viewModel.processIntent(LocationViewIntent.ClearError)
        }
    }

    private fun setupListeners() {
        binding.startServiceBT.setOnClickListener {
            viewModel.processIntent(LocationViewIntent.StartService)
            AppLogger.d("InternetAppUI", "Intent: StartService sent.")
        }
        binding.stopServiceBT.setOnClickListener {
            viewModel.processIntent(LocationViewIntent.StopService)
            AppLogger.d("InternetAppUI", "Intent: StopService sent.")
        }
        binding.getHistoryBT.setOnClickListener {
            viewModel.processIntent(LocationViewIntent.RetrieveLocationHistory)
            AppLogger.d("InternetAppUI", "Intent: RetrieveLocationHistory sent.")
        }
        binding.getLatestBT.setOnClickListener {
            viewModel.processIntent(LocationViewIntent.RetrieveLatestLocation)
            AppLogger.d("InternetAppUI", "Intent: RetrieveLatestLocation sent.")
        }
    }

    private fun setupRecyclerView() {
        binding.locationHistoryRV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = locationAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }
}