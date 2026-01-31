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

class DataUsageFragment : Fragment(R.layout.fragment_data_usage) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = view.findViewById<LinearLayout>(R.id.dataListContainer)
        
        // Mock Data: In real app, we fetch this from NetworkStatsManager
        val apps = listOf(
            Triple("Instagram", "2.4 GB", 80),
            Triple("YouTube", "1.8 GB", 60),
            Triple("Chrome", "850 MB", 40),
            Triple("Spotify", "420 MB", 25),
            Triple("System", "120 MB", 10)
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
