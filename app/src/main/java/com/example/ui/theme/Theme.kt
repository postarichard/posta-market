package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BrightPurpleLight,
    onPrimary = Color.White,
    primaryContainer = DeepViolet,
    onPrimaryContainer = LavenderContainer,
    secondary = BrightYellow,
    onSecondary = Color(0xFF1F1803),
    secondaryContainer = Color(0xFF423200),
    onSecondaryContainer = BrightYellowLight,
    tertiary = BrightPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF500724),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrightPurple,
    onPrimary = Color.White,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = DeepViolet,
    secondary = GoldenYellow,
    onSecondary = Color.White,
    secondaryContainer = BrightYellowContainer,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = BrightPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFCE7F3),
    onTertiaryContainer = Color(0xFF831843),
    background = LightLavenderBackground,
    onBackground = TextDeepPurple,
    surface = LightLavenderSurface,
    onSurface = TextDeepPurple,
    surfaceVariant = LavenderContainer,
    onSurfaceVariant = TextSecondaryDeep,
    outline = LavenderContainerBorder,
  )

@Composable
fun PostaMarketTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep Posta Market's signature vibrant brand palette
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = PostaMarketTheme(darkTheme, dynamicColor, content)
