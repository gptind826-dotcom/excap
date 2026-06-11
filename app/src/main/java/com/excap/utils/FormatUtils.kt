package com.excap.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.2f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.2f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.2f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }

    fun formatBitsPerSecond(bitsPerSecond: Long): String {
        return when {
            bitsPerSecond >= 1_000_000_000 -> String.format("%.2f Gbps", bitsPerSecond / 1_000_000_000.0)
            bitsPerSecond >= 1_000_000 -> String.format("%.2f Mbps", bitsPerSecond / 1_000_000.0)
            bitsPerSecond >= 1_000 -> String.format("%.2f Kbps", bitsPerSecond / 1_000.0)
            else -> "$bitsPerSecond bps"
        }
    }

    fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    fun formatNumber(number: Long): String {
        return String.format("%,d", number)
    }

    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatIpAddress(ip: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            (ip shr 24) and 0xFF,
            (ip shr 16) and 0xFF,
            (ip shr 8) and 0xFF,
            ip and 0xFF
        )
    }

    fun hexDump(data: ByteArray, bytesPerLine: Int = 16): String {
        val result = StringBuilder()
        for (i in data.indices step bytesPerLine) {
            // Offset
            result.append(String.format("%04X  ", i))

            // Hex bytes
            for (j in 0 until bytesPerLine) {
                if (i + j < data.size) {
                    result.append(String.format("%02X ", data[i + j]))
                } else {
                    result.append("   ")
                }
            }

            result.append(" |")

            // ASCII representation
            for (j in 0 until bytesPerLine) {
                if (i + j < data.size) {
                    val b = data[i + j].toInt() and 0xFF
                    result.append(if (b in 32..126) b.toChar() else '.')
                }
            }

            result.append("|\n")
        }
        return result.toString()
    }

    fun getProtocolColor(protocol: String): Int {
        return when (protocol.uppercase()) {
            "HTTP" -> android.graphics.Color.parseColor("#4CAF50")
            "HTTPS" -> android.graphics.Color.parseColor("#2196F3")
            "TCP" -> android.graphics.Color.parseColor("#FF9800")
            "UDP" -> android.graphics.Color.parseColor("#9C27B0")
            "DNS" -> android.graphics.Color.parseColor("#00BCD4")
            "TLS" -> android.graphics.Color.parseColor("#E91E63")
            "QUIC" -> android.graphics.Color.parseColor("#FF5722")
            else -> android.graphics.Color.parseColor("#757575")
        }
    }
}
