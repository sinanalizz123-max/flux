package com.flux.app.ui

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flux.app.R
import com.flux.app.data.NetworkRepo
import com.flux.app.data.SystemRepo
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var netRepo: NetworkRepo
    private lateinit var sysRepo: SystemRepo
    private val handler = Handler(Looper.getMainLooper())
    
    // 1. The "Blast" Launcher: Asks for all runtime permissions at once
    private val multiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // After user decides, refresh the UI to show Data or "Permission Needed"
        updateUI()
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isAdded && context != null) {
                updateUI()
                handler.postDelayed(this, 2000)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        netRepo = NetworkRepo(requireContext())
        sysRepo = SystemRepo(requireContext())

        setupClicks(view)
        
        // ASK FOR EVERYTHING ON STARTUP
        checkAndRequestAllPermissions()
        
        // Start the UI loop
        handler.post(refreshRunnable)
    }

    private fun checkAndRequestAllPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        // Android 13+ needs Notification permission
        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Filter out ones we already have
        val needed = perms.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (needed.isNotEmpty()) {
            multiPermissionLauncher.launch(needed)
        }
    }

    private fun updateUI() {
        val view = view ?: return
        
        // --- 1. NETWORK CARD LOGIC ---
        val hasLoc = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasPhone = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        if (hasLoc && hasPhone) {
            // WE HAVE PERMISSION: Show Real Data
            val stats = netRepo.getNetworkStats()
            setText(view, R.id.sim1_rsrp, stats.rsrp, R.color.leica_white)
            setText(view, R.id.sim1_rsrq, stats.rsrq, R.color.leica_white)
            setText(view, R.id.sim1_sinr, stats.sinr, R.color.leica_white)
            setText(view, R.id.sim1_pci, stats.pci, R.color.leica_white)
            setText(view, R.id.sim1_band, stats.band, R.color.classic_red)
        } else {
            // NO PERMISSION: Show Warning
            val warn = "Tap to Allow"
            setText(view, R.id.sim1_rsrp, "No Perm", R.color.classic_red)
            setText(view, R.id.sim1_rsrq, warn, R.color.classic_red)
            setText(view, R.id.sim1_sinr, "Required", R.color.classic_red)
            setText(view, R.id.sim1_pci, "!", R.color.classic_red)
            setText(view, R.id.sim1_band, "Permission Needed", R.color.classic_red)
        }

        // --- 2. DATA USAGE CARD LOGIC ---
        // Usage Stats is special, it's not a popup permission.
        val dataCardTitle = view.findViewById<TextView>(R.id.cardData).findViewById<TextView>(android.R.id.message) // Finding text inside card is tricky without ID, simplified below:
        // Note: In layout we didn't give ID to the text inside cardData, so we check status via logic
        
        // We can't easily change the text inside the include layout without IDs, 
        // but we can show a Toast if they click it, or we can trust the DataFragment to handle it.
        // For the "Dashboard View", we will leave the text static "Data Usage" 
        // but if they click it without permission, we handle it in setupClicks.
        
        // --- 3. BATTERY ---
        val batLevel = sysRepo.getBatteryLevel()
        view.findViewById<TextView>(R.id.txtBattery)?.text = "${batLevel}%"
    }

    private fun setText(view: View, id: Int, text: String, colorRes: Int) {
        val tv = view.findViewById<TextView>(id)
        tv?.text = text
        tv?.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }
    
    private fun setupClicks(view: View) {
        // Settings Button
        view.findViewById<ImageView>(R.id.btnSettings)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        // NETWORK CARD CLICK (Fix Permissions)
        // If they click the network card and permissions are missing, ask again.
        val cardNet = view.findViewById<View>(R.id.sim1_rsrp)?.parent?.parent?.parent as? View
        // finding the card view via hierarchy or if we gave it an ID. 
        // We didn't give the big card an ID in XML. Let's rely on the user clicking the "Settings" for retrying or re-launch.
        // Actually, let's just use the specific Data/Battery cards which have IDs.

        // DATA CARD CLICK
        view.findViewById<View>(R.id.cardData)?.setOnClickListener {
            if (hasUsageStatsPermission()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, DataUsageFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Permission Needed: Redirecting to Usage Settings...", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }

        // BATTERY CARD CLICK
        view.findViewById<View>(R.id.cardBattery)?.setOnClickListener {
             parentFragmentManager.beginTransaction()
                .replace(R.id.container, BatteryUsageFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
            android.os.Process.myUid(), requireContext().packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshRunnable)
    }
}
