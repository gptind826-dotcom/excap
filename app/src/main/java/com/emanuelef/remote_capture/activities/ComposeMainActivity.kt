package com.emanuelef.remote_capture.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.emanuelef.remote_capture.CaptureService
import com.emanuelef.remote_capture.R
import com.emanuelef.remote_capture.model.AppState
import com.emanuelef.remote_capture.ui.screens.CaptureScreen
import com.emanuelef.remote_capture.ui.screens.ConnectionsScreen
import com.emanuelef.remote_capture.ui.screens.SettingsScreen
import com.emanuelef.remote_capture.ui.screens.StatsScreen
import com.emanuelef.remote_capture.ui.theme.ExcapTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/* eXcap Main Activity - Jetpack Compose Entry Point
 * Built by eXU CODER
 * Bottom navigation with Capture, Connections, Stats, Settings
 */

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Capture : Screen("capture", "Capture",
        Icons.Filled.NetworkCheck, Icons.Outlined.NetworkCheck)
    object Connections : Screen("connections", "Connections",
        Icons.Filled.SwapVert, Icons.Outlined.SwapVert)
    object Stats : Screen("stats", "Stats",
        Icons.Filled.ShowChart, Icons.Outlined.ShowChart)
    object Settings : Screen("settings", "Settings",
        Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(Screen.Capture, Screen.Connections, Screen.Stats, Screen.Settings)

class ComposeMainActivity : ComponentActivity() {

    companion object {
        private val _appState = MutableStateFlow(AppState.idle)
        val appStateFlow: StateFlow<AppState> = _appState

        private val _connectionCount = MutableStateFlow(0)
        val connectionCountFlow: StateFlow<Int> = _connectionCount
    }

    private lateinit var vpnLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register for VPN permission result
        vpnLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                startCaptureService()
            }
        }

        // Register for notification permission (Android 13+)
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* handled */ }

        setContent {
            ExcapTheme {
                ExcapApp(
                    onToggleCapture = { toggleCapture() },
                    onRequestVpnPermission = { requestVpnPermission() }
                )
            }
        }

        // Register for capture state broadcasts
        val filter = IntentFilter(CaptureService.ACTION_STATE_CHANGED)
        LocalBroadcastManager.getInstance(this).registerReceiver(stateReceiver, filter)

        // Check initial state
        _appState.value = CaptureService.getStatus()

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver)
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            _appState.value = CaptureService.getStatus()
        }
    }

    private fun toggleCapture() {
        when (CaptureService.getStatus()) {
            AppState.running -> {
                CaptureService.stopService(this)
            }
            AppState.starting -> {
                // Wait
            }
            else -> {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    vpnLauncher.launch(intent)
                } else {
                    startCaptureService()
                }
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnLauncher.launch(intent)
        }
    }

    private fun startCaptureService() {
        CaptureService.startService(this, Build.VERSION.SDK_INT)
    }
}

@Composable
fun ExcapApp(
    onToggleCapture: () -> Unit,
    onRequestVpnPermission: () -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavigationHost(
            navController = navController,
            paddingValues = paddingValues,
            onToggleCapture = onToggleCapture,
            onRequestVpnPermission = onRequestVpnPermission,
            onHapticFeedback = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onToggleCapture: () -> Unit,
    onRequestVpnPermission: () -> Unit,
    onHapticFeedback: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Capture.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(300))
            }
        ) {
            composable(Screen.Capture.route) {
                CaptureScreen(
                    onToggleCapture = onToggleCapture,
                    onHapticFeedback = onHapticFeedback
                )
            }
            composable(Screen.Connections.route) {
                ConnectionsScreen()
            }
            composable(Screen.Stats.route) {
                StatsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
