package com.flux.app.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.flux.app.R
import com.flux.app.data.FluxVpnService
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var vpnSwitch: SwitchMaterial

    // Handler for the System VPN Dialog
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startFluxVpn()
            vpnSwitch.isChecked = true
        } else {
            vpnSwitch.isChecked = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back Button
        view.findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // VPN Switch Logic
        vpnSwitch = view.findViewById(R.id.switch_vpn)
        
        vpnSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prepareVpn()
            } else {
                stopFluxVpn()
            }
        }
    }

    private fun prepareVpn() {
        // This checks if the user has already granted permission
        val intent = VpnService.prepare(requireContext())
        if (intent != null) {
            // Permission needed: Show system dialog
            vpnPermissionLauncher.launch(intent)
        } else {
            // Already allowed: Start directly
            startFluxVpn()
        }
    }

    private fun startFluxVpn() {
        val intent = Intent(requireContext(), FluxVpnService::class.java)
        requireContext().startService(intent)
    }

    private fun stopFluxVpn() {
        val intent = Intent(requireContext(), FluxVpnService::class.java)
        intent.action = "STOP"
        requireContext().startService(intent)
    }
}
