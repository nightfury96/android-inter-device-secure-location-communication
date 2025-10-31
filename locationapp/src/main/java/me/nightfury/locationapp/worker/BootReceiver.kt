package me.nightfury.locationapp.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.nightfury.locationdomain.usecases.ManageLocationWorkerUseCase
import me.nightfury.sharedlogger.AppLogger
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var manageLocationWorkerUseCase: ManageLocationWorkerUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AppLogger.d(
                "BootReceiver",
                "Device rebooted — check if we need to start scheduling BootWorker"
            )
            CoroutineScope(Dispatchers.Default).launch {
                if (manageLocationWorkerUseCase.isServiceRunning()) {
                    manageLocationWorkerUseCase.startPeriodicWork()
                    AppLogger.d("BootReceiver", "scheduling BootWorker started")
                }
            }
        }
    }
}