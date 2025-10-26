package me.nightfury.locationapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.nightfury.locationapp.databinding.ActivityMainBinding
import me.nightfury.locationpresentation.LocationViewModel
import me.nightfury.sharedlogger.AppLogger

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val logSource = "MainActivity"
    private val locationViewModel: LocationViewModel by viewModels()
    private val locationAdapter = LocationAdapter()
    private lateinit var binding: ActivityMainBinding

    // Permission setup for both Foreground Location and Background Location (if required)
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mutableSetOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        mutableSetOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }.apply {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            AppLogger.i(logSource, "Location permissions granted.")
        } else {
            Toast.makeText(
                this,
                "Location permission is required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkAndRequestPermissions()
        setupRecyclerView()
        setupListeners()
        collectUiState()

        AppLogger.i(logSource, "MainActivity created.")

    }

    private fun collectUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.uiState.collect { state ->
                    locationAdapter.submitList(state.locations)

                    binding.receivedLocationTV.text = state.statusMessage

                    binding.startBT.isEnabled = !state.isServiceRunning
                    binding.stopBT.isEnabled = state.isServiceRunning
                }
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissionsLauncher.launch(requiredPermissions.toTypedArray())
        }
        return allGranted
    }

    private fun setupRecyclerView() {
        binding.locationListRV.apply {
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

    private fun setupListeners() {
        binding.startBT.setOnClickListener {
            if (checkAndRequestPermissions()) {
                locationViewModel.startLocationService()
            }
        }
        binding.stopBT.setOnClickListener {
            locationViewModel.stopLocationService()
        }
        binding.clearBT.setOnClickListener {
            locationViewModel.clearLocations()
        }
    }
}