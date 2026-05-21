package com.nlespam.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import com.nlespam.ui.ThemeColor

// Exposed so Settings screen can toggle it
val LocalThemeColor = compositionLocalOf { ThemeColor.DYNAMIC }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NleSpamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.DYNAMIC,
    useOled: Boolean = false,
    content: @Composable () -> Unit
) {
    val isOled = darkTheme && useOled

    // --- Compute base color scheme ---
    var colorScheme = when {
        themeColor == ThemeColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val schemePrimary: Color
            val schemeSecondary: Color
            val schemeTertiary: Color

            when (themeColor) {
                ThemeColor.BLUE -> {
                    schemePrimary = Color(0xFFAEC6FF)
                    schemeSecondary = Color(0xFFBCC7DB)
                    schemeTertiary = Color(0xFFDABCE2)
                }
                ThemeColor.RED -> {
                    schemePrimary = Color(0xFFFFB4AB)
                    schemeSecondary = Color(0xFFE7BDB8)
                    schemeTertiary = Color(0xFFFFD9A5)
                }
                ThemeColor.GREEN -> {
                    schemePrimary = Color(0xFF97D7A5)
                    schemeSecondary = Color(0xFFB2CDB8)
                    schemeTertiary = Color(0xFF99D0C5)
                }
                ThemeColor.PURPLE -> {
                    schemePrimary = Color(0xFFD0BCFF)
                    schemeSecondary = Color(0xFFCBC2DB)
                    schemeTertiary = Color(0xFFEFB8C8)
                }
                else -> { // Cyan default
                    schemePrimary = PrimaryDark
                    schemeSecondary = SecondaryDark
                    schemeTertiary = TertiaryDark
                }
            }

            if (darkTheme) {
                darkColorScheme(
                    primary = schemePrimary,
                    onPrimary = Color(0xFF000000),
                    primaryContainer = schemePrimary.copy(alpha = 0.3f),
                    onPrimaryContainer = schemePrimary,
                    secondary = schemeSecondary,
                    onSecondary = Color(0xFF000000),
                    secondaryContainer = schemeSecondary.copy(alpha = 0.3f),
                    onSecondaryContainer = schemeSecondary,
                    tertiary = schemeTertiary,
                    onTertiary = Color(0xFF000000),
                    tertiaryContainer = schemeTertiary.copy(alpha = 0.3f),
                    onTertiaryContainer = schemeTertiary,
                    surface = Color(0xFF141414),
                    onSurface = Color(0xFFE0E0E0),
                    surfaceVariant = Color(0xFF1E1E1E),
                    onSurfaceVariant = Color(0xFFA0A0A0),
                    surfaceContainerLowest = Color(0xFF0C0C0C),
                    surfaceContainerLow = Color(0xFF1A1A1A),
                    surfaceContainer = Color(0xFF1E1E1E),
                    surfaceContainerHigh = Color(0xFF242424),
                    surfaceContainerHighest = Color(0xFF2A2A2A),
                    background = Color(0xFF141414),
                    onBackground = Color(0xFFE0E0E0),
                    outline = Color(0xFF555555),
                    outlineVariant = Color(0xFF333333)
                )
            } else {
                lightColorScheme(
                    primary = schemePrimary,
                    secondary = schemeSecondary,
                    tertiary = schemeTertiary,
                    surface = Color(0xFFF8F9FA),
                    surfaceVariant = Color(0xFFE9ECEF),
                    surfaceContainerLowest = Color(0xFFFFFFFF),
                    surfaceContainerLow = Color(0xFFF1F3F5),
                    surfaceContainer = Color(0xFFE9ECEF),
                    surfaceContainerHigh = Color(0xFFDEE2E6),
                    surfaceContainerHighest = Color(0xFFCED4DA),
                    background = Color(0xFFF8F9FA),
                )
            }
        }
    }

    // --- Apply OLED overrides AFTER color scheme is built (works for Dynamic too) ---
    if (isOled) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF111111),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color(0xFF161616),
            surfaceContainerHighest = Color(0xFF202020),
        )
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = NleSpamShapes,
        typography = NleSpamTypography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
