package com.flux.app.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.flux.app.R
import com.flux.app.service.SystemMonitor

class BatteryFragment : Fragment(R.layout.fragment_battery) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val monitor = SystemMonitor()
        view.findViewById<TextView>(R.id.txt_current).text = monitor.getCurrentMa()
    }
}
