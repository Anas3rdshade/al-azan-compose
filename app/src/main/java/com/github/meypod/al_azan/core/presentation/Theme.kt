package com.github.meypod.al_azan.core.presentation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.github.meypod.al_azan.core.domain.model.settings.ThemeColor

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    surfaceTint = LightSurfaceTint,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

/** Redesign tokens Material's [ColorScheme] has no slot for (hero card, row card, muted, line, nav). */
@Immutable
data class ShadeColors(
    val accent: Color,
    val hero: Brush,
    val heroFg: Color,
    val card: Color,
    val muted: Color,
    val line: Color,
    val pill: Color,
    val navBg: Color,
)

// Light · Sukoon (sage) and Dark · Raat (night navy + gold); see Namaz Tracker/05_UI_Style_Options.html
private val SukoonColors = ShadeColors(
    accent = Color(0xFF5F8F7B),
    hero = Brush.linearGradient(listOf(Color(0xFFDFE9E0), Color(0xFFDFE9E0))),
    heroFg = Color(0xFF24312B),
    card = Color(0xFFFFFFFF),
    muted = Color(0xFFBFC8C0),
    line = Color(0xFFE3E8E2),
    pill = Color(0xFFE2EEE7),
    navBg = Color(0xFFFFFFFF),
)

private val RaatColors = ShadeColors(
    accent = Color(0xFFE3B75E),
    // 170deg CSS gradient: near-vertical, top slightly left
    hero = Brush.linearGradient(
        listOf(Color(0xFF1C2C4F), Color(0xFF0F1A30)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(180f, 1000f),
    ),
    heroFg = Color(0xFFF2EAD6),
    card = Color(0xFF152036),
    muted = Color(0xFF4A5878),
    line = Color(0xFF1F2B45),
    pill = Color(0xFF2A3550),
    navBg = Color(0xFF0F182B),
)

private fun ShadeColors.toScheme(dark: Boolean, bg: Color, fg: Color, subFg: Color): ColorScheme {
    val onAccent = if (dark) Color(0xFF1A1405) else Color.White
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = pill,
        onPrimaryContainer = fg,
        secondary = accent,
        onSecondary = onAccent,
        secondaryContainer = pill,
        onSecondaryContainer = fg,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = pill,
        onTertiaryContainer = fg,
        background = bg,
        onBackground = fg,
        surface = card,
        onSurface = fg,
        surfaceVariant = pill,
        onSurfaceVariant = subFg,
        surfaceTint = accent,
        outline = muted,
        outlineVariant = line,
        surfaceContainerLowest = card,
        surfaceContainerLow = card,
        surfaceContainer = card,
        surfaceContainerHigh = pill,
        surfaceContainerHighest = pill,
    )
}

private val SukoonColorScheme =
    SukoonColors.toScheme(dark = false, bg = Color(0xFFF2F4EF), fg = Color(0xFF2B3330), subFg = Color(0xFF5E6B64))
private val RaatColorScheme =
    RaatColors.toScheme(dark = true, bg = Color(0xFF0C1424), fg = Color(0xFFE8ECF4), subFg = Color(0xFF9AA6BF))

val LocalShadeColors = staticCompositionLocalOf { SukoonColors }

@Composable
fun AlAzanTheme(
    themeColor: ThemeColor = ThemeColor.Default,
    displayScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeColor) {
        ThemeColor.Light, ThemeColor.ClassicLight -> false
        ThemeColor.Dark, ThemeColor.ClassicDark -> true
        ThemeColor.Dynamic, ThemeColor.Default -> systemDarkTheme
    }
    // Classic and Dynamic are no longer offered; existing values fall back to the redesign palette.
    val colorScheme = if (darkTheme) RaatColorScheme else SukoonColorScheme
    val shadeColors = if (darkTheme) RaatColors else SukoonColors

    val activity = LocalActivity.current
    if (activity != null) {
        // keep status/navigation bar icon contrast in sync with the resolved app theme,
        // which can differ from the system dark-mode setting
        SideEffect {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val density = LocalDensity.current
    // Scale the dp density (not fontScale) so the whole UI — text, icons, spacing — zooms uniformly
    // by the slider. sp grows through density too, so text isn't scaled twice; the system font scale
    // stays in fontScale and still stacks on top.
    val scaledDensity = remember(density, displayScale) {
        Density(density.density * displayScale, density.fontScale)
    }

    // Persian/Arabic UI ship a bundled font whose metrics sit better with Latin than the system
    // Arabic fallback; other locales keep the default. Gated on the applied UI locale, not device.
    val typography = when (LocalConfiguration.current.locales[0].language) {
        "fa" -> VazirmatnTypography
        "ar" -> NotoSansArabicTypography
        else -> ManropeTypography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
    ) {
        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalShadeColors provides shadeColors,
            content = content,
        )
    }
}
