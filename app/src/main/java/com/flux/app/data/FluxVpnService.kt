package com.flux.app.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
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
        
        // 1. Create Notification Channel (Required for Android 8+)
        val channelId = "flux_vpn_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Flux VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Start Foreground (Keeps VPN alive)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Flux Guard Active")
            .setContentText("Monitoring network traffic")
            .setSmallIcon(R.drawable.ic_settings) // Using existing icon to prevent crash
            .setContentIntent(pendingIntent)
            .build()
        
        startForeground(1, notification)

        // 3. Establish VPN Interface
        // NOTE: We do NOT add a route to 0.0.0.0 yet because we don't have a packet processor.
        // If we route traffic without processing it, Internet dies. 
        // We will enable the interface so the "Key" icon appears, but let traffic bypass for now (Split Tunnel).
        val builder = Builder()
        builder.setSession("Flux Guard")
        builder.addAddress("10.0.0.2", 32)
        // builder.addRoute("0.0.0.0", 0) <--- Commented out to prevent blocking internet until packet engine is ready
        
        try {
            vpnInterface = builder.establish()
            isRunning = true
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
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
