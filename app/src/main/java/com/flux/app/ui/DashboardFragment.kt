package com.flux.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.flux.app.R
import com.flux.app.data.NetworkRepo
import com.flux.app.data.SystemRepo

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var netRepo: NetworkRepo
    private lateinit var sysRepo: SystemRepo
    private val handler = Handler(Looper.getMainLooper())
    
    // Refresh data every 2 seconds
    private val refreshRunnable = object : Runnable {
        override fun run() {
            updateUI()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Backend
        netRepo = NetworkRepo(requireContext())
        sysRepo = SystemRepo(requireContext())

        // Setup Navigation
        setupClicks(view)

        // Check Permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            startMonitoring()
        } else {
            // Ask for permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startMonitoring()
    }

    private fun startMonitoring() {
        handler.post(refreshRunnable)
    }

    private fun updateUI() {
        val view = view ?: return
        
        // 1. Get Real Network Data
        val stats = netRepo.getNetworkStats()
        
        view.findViewById<TextView>(R.id.sim1_rsrp).text = stats.rsrp
        view.findViewById<TextView>(R.id.sim1_rsrq).text = stats.rsrq
        view.findViewById<TextView>(R.id.sim1_sinr).text = stats.sinr
        view.findViewById<TextView>(R.id.sim1_pci).text = stats.pci
        view.findViewById<TextView>(R.id.sim1_band).text = stats.band

        // 2. Get Real Battery Data
        val batLevel = sysRepo.getBatteryLevel()
        view.findViewById<TextView>(R.id.txtBattery)?.text = "${batLevel}%"
    }
    
    private fun setupClicks(view: View) {
        view.findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<View>(R.id.cardData)?.setOnClickListener {
             parentFragmentManager.beginTransaction()
                .replace(R.id.container, DataUsageFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<View>(R.id.cardBattery)?.setOnClickListener {
             parentFragmentManager.beginTransaction()
                .replace(R.id.container, BatteryUsageFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(refreshRunnable)
    }
}
