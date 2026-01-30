package com.flux.app.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.flux.app.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val vpnSwitch = view.findViewById<SwitchMaterial>(R.id.switch_vpn)
        val adBlockSwitch = view.findViewById<SwitchMaterial>(R.id.switch_adblock)

        vpnSwitch.setOnCheckedChangeListener { _, isChecked ->
            adBlockSwitch.isEnabled = isChecked
            adBlockSwitch.alpha = if (isChecked) 1.0f else 0.4f
            
            if (!isChecked) {
                adBlockSwitch.isChecked = false
            }
        }
    }
}
