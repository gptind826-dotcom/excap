package com.emanuelef.remote_capture.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emanuelef.remote_capture.ui.theme.*

/* eXcap Settings Screen
 * Built by eXU CODER
 * Grouped preference categories with toggle switches and explanations
 */

@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Capture Settings Group
        SettingsGroup(title = "Capture") {
            SettingsToggleItem(
                icon = Icons.Outlined.VpnKey,
                title = "Root Capture",
                subtitle = "Allows eXcap to run alongside other VPN apps. Requires root access.",
                defaultChecked = false
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Lock,
                title = "TLS Decryption",
                subtitle = "Decrypt HTTPS traffic using user-installed CA certificate via mitmproxy addon.",
                defaultChecked = false
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Dns,
                title = "DNS Logging",
                subtitle = "Log all DNS queries and responses during capture.",
                defaultChecked = true
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Save,
                title = "Auto-save PCAP",
                subtitle = "Automatically save captured traffic to PCAP files.",
                defaultChecked = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Settings Group
        SettingsGroup(title = "Filtering") {
            SettingsToggleItem(
                icon = Icons.Outlined.FilterAlt,
                title = "App Filter",
                subtitle = "Only capture traffic from selected applications.",
                defaultChecked = false
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Block,
                title = "Firewall Mode",
                subtitle = "Block connections matching filter rules.",
                defaultChecked = false
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Translate,
                title = "IP Geolocation",
                subtitle = "Show country information for remote IP addresses. Requires GeoIP database.",
                defaultChecked = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export Settings Group
        SettingsGroup(title = "Export") {
            SettingsActionItem(
                icon = Icons.Outlined.FileDownload,
                title = "Export CA Certificate",
                subtitle = "Export the eXcap CA certificate for TLS decryption setup."
            )
            SettingsActionItem(
                icon = Icons.Outlined.Share,
                title = "Share PCAP File",
                subtitle = "Share the last captured PCAP file."
            )
            SettingsToggleItem(
                icon = Icons.Outlined.Http,
                title = "HTTP Server",
                subtitle = "Enable HTTP server to download PCAP files over the network.",
                defaultChecked = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Group
        SettingsGroup(title = "About") {
            SettingsInfoItem(
                icon = Icons.Outlined.Info,
                title = "eXcap Version",
                value = "2.0.0"
            )
            SettingsInfoItem(
                icon = Icons.Outlined.Code,
                title = "Built by",
                value = "eXU CODER"
            )
            SettingsActionItem(
                icon = Icons.Outlined.Description,
                title = "Open Source Licenses",
                subtitle = "View third-party library licenses."
            )
            SettingsActionItem(
                icon = Icons.Outlined.HelpOutline,
                title = "User Guide",
                subtitle = "Open the eXcap documentation."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = ExcapBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    defaultChecked: Boolean
) {
    var checked by remember { mutableStateOf(defaultChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { checked = !checked }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = ExcapBlue,
                checkedTrackColor = ExcapBlue.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = "Open",
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ExcapBlue
        )
    }
}
