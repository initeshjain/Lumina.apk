package com.swipegallery.app

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest

// Standardized color palette
internal val GreenPrimary = Color(0xFF1B6C53)
internal val GreenDark = Color(0xFF4FD8A2)
// User requirement: "red color is kind of off, use correct shade."
internal val RedStandard = Color(0xFFD32F2F)
// User requirement: "And for restore button, use black color only."
internal val RestoreBlack = Color(0xFF000000)

data class AppColors(
    val canvas: Color,
    val card: Color,
    val cardSecondary: Color,
    val ink: Color,
    val muted: Color,
    val green: Color,
    val red: Color,
    val restoreBlack: Color,
    val border: Color,
    val chipBackground: Color,
    val isDark: Boolean,
)

private val LightAppColors = AppColors(
    canvas = Color(0xFFF7F8FA),
    card = Color(0xFFFFFFFF),
    cardSecondary = Color(0xFFF1F3F5),
    ink = Color(0xFF1A1C1E),
    muted = Color(0xFF72777F),
    green = GreenPrimary,
    red = RedStandard,
    restoreBlack = RestoreBlack,
    border = Color(0xFFE3E5E9),
    chipBackground = Color(0xFFE9ECEF),
    isDark = false
)

private val DarkAppColors = AppColors(
    canvas = Color(0xFF111315),
    card = Color(0xFF1E2124),
    cardSecondary = Color(0xFF2A2D32),
    ink = Color(0xFFE3E5E8),
    muted = Color(0xFF9197A2),
    green = GreenDark,
    red = Color(0xFFEF5350),
    restoreBlack = RestoreBlack,
    border = Color(0xFF2E3238),
    chipBackground = Color(0xFF282B30),
    isDark = true
)

internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object GalleryThemeTokens {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// Fallback direct references that resolve based on theme context when possible
internal val Ink: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.ink
internal val Muted: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.muted
internal val Canvas: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.canvas
internal val Green: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.green
internal val Coral: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.red
internal val Card: Color @Composable @ReadOnlyComposable get() = LocalAppColors.current.card

@Composable
fun GalleryTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = appColors.green,
            background = appColors.canvas,
            surface = appColors.card,
            surfaceVariant = appColors.cardSecondary,
            onBackground = appColors.ink,
            onSurface = appColors.ink,
            error = appColors.red
        )
    } else {
        lightColorScheme(
            primary = appColors.green,
            background = appColors.canvas,
            surface = appColors.card,
            surfaceVariant = appColors.cardSecondary,
            onBackground = appColors.ink,
            onSurface = appColors.ink,
            error = appColors.red
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            window.statusBarColor = appColors.canvas.toArgb()
            window.navigationBarColor = appColors.canvas.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

@Composable
internal fun GalleryImage(
    model: Any,
    description: String,
    modifier: Modifier = Modifier,
    scale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val request = remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(180)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = description,
        modifier = modifier,
        contentScale = scale
    )
}
