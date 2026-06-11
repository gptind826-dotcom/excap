package com.emanuelef.remote_capture.ui.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emanuelef.remote_capture.CaptureService
import com.emanuelef.remote_capture.model.AppState
import com.emanuelef.remote_capture.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

/* eXcap Capture Screen
 * Built by eXU CODER
 * Central toggle with animated waveform, real-time bandwidth, filter chips
 */

@Composable
fun CaptureScreen(
    onToggleCapture: () -> Unit,
    onHapticFeedback: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val appState by remember { mutableStateOf(AppState.idle) }
    var isCapturing by remember { mutableStateOf(false) }
    var bytesReceived by remember { mutableLongStateOf(0L) }
    var bytesSent by remember { mutableLongStateOf(0L) }
    var connectionCount by remember { mutableIntStateOf(0) }

    // Observe capture service state
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val status = CaptureService.getStatus()
                isCapturing = (status == AppState.running)
            }
        }
        val filter = IntentFilter(CaptureService.ACTION_STATE_CHANGED)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "eXcap",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Network Capture & Analysis",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Main Capture Toggle with Waveform
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CaptureToggle(
                isCapturing = isCapturing,
                onToggle = {
                    onHapticFeedback()
                    onToggleCapture()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Real-time Stats
        if (isCapturing) {
            RealtimeStats(
                bytesReceived = bytesReceived,
                bytesSent = bytesSent,
                connectionCount = connectionCount
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        FilterChipRow()

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        StatusCard(isCapturing = isCapturing)
    }
}

@Composable
fun CaptureToggle(
    isCapturing: Boolean,
    onToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCapturing) 1.05f else 1.0f,
        animationSpec = tween(600, easing = EaseInOutCubic),
        label = "scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isCapturing) 0.6f else 0.0f,
        animationSpec = tween(800),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Animated waveform background
        if (isCapturing) {
            AnimatedWaveform(
                modifier = Modifier.fillMaxSize(),
                color = ExcapBlue.copy(alpha = 0.3f)
            )
        }

        // Glow effect
        if (isCapturing) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(ExcapBlue.copy(alpha = glowAlpha * 0.3f))
            )
        }

        // Main button
        FilledIconButton(
            onClick = onToggle,
            modifier = Modifier
                .size(180.dp)
                .shadow(
                    elevation = if (isCapturing) 24.dp else 8.dp,
                    shape = CircleShape,
                    spotColor = if (isCapturing) ExcapBlue else Color.Transparent
                ),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isCapturing) ExcapError else ExcapBlue,
                contentColor = Color.White
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCapturing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isCapturing) "Stop Capture" else "Start Capture",
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = if (isCapturing) "STOP" else "START",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedWaveform(
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    // Multiple animated phases for rich waveform
    val phases = List(5) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + index * 400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase_$index"
        )
    }

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Transparent)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2.5f

        // Draw concentric waveform rings
        phases.forEachIndexed { index, phase ->
            val waveRadius = radius * (0.6f + index * 0.15f)
            val strokeWidth = 2f + index * 1.5f
            
            androidx.compose.ui.graphics.drawscope.drawContext.canvas.nativeCanvas.apply {
                val path = androidx.compose.ui.graphics.Path()
                
                for (i in 0..360 step 5) {
                    val angle = Math.toRadians(i.toDouble())
                    val waveOffset = sin(angle + phase.value + index * 0.5) * (8f + index * 4f)
                    val r = waveRadius + waveOffset.toFloat()
                    val x = centerX + (r * kotlin.math.cos(angle.toFloat()))
                    val y = centerY + (r * kotlin.math.sin(angle.toFloat()))
                    
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                
                drawPath(
                    path,
                    color = color.copy(alpha = 0.15f - index * 0.02f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun RealtimeStats(
    bytesReceived: Long,
    bytesSent: Long,
    connectionCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Received", formatBytes(bytesReceived), ExcapSuccess)
            StatItem("Sent", formatBytes(bytesSent), ExcapInfo)
            StatItem("Connections", connectionCount.toString(), ExcapCyan)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterChipRow() {
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf("All", "HTTP", "HTTPS", "DNS", "TCP", "UDP")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val selected = selectedFilter == filter
            FilterChip(
                selected = selected,
                onClick = { selectedFilter = if (selected) null else filter },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExcapBlue.copy(alpha = 0.2f),
                    selectedLabelColor = ExcapBlue,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

@Composable
fun StatusCard(isCapturing: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCapturing)
                ExcapSuccess.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCapturing) ExcapSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.5f
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isCapturing) "● Capturing network traffic" else "○ Capture stopped",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCapturing) ExcapSuccess else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> String.format("%.1f MB", bytes / 1_000_000.0)
        bytes >= 1_000 -> String.format("%.1f KB", bytes / 1_000.0)
        else -> "$bytes B"
    }
}
