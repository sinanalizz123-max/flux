package com.flux.app.data

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.flux.app.MainActivity
import com.flux.app.R

class FluxVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
        } else {
            startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        
        val builder = Builder()
        
        // 1. Configure the virtual network interface
        // We set up a local IP so traffic flows through us
        builder.setSession("Flux Guard")
        builder.addAddress("10.0.0.2", 32)
        builder.addRoute("0.0.0.0", 0) // Route ALL traffic through Flux
        
        // 2. Add the notification icon intent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        builder.setConfigureIntent(pendingIntent)

        // 3. Establish connection
        try {
            vpnInterface = builder.establish()
            isRunning = true
            // In a real AdBlocker, we would start a thread here to read/filter packets
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            isRunning = false
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }
}
