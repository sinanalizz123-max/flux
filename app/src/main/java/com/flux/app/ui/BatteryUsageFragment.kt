package com.flux.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.flux.app.R

class BatteryUsageFragment : Fragment(R.layout.fragment_battery_usage) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<LinearLayout>(R.id.batteryListContainer)
        
        val apps = listOf(
            Triple("Display", "34%", 90),
            Triple("Instagram", "12%", 40),
            Triple("Call of Duty", "8%", 30),
            Triple("WhatsApp", "5%", 20),
            Triple("Standby", "2%", 10)
        )

        val inflater = LayoutInflater.from(requireContext())

        for (app in apps) {
            val row = inflater.inflate(R.layout.item_usage_row, container, false)
            row.findViewById<TextView>(R.id.txtAppName).text = app.first
            row.findViewById<TextView>(R.id.txtUsageValue).text = app.second
            row.findViewById<ProgressBar>(R.id.progressBar).progress = app.third
            container.addView(row)
        }
    }
}
