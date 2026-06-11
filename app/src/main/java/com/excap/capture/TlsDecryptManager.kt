package com.excap.capture

import android.content.Context
import android.content.Intent
import android.security.KeyChain
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * TLS Decryption Manager
 * Handles certificate installation and TLS decryption setup for HTTPS inspection.
 * Integrates with mitmproxy for TLS decryption capabilities.
 *
 * Built by eXU CODER
 */
class TlsDecryptManager(private val context: Context) {

    companion object {
        private const val TAG = "TlsDecryptManager"
        const val CA_CERTIFICATE_ALIAS = "eXcap CA"
        const val CA_CERTIFICATE_FILE = "excap-ca.crt"
        const val KEYLOG_FILE = "sslkeylogfile.txt"
        const val MITMPROXY_PORT = 8080
        const val CERT_VALIDITY_DAYS = 365

        // Certificate file paths
        fun getCaCertPath(context: Context): String {
            return File(context.filesDir, CA_CERTIFICATE_FILE).absolutePath
        }

        fun getKeylogFilePath(context: Context): File {
            return File(context.cacheDir, KEYLOG_FILE)
        }
    }

    private var isDecryptionEnabled = false
    private var mitmProxyRunning = false

    /**
     * Check if CA certificate is installed
     */
    fun isCaCertificateInstalled(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidCAStore")
            keyStore.load(null, null)
            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()
                if (alias.contains("eXcap") || alias.contains("excap")) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking CA certificate", e)
            false
        }
    }

    /**
     * Generate and install CA certificate
     */
    suspend fun generateAndInstallCertificate(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Generate self-signed CA certificate using native code
            val certPath = getCaCertPath(context)
            val result = nativeGenerateCertificate(certPath, CERT_VALIDITY_DAYS)
            
            if (result == 0) {
                installCertificate(certPath)
                Log.i(TAG, "CA certificate generated and installed")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to generate certificate (error $result)"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Certificate generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Install certificate to system trust store
     */
    fun installCertificate(certPath: String) {
        try {
            val certFile = File(certPath)
            if (!certFile.exists()) {
                throw IllegalStateException("Certificate file not found")
            }

            val intent = KeyChain.createInstallIntent()
            intent.putExtra(KeyChain.EXTRA_NAME, CA_CERTIFICATE_ALIAS)
            intent.putExtra(KeyChain.EXTRA_CERTIFICATE, certFile.readBytes())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            
            Log.i(TAG, "Certificate install intent launched")
        } catch (e: Exception) {
            Log.e(TAG, "Certificate installation failed", e)
        }
    }

    /**
     * Enable TLS decryption
     */
    fun enableDecryption() {
        isDecryptionEnabled = true
        Log.i(TAG, "TLS decryption enabled")
    }

    /**
     * Disable TLS decryption
     */
    fun disableDecryption() {
        isDecryptionEnabled = false
        stopMitmProxy()
        Log.i(TAG, "TLS decryption disabled")
    }

    /**
     * Start mitmproxy for TLS decryption
     */
    fun startMitmProxy() {
        if (mitmProxyRunning) return
        
        try {
            val keylogPath = getKeylogFilePath(context).absolutePath
            val result = nativeStartMitmProxy(MITMPROXY_PORT, keylogPath)
            if (result == 0) {
                mitmProxyRunning = true
                Log.i(TAG, "mitmproxy started on port $MITMPROXY_PORT")
            } else {
                Log.e(TAG, "Failed to start mitmproxy (error $result)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "mitmproxy start failed", e)
        }
    }

    /**
     * Stop mitmproxy
     */
    fun stopMitmProxy() {
        if (!mitmProxyRunning) return
        
        try {
            nativeStopMitmProxy()
            mitmProxyRunning = false
            Log.i(TAG, "mitmproxy stopped")
        } catch (e: Exception) {
            Log.e(TAG, "mitmproxy stop failed", e)
        }
    }

    /**
     * Check if decryption is active
     */
    fun isDecryptionActive(): Boolean {
        return isDecryptionEnabled && mitmProxyRunning
    }

    /**
     * Get SSL key log file for Wireshark
     */
    fun getSslKeyLogFile(): File? {
        val file = getKeylogFilePath(context)
        return if (file.exists()) file else null
    }

    /**
     * Export SSL key log file
     */
    suspend fun exportKeyLogFile(outputFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val keylogFile = getKeylogFilePath(context)
            if (!keylogFile.exists()) {
                return@withContext Result.failure(Exception("No key log data available"))
            }
            
            keylogFile.copyTo(outputFile, overwrite = true)
            Log.i(TAG, "SSL key log exported to ${outputFile.absolutePath}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "SSL key log export failed", e)
            Result.failure(e)
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopMitmProxy()
        isDecryptionEnabled = false
    }

    // Native methods
    private external fun nativeGenerateCertificate(certPath: String, validityDays: Int): Int
    private external fun nativeStartMitmProxy(port: Int, keylogPath: String): Int
    private external fun nativeStopMitmProxy(): Int
}
