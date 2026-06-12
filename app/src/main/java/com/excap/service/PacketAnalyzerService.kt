package com.excap.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.emanuelef.remote_capture.R
import com.excap.database.AppDatabase
import com.excap.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class PacketAnalyzerService : Service() {

    companion object {
        private const val TAG = "PacketAnalyzerService"
        const val NOTIFICATION_CHANNEL_ID = "excap_analyzer_channel"
        const val NOTIFICATION_ID = 1002
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var database: AppDatabase? = null
    private val binder = AnalyzerBinder()

    // Observable states
    private val _captureState = MutableStateFlow(false)
    val captureState: StateFlow<Boolean> = _captureState.asStateFlow()

    private val _recentPackets = MutableStateFlow<List<PacketInfo>>(emptyList())
    val recentPackets: StateFlow<List<PacketInfo>> = _recentPackets.asStateFlow()

    private val _appStats = MutableStateFlow<List<AppTrafficStats>>(emptyList())
    val appStats: StateFlow<List<AppTrafficStats>> = _appStats.asStateFlow()

    private val _connections = MutableStateFlow<List<ConnectionInfo>>(emptyList())
    val connections: StateFlow<List<ConnectionInfo>> = _connections.asStateFlow()

    private val _statistics = MutableStateFlow(CaptureStatistics())
    val statistics: StateFlow<CaptureStatistics> = _statistics.asStateFlow()

    inner class AnalyzerBinder : Binder() {
        fun getService(): PacketAnalyzerService = this@PacketAnalyzerService
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        createNotificationChannel()
        startDataCollection()
    }

    private fun startDataCollection() {
        // Collect recent packets
        serviceScope.launch {
            database?.packetDao()?.getRecentPackets(100)?.collect { packets ->
                _recentPackets.value = packets
                updateStatistics(packets)
            }
        }

        // Collect app stats
        serviceScope.launch {
            database?.appStatsDao()?.getAllAppStats()?.collect { stats ->
                _appStats.value = stats
            }
        }

        // Collect active connections
        serviceScope.launch {
            database?.connectionDao()?.getActiveConnections()?.collect { conns ->
                _connections.value = conns
            }
        }
    }

    private fun updateStatistics(packets: List<PacketInfo>) {
        if (packets.isEmpty()) return

        val totalBytes = packets.sumOf { it.totalSize }
        val httpCount = packets.count { it.protocol == "HTTP" }
        val httpsCount = packets.count { it.protocol == "HTTPS" }
        val tcpCount = packets.count { it.protocol == "TCP" }
        val uniqueHosts = packets.map { it.destinationHost }.distinct().size

        _statistics.value = CaptureStatistics(
            totalPackets = packets.size.toLong(),
            totalBytes = totalBytes,
            httpRequests = httpCount.toLong(),
            httpsRequests = httpsCount.toLong(),
            tcpConnections = tcpCount.toLong(),
            uniqueHosts = uniqueHosts,
            averagePacketSize = if (packets.isNotEmpty()) totalBytes / packets.size else 0,
            captureRate = calculateCaptureRate(packets)
        )
    }

    private fun calculateCaptureRate(packets: List<PacketInfo>): Float {
        if (packets.size < 2) return 0f
        val timeSpan = (packets.first().timestamp - packets.last().timestamp) / 1000f
        return if (timeSpan > 0) packets.size / timeSpan else 0f
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "eXcap Analyzer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background packet analysis"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("eXcap Analyzer")
            .setContentText("Analyzing network traffic...")
            .setSmallIcon(R.drawable.ic_analyzer)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    data class CaptureStatistics(
        val totalPackets: Long = 0,
        val totalBytes: Long = 0,
        val httpRequests: Long = 0,
        val httpsRequests: Long = 0,
        val tcpConnections: Long = 0,
        val uniqueHosts: Int = 0,
        val averagePacketSize: Long = 0,
        val captureRate: Float = 0f
    )
}
