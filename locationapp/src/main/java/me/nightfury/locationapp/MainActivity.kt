package me.nightfury.locationapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
    private val requiredForegroundPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    @RequiresApi(Build.VERSION_CODES.Q)
    private val backgroundPermission =
        Manifest.permission.ACCESS_BACKGROUND_LOCATION

    private val requestForegroundPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.values.all { it }
            if (granted) {
                AppLogger.i(logSource, "Foreground location granted(location + notification).")
                checkAndRequestBackgroundPermission()
            } else {
                Toast.makeText(
                    this,
                    "Location & Notification permissions are required",
                    Toast.LENGTH_LONG
                ).show()
                openApplicationSettings()
            }
        }

    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                AppLogger.i(logSource, "Background location granted (Allow all the time).")
            } else {
                Toast.makeText(
                    this,
                    "Please enable 'Allow all the time' for background location.",
                    Toast.LENGTH_LONG
                ).show()
                openApplicationSettings()
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

    fun checkAndRequestPermissions(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            else true

        val notificationGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            else true
        val allForegroundGranted = fineGranted && coarseGranted && notificationGranted

        return if (!allForegroundGranted) {
            requestForegroundPermissionsLauncher.launch(requiredForegroundPermissions)
            false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundGranted) {
            checkAndRequestBackgroundPermission()
            false
        } else {
            true
        }
    }

    private fun checkAndRequestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                backgroundPermission
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                // For Android 11+ (API 30), background permission often requires going to Settings
                requestBackgroundPermissionLauncher.launch(backgroundPermission)
            }
        }
    }

    private fun openApplicationSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
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