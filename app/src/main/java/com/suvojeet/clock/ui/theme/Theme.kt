package com.suvojeet.clock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryExpressive,
    onPrimary = StarlightWhite,
    primaryContainer = PrimaryExpressive.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryExpressive,
    
    secondary = SecondaryExpressive,
    onSecondary = StarlightWhite,
    secondaryContainer = SecondaryExpressive.copy(alpha = 0.2f),
    onSecondaryContainer = SecondaryExpressive,
    
    tertiary = TertiaryExpressive,
    onTertiary = StarlightWhite,
    tertiaryContainer = TertiaryExpressive.copy(alpha = 0.2f),
    onTertiaryContainer = TertiaryExpressive,
    
    background = DeepSpaceBlack,
    onBackground = StarlightWhite,
    
    surface = SurfaceDark,
    onSurface = StarlightWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = StarlightWhite,
    
    error = ErrorExpressive,
    onError = StarlightWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryExpressive,
    secondary = SecondaryExpressive,
    tertiary = TertiaryExpressive
    // Ideally we would define a full light theme, but Cosmic is dark-first.
)

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp) // Expressive large corners
)

@Composable
fun CosmicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to enforce Cosmic theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Force dark theme for Cosmic look
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}