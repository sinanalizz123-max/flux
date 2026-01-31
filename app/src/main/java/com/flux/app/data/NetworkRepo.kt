package com.flux.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.TelephonyManager
import android.os.Build

data class NetworkStats(
    val type: String = "Unknown",
    val rsrp: String = "--",
    val rsrq: String = "--",
    val sinr: String = "--",
    val pci: String = "--",
    val band: String = "Searching..."
)

class NetworkRepo(private val context: Context) {

    private val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    @SuppressLint("MissingPermission") // We handle permissions in UI
    fun getNetworkStats(): NetworkStats {
        val stats = NetworkStats()
        
        // If no permission or no SIM, return empty
        try {
            val allCellInfo = tm.allCellInfo ?: return stats
            
            for (info in allCellInfo) {
                if (info.isRegistered) {
                    when (info) {
                        is CellInfoLte -> {
                            val signal = info.cellSignalStrength
                            val id = info.cellIdentity
                            return NetworkStats(
                                type = "LTE / 4G",
                                rsrp = "${signal.rsrp} dBm",
                                rsrq = "${signal.rsrq} dB",
                                sinr = "${signal.rssnr / 10.0} dB",
                                pci = "${id.pci}",
                                band = "Band ${getBand(id.earfcn)}"
                            )
                        }
                        is CellInfoNr -> { // 5G
                             val signal = info.cellSignalStrength as android.telephony.CellSignalStrengthNr
                             val id = info.cellIdentity as android.telephony.CellIdentityNr
                             return NetworkStats(
                                type = "5G NR",
                                rsrp = "${signal.csiRsrp} dBm",
                                rsrq = "${signal.csiRsrq} dB",
                                sinr = "${signal.ssSinr} dB",
                                pci = "${id.pci}",
                                band = "N78 / 5G"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return NetworkStats(band = "Error: ${e.message}")
        }
        return stats
    }

    // Simple helper to guess band from frequency (Simplified map)
    private fun getBand(earfcn: Int): String {
        return when (earfcn) {
            in 1200..1949 -> "3 (1800 MHz)"
            in 2750..3449 -> "7 (2600 MHz)"
            in 38650..39649 -> "40 (2300 MHz)"
            else -> "Unknown (${earfcn})"
        }
    }
}
