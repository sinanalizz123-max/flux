package com.flux.app.ui

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var netRepo: NetworkRepo
    private lateinit var sysRepo: SystemRepo
    private val handler = Handler(Looper.getMainLooper())
    
    private val multiPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
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
        checkAndRequestAllPermissions()
        handler.post(refreshRunnable)
    }

    private fun checkAndRequestAllPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needed = perms.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (needed.isNotEmpty()) {
            multiPermissionLauncher.launch(needed)
        }
    }

    private fun updateUI() {
        val view = view ?: return
        
        // 1. Network Card Logic
        val hasLoc = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (hasLoc) {
            val stats = netRepo.getNetworkStats()
            setText(view, R.id.sim1_rsrp, stats.rsrp, R.color.leica_white)
            setText(view, R.id.sim1_rsrq, stats.rsrq, R.color.leica_white)
            setText(view, R.id.sim1_sinr, stats.sinr, R.color.leica_white)
            setText(view, R.id.sim1_pci, stats.pci, R.color.leica_white)
            setText(view, R.id.sim1_band, stats.band, R.color.classic_red)
        } else {
            setText(view, R.id.sim1_rsrp, "No Perm", R.color.classic_red)
            setText(view, R.id.sim1_band, "Permission Required", R.color.classic_red)
        }

        // 2. Data Card Logic (FIXED CRASH HERE)
        val lblData = view.findViewById<TextView>(R.id.lblData)
        if (hasUsageStatsPermission()) {
            lblData.text = "Data Usage"
            lblData.setTextColor(ContextCompat.getColor(requireContext(), R.color.leica_white))
        } else {
            lblData.text = "Grant Access"
            lblData.setTextColor(ContextCompat.getColor(requireContext(), R.color.classic_red))
        }

        // 3. Battery Logic
        val batLevel = sysRepo.getBatteryLevel()
        view.findViewById<TextView>(R.id.txtBattery)?.text = "${batLevel}%"
    }

    private fun setText(view: View, id: Int, text: String, colorRes: Int) {
        val tv = view.findViewById<TextView>(id)
        tv?.text = text
        tv?.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }
    
    private fun setupClicks(view: View) {
        view.findViewById<ImageView>(R.id.btnSettings)?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Data Card Click
        view.findViewById<View>(R.id.cardData)?.setOnClickListener {
            if (hasUsageStatsPermission()) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, DataUsageFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Redirecting to Settings...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }

        // Battery Card Click
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
