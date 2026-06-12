package com.excap.model


@Entity(
    tableName = "captured_packets",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["appPackage"]),
        Index(value = ["protocol"]),
        Index(value = ["connectionId"])
    ]
)
data class PacketInfo(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val appPackage: String = "",
    val appName: String = "",
    val protocol: String = "TCP",
    val sourceIp: String = "",
    val sourcePort: Int = 0,
    val destinationIp: String = "",
    val destinationPort: Int = 0,
    val destinationHost: String = "",
    val payloadSize: Long = 0,
    val totalSize: Long = 0,
    val method: String? = null,
    val url: String? = null,
    val statusCode: Int? = null,
    val headers: String? = null,
    val payload: String? = null,
    val connectionId: String = "",
    val direction: String = "OUTGOING",
    val flags: String = "",
    val sequenceNumber: Long = 0,
    val acknowledgmentNumber: Long = 0,
    val tlsVersion: String? = null,
    val cipherSuite: String? = null,
    val isDecrypted: Boolean = false,
    val tags: String = ""
)

data class AppTrafficStats(
    @PrimaryKey
    val packageName: String = "",
    val appName: String = "",
    val totalBytesSent: Long = 0,
    val totalBytesReceived: Long = 0,
    val totalPackets: Long = 0,
    val httpRequests: Long = 0,
    val httpsRequests: Long = 0,
    val tcpConnections: Long = 0,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val isMonitored: Boolean = true,
    val isBlocked: Boolean = false,
    val iconBase64: String? = null
)

data class ConnectionInfo(
    @PrimaryKey
    val connectionId: String = "",
    val packageName: String = "",
    val appName: String = "",
    val destinationHost: String = "",
    val destinationIp: String = "",
    val destinationPort: Int = 0,
    val protocol: String = "TCP",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val bytesSent: Long = 0,
    val bytesReceived: Long = 0,
    val packetsSent: Long = 0,
    val packetsReceived: Long = 0,
    val isActive: Boolean = true,
    val tlsVersion: String? = null,
    val sniHostname: String? = null
)

data class FilterRule(
    val id: Long = 0,
    val name: String = "",
    val type: String = "APP",
    val target: String = "",
    val action: String = "CAPTURE",
    val isEnabled: Boolean = true,
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
