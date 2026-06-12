package com.excap.capture

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * PCAP/PCAPNG Exporter
 * Handles export of captured traffic to PCAP and PCAPNG file formats.
 * Preserves the original PCAPdroid export functionality.
 *
 * Built by eXU CODER
 */
class PcapExporter(private val context: Context) {

    companion object {
        private const val TAG = "PcapExporter"
        private const val PCAP_MAGIC_NUMBER = 0xA1B2C3D4
        private const val PCAPNG_MAGIC_NUMBER = 0x0A0D0D0A
        private const val PCAP_VERSION_MAJOR = 2
        private const val PCAP_VERSION_MINOR = 4
        private const val PCAP_SNAPLEN = 65535
        private const val PCAP_LINKTYPE_RAW = 101
        private const val PCAP_LINKTYPE_ETHERNET = 1

        // PCAPNG block types
        private const val PCAPNG_SECTION_HEADER_BLOCK = 0x0A0D0D0A
        private const val PCAPNG_INTERFACE_DESCRIPTION_BLOCK = 0x00000001
        private const val PCAPNG_ENHANCED_PACKET_BLOCK = 0x00000006
        private const val PCAPNG_SIMPLE_PACKET_BLOCK = 0x00000003
    }

    /**
     * Export capture to PCAP file
     */
    suspend fun exportToPcap(
        connections: List<ConnectionInfo>,
        outputUri: Uri? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "excap_capture_$timestamp.pcap"

            val outputFile = if (outputUri != null) {
                // Write to provided URI
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    writePcapHeader(output)
                    connections.forEach { conn ->
                        conn.packets.forEach { packet ->
                            writePcapPacket(output, packet)
                        }
                    }
                }
                return@withContext Result.success(outputUri)
            } else {
                // Write to downloads directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, filename)
                FileOutputStream(file).use { output ->
                    writePcapHeader(output)
                    connections.forEach { conn ->
                        conn.packets.forEach { packet ->
                            writePcapPacket(output, packet)
                        }
                    }
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            Log.i(TAG, "PCAP export successful: $filename")
            Result.success(outputUri ?: outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "PCAP export failed", e)
            Result.failure(e)
        }
    }

    /**
     * Export capture to PCAPNG file
     */
    suspend fun exportToPcapNg(
        connections: List<ConnectionInfo>,
        outputUri: Uri? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "excap_capture_$timestamp.pcapng"

            val outputFile = if (outputUri != null) {
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    writePcapNgFile(output, connections)
                }
                return@withContext Result.success(outputUri)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, filename)
                FileOutputStream(file).use { output ->
                    writePcapNgFile(output, connections)
                }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }

            Log.i(TAG, "PCAPNG export successful: $filename")
            Result.success(outputUri ?: outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "PCAPNG export failed", e)
            Result.failure(e)
        }
    }

    /**
     * Start HTTP server for PCAP download
     */
    fun startHttpServer(port: Int = 8080, pcapFile: File): String {
        // Implementation uses native pcapd HTTP server
        return try {
            val result = nativeStartHttpServer(port, pcapFile.absolutePath)
            Log.i(TAG, "HTTP server started on port $port")
            "http://localhost:$port"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start HTTP server", e)
            ""
        }
    }

    /**
     * Stop HTTP server
     */
    fun stopHttpServer() {
        try {
            nativeStopHttpServer()
            Log.i(TAG, "HTTP server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping HTTP server", e)
        }
    }

    private fun writePcapHeader(output: OutputStream) {
        val header = ByteArrayOutputStream()
        val dos = DataOutputStream(header)

        // PCAP Global Header
        dos.writeInt(PCAP_MAGIC_NUMBER.toInt())        // magic number
        dos.writeShort(PCAP_VERSION_MAJOR)     // version major
        dos.writeShort(PCAP_VERSION_MINOR)     // version minor
        dos.writeInt(0)                         // thiszone
        dos.writeInt(0)                         // sigfigs
        dos.writeInt(PCAP_SNAPLEN)             // snaplen
        dos.writeInt(PCAP_LINKTYPE_RAW)        // network (RAW IP)

        output.write(header.toByteArray())
    }

    private fun writePcapPacket(output: OutputStream, packet: PacketData) {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        val timestampSeconds = (packet.timestamp / 1000).toInt()
        val timestampMicroseconds = ((packet.timestamp % 1000) * 1000).toInt()

        dos.writeInt(timestampSeconds)          // ts_sec
        dos.writeInt(timestampMicroseconds)     // ts_usec
        dos.writeInt(packet.data.size)          // incl_len
        dos.writeInt(packet.data.size)          // orig_len
        dos.write(packet.data)                  // packet data

        output.write(bos.toByteArray())
    }

    private fun writePcapNgFile(output: OutputStream, connections: List<ConnectionInfo>) {
        // Write Section Header Block
        writePcapNgSectionHeader(output)

        // Write Interface Description Block
        writePcapNgInterfaceDescription(output)

        // Write Enhanced Packet Blocks
        connections.forEach { conn ->
            conn.packets.forEach { packet ->
                writePcapNgEnhancedPacketBlock(output, packet)
            }
        }
    }

    private fun writePcapNgSectionHeader(output: OutputStream) {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        dos.writeInt(PCAPNG_SECTION_HEADER_BLOCK)  // Block Type
        dos.writeInt(28)                            // Block Total Length
        dos.writeInt(PCAPNG_MAGIC_NUMBER)           // Byte-Order Magic
        dos.writeShort(1)                           // Major Version
        dos.writeShort(0)                           // Minor Version
        dos.writeLong(-1)                           // Section Length (unknown)
        dos.writeInt(28)                            // Block Total Length

        output.write(bos.toByteArray())
    }

    private fun writePcapNgInterfaceDescription(output: OutputStream) {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        dos.writeInt(PCAPNG_INTERFACE_DESCRIPTION_BLOCK)  // Block Type
        dos.writeInt(20)                                   // Block Total Length
        dos.writeShort(PCAP_LINKTYPE_RAW)                  // Link Type
        dos.writeShort(0)                                  // Reserved
        dos.writeInt(PCAP_SNAPLEN)                         // Snap Length
        dos.writeInt(20)                                   // Block Total Length

        output.write(bos.toByteArray())
    }

    private fun writePcapNgEnhancedPacketBlock(output: OutputStream, packet: PacketData) {
        val packetData = packet.data
        val paddingSize = (4 - (packetData.size % 4)) % 4
        val blockTotalLength = 32 + packetData.size + paddingSize

        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        dos.writeInt(PCAPNG_ENHANCED_PACKET_BLOCK)     // Block Type
        dos.writeInt(blockTotalLength)                  // Block Total Length
        dos.writeInt(0)                                  // Interface ID
        dos.writeInt((packet.timestamp / 1000).toInt()) // Timestamp (High)
        dos.writeInt(((packet.timestamp % 1000) * 1000).toInt()) // Timestamp (Low)
        dos.writeInt(packetData.size)                   // Captured Packet Length
        dos.writeInt(packetData.size)                   // Original Packet Length
        dos.write(packetData)                           // Packet Data

        // Padding
        repeat(paddingSize) { dos.writeByte(0) }

        dos.writeInt(blockTotalLength)                  // Block Total Length

        output.write(bos.toByteArray())
    }

    // Native methods for HTTP server
    private external fun nativeStartHttpServer(port: Int, pcapPath: String): Int
    private external fun nativeStopHttpServer(): Int
}

/**
 * Connection info for export
 */
data class ConnectionInfo(
    val id: String,
    val protocol: String,
    val sourceIp: String,
    val sourcePort: Int,
    val destinationIp: String,
    val destinationPort: Int,
    val appName: String,
    val packets: List<PacketData>
)

/**
 * Packet data for export
 */
data class PacketData(
    val timestamp: Long,
    val data: ByteArray,
    val direction: Int  // 0 = outgoing, 1 = incoming
)
