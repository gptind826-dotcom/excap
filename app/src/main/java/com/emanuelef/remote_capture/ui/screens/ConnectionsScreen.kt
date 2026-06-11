package com.emanuelef.remote_capture.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emanuelef.remote_capture.CaptureService
import com.emanuelef.remote_capture.ConnectionsRegister
import com.emanuelef.remote_capture.model.ConnectionDescriptor
import com.emanuelef.remote_capture.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/* eXcap Connections Screen
 * Built by eXU CODER
 * Searchable, filterable connection list with swipeable cards
 */

@Composable
fun ConnectionsScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedProtocol by remember { mutableStateOf<String?>(null) }
    var connections by remember { mutableStateOf<List<ConnectionDescriptor>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Poll for connections
    LaunchedEffect(Unit) {
        while (true) {
            val register = CaptureService.getConnsRegister()
            register?.let {
                connections = it.getAllConnections().toList()
            }
            delay(1000)
        }
    }

    // Filter connections
    val filteredConnections = remember(connections, searchQuery, selectedProtocol) {
        connections.filter { conn ->
            val matchesSearch = searchQuery.isEmpty() ||
                conn.info.lowercase().contains(searchQuery.lowercase()) ||
                conn.dst_ip.lowercase().contains(searchQuery.lowercase()) ||
                conn.l7proto.lowercase().contains(searchQuery.lowercase())
            
            val matchesProtocol = selectedProtocol == null ||
                conn.l7proto.equals(selectedProtocol, ignoreCase = true)
            
            matchesSearch && matchesProtocol
        }.take(500) // Limit for performance
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Connections",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search by host, IP, protocol...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = ExcapBlue,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { })
        )

        // Protocol Filter Chips
        ProtocolFilterChips(
            selectedProtocol = selectedProtocol,
            onProtocolSelected = { selectedProtocol = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Connection count
        Text(
            text = "${filteredConnections.size} connections${if (connections.size > 500) " (showing top 500)" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Connection List
        if (filteredConnections.isEmpty()) {
            EmptyConnectionsState(searchQuery.isNotEmpty())
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredConnections,
                    key = { it.incr_id }
                ) { connection ->
                    ConnectionCard(connection = connection)
                }
            }
        }
    }
}

@Composable
fun ProtocolFilterChips(
    selectedProtocol: String?,
    onProtocolSelected: (String?) -> Unit
) {
    val protocols = listOf("HTTP", "HTTPS", "DNS", "TCP", "UDP", "QUIC", "TLS")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedProtocol == null,
            onClick = { onProtocolSelected(null) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ExcapBlue.copy(alpha = 0.2f),
                selectedLabelColor = ExcapBlue
            )
        )
        protocols.forEach { protocol ->
            val selected = selectedProtocol == protocol
            FilterChip(
                selected = selected,
                onClick = { onProtocolSelected(if (selected) null else protocol) },
                label = { Text(protocol) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = getProtocolColor(protocol).copy(alpha = 0.2f),
                    selectedLabelColor = getProtocolColor(protocol)
                )
            )
        }
    }
}

@Composable
fun ConnectionCard(connection: ConnectionDescriptor) {
    val protocolColor = getProtocolColor(connection.l7proto)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Protocol indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(protocolColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = connection.l7proto.take(2).uppercase(),
                    color = protocolColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Connection info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.info.ifEmpty { connection.dst_ip },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${connection.dst_ip}:${connection.dst_port} • ${connection.l7proto}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Data transferred
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatBytes(connection.rcvd_bytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ExcapSuccess,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatBytes(connection.sent_bytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ExcapInfo,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyConnectionsState(hasSearchQuery: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SwapVert,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasSearchQuery) "No matching connections" else "No connections yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (hasSearchQuery) "Try a different search term" else "Start capture to see network connections",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun getProtocolColor(protocol: String): Color {
    return when (protocol.uppercase()) {
        "HTTP" -> ExcapHttp
        "HTTPS" -> ExcapHttps
        "DNS" -> ExcapDns
        "TCP" -> ExcapTcp
        "UDP" -> ExcapUdp
        "QUIC" -> Color(0xFFFF6D00)
        "TLS" -> Color(0xFFAB47BC)
        else -> ExcapBlue
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
