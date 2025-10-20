package me.nightfury.locationapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.nightfury.locationapp.databinding.ActivityMainBinding
import me.nightfury.locationpresentation.LocationViewModel
import me.nightfury.sharedlogger.AppLogger

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationViewModel.locations.collectLatest { locations ->
                    AppLogger.d("MainActivity", "Received ${locations.size} locations")
                    binding.receivedLocationTV.text = locations.joinToString("\n") { loc ->
                        "ID: ${loc.id}, Lat: ${loc.latitude}, Lon: ${loc.longitude}, Time: ${loc.timestamp}"
                    }
                }
            }
        }

    }
}