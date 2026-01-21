package de.armando.kalorientracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val OceanColorScheme = lightColorScheme(
    primary = OceanBlue,
    secondary = OceanCyan,
    tertiary = OceanSand,
    background = Color(0xFFF8FEFF),
    surface = Color(0xFFF8FEFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = OceanDark,
    onBackground = Color(0xFF191C1D),
    onSurface = Color(0xFF191C1D),
    primaryContainer = OceanBlue,
    onPrimaryContainer = Color.White
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestLightGreen,
    secondary = ForestBrown,
    tertiary = ForestGreen,
    background = Color(0xFF151111),
    surface = Color(0xFF151111),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = ForestDark,
    onBackground = Color(0xFFE2DEDE),
    onSurface = Color(0xFFE2DEDE),
    primaryContainer = ForestGreen,
    onPrimaryContainer = Color.Black
)

val AppThemes = mapOf(
    "Default" to LightColorScheme,
    "Ocean" to OceanColorScheme,
    "Dark Forest" to ForestColorScheme,
    "Dark Purple" to DarkColorScheme
)


@Composable
fun KalorientrackerTheme(
    themeName: String = "Default",
    content: @Composable () -> Unit
) {
    val colorScheme = AppThemes[themeName] ?: LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Adjust based on theme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}