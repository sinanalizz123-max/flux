package com.flux.app.service

import java.util.Random

class SystemMonitor {
    // Simulating Termux API calls for now
    fun getCurrentMa(): String {
        val rand = Random()
        val current = -1 * (rand.nextInt(300) + 200) // Random between -200 and -500
        return "$current mA"
    }
    
    fun getBandMHz(): String {
        return "2300+1800 MHz"
    }
}
