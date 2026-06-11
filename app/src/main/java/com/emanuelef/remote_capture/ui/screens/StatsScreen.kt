package com.emanuelef.remote_capture.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emanuelef.remote_capture.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/* eXcap Stats Screen
 * Built by eXU CODER
 * Animated donut charts, bandwidth graph, top domains and apps
 */

@Composable
fun StatsScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Protocol Distribution Donut Chart
        ProtocolDistributionCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Bandwidth Graph
        BandwidthGraphCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Top Domains
        TopDomainsCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Top Apps
        TopAppsCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProtocolDistributionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Protocol Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedDonutChart(
                    data = listOf(
                        DonutSegment("HTTPS", 45f, ExcapHttps),
                        DonutSegment("HTTP", 20f, ExcapHttp),
                        DonutSegment("DNS", 15f, ExcapDns),
                        DonutSegment("TCP", 12f, ExcapTcp),
                        DonutSegment("UDP", 8f, ExcapUdp)
                    ),
                    modifier = Modifier.size(180.dp)
                )

                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "1.2K",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "packets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProtocolLegend("HTTPS", ExcapHttps, "45%")
                ProtocolLegend("HTTP", ExcapHttp, "20%")
                ProtocolLegend("DNS", ExcapDns, "15%")
                ProtocolLegend("TCP", ExcapTcp, "12%")
                ProtocolLegend("UDP", ExcapUdp, "8%")
            }
        }
    }
}

data class DonutSegment(val label: String, val percentage: Float, val color: Color)

@Composable
fun AnimatedDonutChart(
    data: List<DonutSegment>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.percentage.toDouble() }.toFloat()
    val animatedProgress by rememberInfiniteTransition(label = "chart").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = (canvasWidth.coerceAtMost(canvasHeight) / 2) * 0.8f
        val strokeWidth = radius * 0.25f

        var startAngle = -90f

        data.forEach { segment ->
            val sweepAngle = (segment.percentage / total) * 360f * animatedProgress

            drawArc(
                color = segment.color.copy(alpha = 0.8f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun ProtocolLegend(label: String, color: Color, percentage: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = percentage,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BandwidthGraphCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bandwidth Over Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    StatusDot(color = ExcapSuccess, label = "RX")
                    Spacer(modifier = Modifier.width(12.dp))
                    StatusDot(color = ExcapInfo, label = "TX")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Animated bandwidth graph
            AnimatedBandwidthGraph(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
fun AnimatedBandwidthGraph(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bandwidth")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Generate sample data points
    val dataPoints = remember { List(50) { kotlin.random.Random.nextFloat() * 0.8f + 0.1f } }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val pointWidth = width / (dataPoints.size - 1)

        // Draw RX line (green)
        for (i in 0 until dataPoints.size - 1) {
            val progress = (animationProgress * dataPoints.size).toInt()
            if (i > progress) break

            val x1 = i * pointWidth
            val x2 = (i + 1) * pointWidth
            val y1 = height - (dataPoints[i] * height * 0.8f)
            val y2 = height - (dataPoints[i + 1] * height * 0.8f)

            drawLine(
                color = ExcapSuccess.copy(alpha = 0.6f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Draw TX line (blue)
        val txPoints = dataPoints.map { it * 0.6f }
        for (i in 0 until txPoints.size - 1) {
            val progress = (animationProgress * txPoints.size).toInt()
            if (i > progress) break

            val x1 = i * pointWidth
            val x2 = (i + 1) * pointWidth
            val y1 = height - (txPoints[i] * height * 0.6f)
            val y2 = height - (txPoints[i + 1] * height * 0.6f)

            drawLine(
                color = ExcapInfo.copy(alpha = 0.6f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun StatusDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TopDomainsCard() {
    val domains = listOf(
        "google.com" to "245 MB",
        "github.com" to "128 MB",
        "cdn.cloudflare.com" to "89 MB",
        "api.example.com" to "45 MB",
        "fonts.googleapis.com" to "23 MB"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Top Domains",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            domains.forEachIndexed { index, (domain, data) ->
                DomainRow(rank = index + 1, domain = domain, data = data)
                if (index < domains.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DomainRow(rank: Int, domain: String, data: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = ExcapBlue,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.Language,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = domain,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = data,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ExcapCyan
        )
    }
}

@Composable
fun TopAppsCard() {
    val apps = listOf(
        "Chrome" to "456 MB",
        "YouTube" to "234 MB",
        "WhatsApp" to "123 MB",
        "Instagram" to "98 MB",
        "Spotify" to "67 MB"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Top Apps by Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            apps.forEachIndexed { index, (app, data) ->
                AppDataRow(rank = index + 1, appName = app, data = data)
                if (index < apps.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AppDataRow(rank: Int, appName: String, data: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            modifier = Modifier.width(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = ExcapTeal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ExcapTeal.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1),
                color = ExcapTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = appName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = data,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ExcapCyan
        )
    }
}
