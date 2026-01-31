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

    @SuppressLint("MissingPermission")
    fun getNetworkStats(): NetworkStats {
        try {
            val allCellInfo = tm.allCellInfo ?: return NetworkStats()
            
            for (info in allCellInfo) {
                if (info.isRegistered) {
                    when (info) {
                        is CellInfoLte -> {
                            val s = info.cellSignalStrength
                            val i = info.cellIdentity
                            return NetworkStats(
                                type = "LTE",
                                rsrp = if (s.rsrp != 2147483647) "${s.rsrp} dBm" else "--",
                                rsrq = if (s.rsrq != 2147483647) "${s.rsrq} dB" else "--",
                                sinr = if (s.rssnr != 2147483647) "${s.rssnr / 10} dB" else "--",
                                pci = i.pci.toString(),
                                band = getBand(i.earfcn)
                            )
                        }
                        is CellInfoNr -> {
                             val s = info.cellSignalStrength as android.telephony.CellSignalStrengthNr
                             val i = info.cellIdentity as android.telephony.CellIdentityNr
                             return NetworkStats(
                                type = "5G",
                                rsrp = "${s.csiRsrp} dBm",
                                rsrq = "${s.csiRsrq} dB",
                                sinr = "${s.ssSinr} dB",
                                pci = i.pci.toString(),
                                band = "N78 / 5G"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return NetworkStats(band = "No Perm")
        }
        return NetworkStats()
    }

    private fun getBand(earfcn: Int): String {
        return when (earfcn) {
            in 1200..1949 -> "Band 3"
            in 2750..3449 -> "Band 7"
            in 38650..39649 -> "Band 40"
            else -> "Band ${earfcn}"
        }
    }
}
