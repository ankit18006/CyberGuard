package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryRed,
    secondary = AccentRed,
    tertiary = SuspiciousColor,
    background = DarkBackground,
    surface = DeepSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = LightSlate,
    onSurface = LightSlate
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryRed,
    secondary = AccentRed,
    tertiary = SuspiciousColor,
    background = androidx.compose.ui.graphics.Color(0xFFFAFAFA),
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = androidx.compose.ui.graphics.Color(0xFF1E293B),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1E293B)
  )

private val HighContrastColorScheme =
  darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFFFFF00), // High Contrast Yellow
    secondary = androidx.compose.ui.graphics.Color(0xFF00FFFF), // High Contrast Cyan
    tertiary = androidx.compose.ui.graphics.Color.White,
    background = androidx.compose.ui.graphics.Color.Black,
    surface = androidx.compose.ui.graphics.Color(0xFF121212),
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    outline = androidx.compose.ui.graphics.Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  isHighContrast: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      isHighContrast -> HighContrastColorScheme
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
