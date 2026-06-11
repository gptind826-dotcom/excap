package com.excap.parser

import android.util.Log
import com.excap.model.PacketInfo
import com.excap.model.Protocol
import java.nio.ByteBuffer
import java.nio.charset.Charset

object PacketParser {
    private const val TAG = "PacketParser"
    
    // IP Protocol numbers
    private const val IPPROTO_TCP = 6
    private const val IPPROTO_UDP = 17
    
    // TCP Flags
    private const val TCP_FIN = 0x01
    private const val TCP_SYN = 0x02
    private const val TCP_RST = 0x04
    private const val TCP_PSH = 0x08
    private const val TCP_ACK = 0x10
    private const val TCP_URG = 0x20
    
    // Common ports
    private const val PORT_HTTP = 80
    private const val PORT_HTTPS = 443
    private const val PORT_DNS = 53

    data class ParsedPacket(
        val packetInfo: PacketInfo,
        val rawPayload: ByteArray?
    )

    fun parsePacket(rawData: ByteArray, length: Int, appPackage: String = "", appName: String = ""): ParsedPacket? {
        try {
            if (length < 20) return null
            
            val buffer = ByteBuffer.wrap(rawData, 0, length)
            
            // Parse IP Header
            val versionIHL = buffer.get().toInt() and 0xFF
            val ipVersion = versionIHL shr 4
            val headerLength = (versionIHL and 0x0F) * 4
            
            if (ipVersion != 4 || length < headerLength) return null
            
            buffer.get() // DSCP/ECN
            val totalLength = buffer.short.toInt() and 0xFFFF
            buffer.short // Identification
            buffer.short // Flags/Fragment offset
            buffer.get() // TTL
            val protocol = buffer.get().toInt() and 0xFF
            buffer.short // Checksum
            
            // Source IP
            val sourceIpBytes = ByteArray(4)
            buffer.get(sourceIpBytes)
            val sourceIp = sourceIpBytes.joinToString(".") { (it.toInt() and 0xFF).toString() }
            
            // Destination IP
            val destIpBytes = ByteArray(4)
            buffer.get(destIpBytes)
            val destIp = destIpBytes.joinToString(".") { (it.toInt() and 0xFF).toString() }
            
            val (sourcePort, destPort, tcpHeaderLength, flags, seqNum, ackNum, payload) = when (protocol) {
                IPPROTO_TCP -> parseTcpHeader(buffer, length, headerLength)
                IPPROTO_UDP -> parseUdpHeader(buffer, length, headerLength)
                else -> return null
            }
            
            val protocolName = when {
                destPort == PORT_HTTP || sourcePort == PORT_HTTP -> "HTTP"
                destPort == PORT_HTTPS || sourcePort == PORT_HTTPS -> "HTTPS"
                destPort == PORT_DNS || sourcePort == PORT_DNS -> "DNS"
                protocol == IPPROTO_TCP -> "TCP"
                protocol == IPPROTO_UDP -> "UDP"
                else -> "UNKNOWN"
            }
            
            // Determine direction based on port (simplified)
            val direction = if (destPort == PORT_HTTP || destPort == PORT_HTTPS || destPort == PORT_DNS) {
                "OUTGOING"
            } else {
                "INCOMING"
            }
            
            // Try to parse HTTP if applicable
            var method: String? = null
            var url: String? = null
            var httpHeaders: String? = null
            var statusCode: Int? = null
            
            if (protocolName == "HTTP" && payload != null && payload.isNotEmpty()) {
                val httpInfo = parseHttpPayload(payload)
                method = httpInfo.method
                url = httpInfo.url
                httpHeaders = httpInfo.headers
                statusCode = httpInfo.statusCode
            }
            
            val flagStr = if (protocol == IPPROTO_TCP) parseTcpFlags(flags) else ""
            
            val packetInfo = PacketInfo(
                timestamp = System.currentTimeMillis(),
                appPackage = appPackage,
                appName = appName,
                protocol = protocolName,
                sourceIp = sourceIp,
                sourcePort = sourcePort,
                destinationIp = destIp,
                destinationPort = destPort,
                payloadSize = payload?.size?.toLong() ?: 0,
                totalSize = totalLength.toLong(),
                method = method,
                url = url,
                statusCode = statusCode,
                headers = httpHeaders,
                payload = payload?.let { hexDump(it) },
                connectionId = generateConnectionId(sourceIp, sourcePort, destIp, destPort),
                direction = direction,
                flags = flagStr,
                sequenceNumber = seqNum,
                acknowledgmentNumber = ackNum
            )
            
            return ParsedPacket(packetInfo, payload)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing packet", e)
            return null
        }
    }
    
    private fun parseTcpHeader(
        buffer: ByteBuffer, 
        totalLength: Int, 
        ipHeaderLength: Int
    ): TcpParseResult {
        val sourcePort = buffer.short.toInt() and 0xFFFF
        val destPort = buffer.short.toInt() and 0xFFFF
        val seqNum = buffer.int.toLong() and 0xFFFFFFFFL
        val ackNum = buffer.int.toLong() and 0xFFFFFFFFL
        val dataOffset = (buffer.get().toInt() and 0xFF) shr 4
        val flags = buffer.get().toInt() and 0xFF
        buffer.short // Window size
        buffer.short // Checksum
        buffer.short // Urgent pointer
        
        val tcpHeaderLength = dataOffset * 4
        val payloadOffset = ipHeaderLength + tcpHeaderLength
        
        val payload = if (totalLength > payloadOffset) {
            val payloadSize = totalLength - payloadOffset
            val payloadBytes = ByteArray(payloadSize)
            buffer.get(payloadBytes)
            payloadBytes
        } else null
        
        return TcpParseResult(sourcePort, destPort, tcpHeaderLength, flags, seqNum, ackNum, payload)
    }
    
    private fun parseUdpHeader(
        buffer: ByteBuffer,
        totalLength: Int,
        ipHeaderLength: Int
    ): TcpParseResult {
        val sourcePort = buffer.short.toInt() and 0xFFFF
        val destPort = buffer.short.toInt() and 0xFFFF
        val udpLength = buffer.short.toInt() and 0xFFFF
        buffer.short // Checksum
        
        val payloadSize = udpLength - 8
        val payload = if (payloadSize > 0 && buffer.remaining() >= payloadSize) {
            val payloadBytes = ByteArray(payloadSize)
            buffer.get(payloadBytes)
            payloadBytes
        } else null
        
        return TcpParseResult(sourcePort, destPort, 8, 0, 0, 0, payload)
    }
    
    private data class TcpParseResult(
        val sourcePort: Int,
        val destPort: Int,
        val headerLength: Int,
        val flags: Int,
        val seqNum: Long,
        val ackNum: Long,
        val payload: ByteArray?
    )
    
    private fun parseTcpFlags(flags: Int): String {
        val flagList = mutableListOf<String>()
        if (flags and TCP_SYN != 0) flagList.add("SYN")
        if (flags and TCP_ACK != 0) flagList.add("ACK")
        if (flags and TCP_FIN != 0) flagList.add("FIN")
        if (flags and TCP_RST != 0) flagList.add("RST")
        if (flags and TCP_PSH != 0) flagList.add("PSH")
        if (flags and TCP_URG != 0) flagList.add("URG")
        return flagList.joinToString(",")
    }
    
    private data class HttpInfo(
        val method: String?,
        val url: String?,
        val headers: String?,
        val statusCode: Int?
    )
    
    private fun parseHttpPayload(payload: ByteArray): HttpInfo {
        try {
            val payloadStr = String(payload, Charset.forName("UTF-8"))
            val lines = payloadStr.split("\r\n")
            
            if (lines.isEmpty()) return HttpInfo(null, null, null, null)
            
            val firstLine = lines[0]
            
            // Check if request or response
            return if (firstLine.startsWith("HTTP/")) {
                // Response
                val parts = firstLine.split(" ")
                val statusCode = parts.getOrNull(1)?.toIntOrNull()
                HttpInfo(null, null, extractHeaders(lines), statusCode)
            } else {
                // Request
                val parts = firstLine.split(" ")
                val method = parts.getOrNull(0)
                val path = parts.getOrNull(1)
                val host = lines.find { it.startsWith("Host:", ignoreCase = true) }
                    ?.substringAfter(":")?.trim()
                val url = if (host != null && path != null) "https://$host$path" else path
                HttpInfo(method, url, extractHeaders(lines), null)
            }
        } catch (e: Exception) {
            return HttpInfo(null, null, null, null)
        }
    }
    
    private fun extractHeaders(lines: List<String>): String {
        val headers = mutableListOf<String>()
        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isEmpty()) break
            headers.add(line)
        }
        return headers.joinToString("\n")
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
    
    fun generateConnectionId(srcIp: String, srcPort: Int, dstIp: String, dstPort: Int): String {
        val minIp = if (srcIp < dstIp) srcIp else dstIp
        val maxIp = if (srcIp > dstIp) srcIp else dstIp
        val minPort = if (srcPort < dstPort) srcPort else dstPort
        val maxPort = if (srcPort > dstPort) srcPort else dstPort
        return "$minIp:$minPort-$maxIp:$maxPort"
    }
}
