package com.excap.capture

import android.util.Log

/**
 * nDPI (Deep Packet Inspection) Wrapper
 * Provides Kotlin bindings for the nDPI library integration.
 * Preserves the original PCAPdroid nDPI bindings.
 *
 * Built by eXU CODER
 */
class NdpiWrapper private constructor() {

    companion object {
        private const val TAG = "NdpiWrapper"

        @Volatile
        private var instance: NdpiWrapper? = null
        private var initialized = false

        fun getInstance(): NdpiWrapper {
            return instance ?: synchronized(this) {
                instance ?: NdpiWrapper().also { instance = it }
            }
        }

        // Protocol IDs from nDPI
        const val NDPI_PROTOCOL_UNKNOWN = 0
        const val NDPI_PROTOCOL_DNS = 5
        const val NDPI_PROTOCOL_TLS = 91
        const val NDPI_PROTOCOL_HTTP = 7
        const val NDPI_PROTOCOL_SSH = 22
        const val NDPI_PROTOCOL_FTP_CONTROL = 1
        const val NDPI_PROTOCOL_FTP_DATA = 2
        const val NDPI_PROTOCOL_SMTP = 3
        const val NDPI_PROTOCOL_POP3 = 4
        const val NDPI_PROTOCOL_IMAP = 8
        const val NDPI_PROTOCOL_NTP = 9
    }

    /**
     * Initialize the nDPI engine
     */
    fun initialize(): Boolean {
        if (initialized) return true
        return try {
            nativeInitNdpi()
            initialized = true
            Log.i(TAG, "nDPI initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize nDPI", e)
            false
        }
    }

    /**
     * Process a packet and get protocol detection result
     */
    fun detectProtocol(packetData: ByteArray, packetLen: Int, srcIp: String, dstIp: String): ProtocolDetection {
        return try {
            nativeDetectProtocol(packetData, packetLen, srcIp, dstIp)
        } catch (e: Exception) {
            Log.e(TAG, "Protocol detection failed", e)
            ProtocolDetection(NDPI_PROTOCOL_UNKNOWN, "UNKNOWN", 0.0f)
        }
    }

    /**
     * Get human-readable protocol name
     */
    fun getProtocolName(protocolId: Int): String {
        return try {
            nativeGetProtocolName(protocolId) ?: "UNKNOWN"
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }

    /**
     * Destroy nDPI resources
     */
    fun destroy() {
        if (!initialized) return
        try {
            nativeDestroyNdpi()
            initialized = false
            instance = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying nDPI", e)
        }
    }

    // Native methods
    private external fun nativeInitNdpi(): Int
    private external fun nativeDetectProtocol(
        packetData: ByteArray,
        packetLen: Int,
        srcIp: String,
        dstIp: String
    ): ProtocolDetection
    private external fun nativeGetProtocolName(protocolId: Int): String?
    private external fun nativeDestroyNdpi()
}

/**
 * Protocol detection result
 */
data class ProtocolDetection(
    val protocolId: Int,
    val protocolName: String,
    val confidence: Float
) {
    fun isEncrypted(): Boolean {
        return protocolId == NdpiWrapper.NDPI_PROTOCOL_TLS
    }

    fun isHttp(): Boolean {
        return protocolId == NdpiWrapper.NDPI_PROTOCOL_HTTP
    }

    fun isDns(): Boolean {
        return protocolId == NdpiWrapper.NDPI_PROTOCOL_DNS
    }
}
