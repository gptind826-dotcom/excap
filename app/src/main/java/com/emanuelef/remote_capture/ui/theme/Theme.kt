package com.emanuelef.remote_capture.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/* eXcap Material 3 Theme
 * Dark-first design with dynamic color support on Android 12+
 * Built by eXU CODER
 */

private val DarkColorScheme = darkColorScheme(
    primary = ExcapBlue,
    onPrimary = ExcapOnPrimary,
    primaryContainer = ExcapTealDark,
    onPrimaryContainer = ExcapBlueLight,
    secondary = ExcapTeal,
    onSecondary = ExcapOnBackground,
    secondaryContainer = ExcapTealDark,
    onSecondaryContainer = ExcapTealLight,
    tertiary = ExcapCyan,
    onTertiary = ExcapOnPrimary,
    tertiaryContainer = ExcapCyanDark,
    onTertiaryContainer = ExcapCyanLight,
    background = ExcapBackground,
    onBackground = ExcapOnBackground,
    surface = ExcapSurface,
    onSurface = ExcapOnSurface,
    surfaceVariant = ExcapSurfaceVariant,
    onSurfaceVariant = ExcapOnSurfaceVariant,
    surfaceTint = ExcapBlue,
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF0F172A),
    inversePrimary = ExcapBlueDark,
    error = ExcapError,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    scrim = Color.Black,
)

private val LightColorScheme = lightColorScheme(
    primary = ExcapBlueDark,
    onPrimary = Color.White,
    primaryContainer = ExcapBlueLight,
    onPrimaryContainer = ExcapOnPrimary,
    secondary = ExcapTeal,
    onSecondary = Color.White,
    secondaryContainer = ExcapTealLight,
    onSecondaryContainer = ExcapTealDark,
    tertiary = ExcapCyanDark,
    onTertiary = Color.White,
    tertiaryContainer = ExcapCyanLight,
    onTertiaryContainer = ExcapCyanDark,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = ExcapBlueDark,
    inverseSurface = Color(0xFF1E293B),
    inverseOnSurface = Color(0xFFF1F5F9),
    inversePrimary = ExcapBlue,
    error = ExcapError,
    onError = Color.White,
    errorContainer = Color(0xFFFECACA),
    onErrorContainer = Color(0xFF7F1D1D),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color.Black,
)

@Composable
fun ExcapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExcapTypography,
        content = content
    )
}
