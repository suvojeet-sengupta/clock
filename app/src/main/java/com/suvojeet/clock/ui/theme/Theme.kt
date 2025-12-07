package com.suvojeet.clock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.suvojeet.clock.data.settings.AppTheme

// Cosmic Theme (Default)
private val CosmicColorScheme = darkColorScheme(
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

// Ocean Theme
private val OceanColorScheme = darkColorScheme(
    primary = Color(0xFF00ACC1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00ACC1).copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF00ACC1),
    
    secondary = Color(0xFF26C6DA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF26C6DA).copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFF26C6DA),
    
    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4DD0E1).copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFF4DD0E1),
    
    background = Color(0xFF0D1B2A),
    onBackground = Color.White,
    
    surface = Color(0xFF1B263B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C3E50),
    onSurfaceVariant = Color.White,
    
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

// Forest Theme
private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4CAF50).copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF4CAF50),
    
    secondary = Color(0xFF81C784),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF81C784).copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFF81C784),
    
    tertiary = Color(0xFFA5D6A7),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA5D6A7).copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFFA5D6A7),
    
    background = Color(0xFF0D1912),
    onBackground = Color.White,
    
    surface = Color(0xFF1A2E1C),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2E4A30),
    onSurfaceVariant = Color.White,
    
    error = Color(0xFFFF6B6B),
    onError = Color.White
)

// Sunset Theme
private val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFFF7043),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFF7043).copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFFFF7043),
    
    secondary = Color(0xFFFFB74D),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFB74D).copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFFFFB74D),
    
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFD54F).copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFFFFD54F),
    
    background = Color(0xFF1A1010),
    onBackground = Color.White,
    
    surface = Color(0xFF2D1B1B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF4A2C2C),
    onSurfaceVariant = Color.White,
    
    error = Color(0xFFEF5350),
    onError = Color.White
)

// Midnight Theme
private val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFF9E9E9E),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF9E9E9E).copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF9E9E9E),
    
    secondary = Color(0xFFBDBDBD),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFBDBDBD).copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFFBDBDBD),
    
    tertiary = Color(0xFFE0E0E0),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFE0E0E0).copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFFE0E0E0),
    
    background = Color(0xFF000000),
    onBackground = Color.White,
    
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color.White,
    
    error = Color(0xFFCF6679),
    onError = Color.Black
)

// Smooth Theme
private val SmoothColorScheme = darkColorScheme(
    primary = SmoothPrimary,
    onPrimary = SmoothBackground,
    primaryContainer = SmoothPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = SmoothPrimary,

    secondary = SmoothSecondary,
    onSecondary = SmoothBackground,
    secondaryContainer = SmoothSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = SmoothSecondary,

    tertiary = SmoothTertiary,
    onTertiary = SmoothBackground,
    tertiaryContainer = SmoothTertiary.copy(alpha = 0.2f),
    onTertiaryContainer = SmoothTertiary,

    background = SmoothBackground,
    onBackground = StarlightWhite, // Keep text white/light

    surface = SmoothSurface,
    onSurface = StarlightWhite,
    surfaceVariant = SmoothSurface, // Or slightly different if needed
    onSurfaceVariant = StarlightWhite,

    error = SmoothError,
    onError = SmoothBackground
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

/**
 * Get the color scheme for the specified theme
 */
fun getColorSchemeForTheme(theme: AppTheme): ColorScheme {
    return when (theme) {
        AppTheme.COSMIC -> CosmicColorScheme
        AppTheme.OCEAN -> OceanColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.SUNSET -> SunsetColorScheme
        AppTheme.MIDNIGHT -> MidnightColorScheme
        AppTheme.SMOOTH -> SmoothColorScheme
    }
}

@Composable
fun CosmicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.SMOOTH,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color to enforce Cosmic theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getColorSchemeForTheme(appTheme)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}