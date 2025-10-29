package me.nightfury.locationapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.nightfury.locationapp.R
import me.nightfury.locationdata.local.LocationEntity
import me.nightfury.locationdomain.repo.LocationRepository
import me.nightfury.locationdomain.usecases.ManageLocationWorkerUseCase
import me.nightfury.sharedlogger.AppLogger
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : LifecycleService() {

    companion object {
        private const val NOTIF_CHANNEL_ID = "location_foreground_channel"
        private const val CHANNEL_NAME = "Location Tracking"
        private const val NOTIF_ID = 1001
        const val ACTION_START = "me.nightfury.locationapp.action.START"
        const val ACTION_STOP = "me.nightfury.locationapp.action.STOP"

        fun start(context: Context) {
            val serviceIntent =
                Intent(context, LocationForegroundService::class.java).apply {
                    action = ACTION_START
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }

        fun stop(context: Context) {
            val serviceIntent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(serviceIntent)
        }
    }

    @Inject
    lateinit var manageLocationWorkerUseCase: ManageLocationWorkerUseCase

    @Inject
    lateinit var locationRepository: LocationRepository
    private val logSource = "LFGS_Lifecycle"

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    // Location request parameters for 1-minute interval
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L // Guaranteed update interval: 60 seconds (1 minute)
    ).apply {
        setMinUpdateIntervalMillis(2000L)
    }.build()

    // In real implementation, obtain LocationProvider and request updates
    // For Step 1, we simulate location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation ?: return

            val record = LocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis()
            )

            // Use lifecycleScope for structured concurrency tied to the Service lifecycle
            lifecycleScope.launch(Dispatchers.IO) {
                locationRepository.saveLocation(record.toLocationRecord())
                AppLogger.i(
                    logSource,
                    "Location updated (1 min): (${record.latitude}, ${record.longitude})"
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = intent?.action
        AppLogger.i(logSource, "Service received command: $action")

        when (action) {
            ACTION_START -> startLocationUpdates()
            ACTION_STOP -> stopSelf()
            null, "" -> handleOsRestart()
            else -> {
                AppLogger.w(logSource, "Unknown service action received: $action")
            }
        }

        // START_STICKY ensures the service restarts if it's killed by the system
        return START_STICKY
    }

    private fun handleOsRestart() {
        lifecycleScope.launch {
            if (manageLocationWorkerUseCase.isServiceRunning()) {
                AppLogger.i(
                    logSource,
                    "OS restart detected (action=null). User intent is ON. Resuming service and re-scheduling recovery worker."
                )
                manageLocationWorkerUseCase.startPeriodicWork()
            } else {
                AppLogger.i(
                    logSource,
                    "OS restart detected (action=null). User intent is OFF. Stopping service."
                )
                stopSelf()
            }
        }
    }

    private fun startLocationUpdates() {
        // Request location updates
        try {
            startForeground(NOTIF_ID, createNotification())

            lifecycleScope.launch(Dispatchers.IO) {
                locationRepository.setServiceStatus(true)
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
            AppLogger.i(logSource, "Location updates requested every 1 minute.")
        } catch (e: SecurityException) {
            AppLogger.e(logSource, "Location permission missing or denied.", e)
            // Stop service if permissions are missing
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.getNotificationChannel(NOTIF_CHANNEL_ID) ?: run {
                val channel = NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                )
                manager.createNotificationChannel(channel)
            }
        }

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Location Service Running")
            .setContentText("Collecting location every minute.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        lifecycleScope.launch(Dispatchers.IO) {
            locationRepository.setServiceStatus(false)
            AppLogger.i(logSource, "Location service ServiceStatus set to false")
        }
        AppLogger.i(logSource, "Location service onDestroy called")
    }


    override fun onBind(intent: Intent) = super.onBind(intent)
}