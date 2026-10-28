package com.kshetrajna.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KshetrajnaLightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = PureWhite,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = WarmIvory,
    secondary = MutedSage,
    onSecondary = PureWhite,
    secondaryContainer = WarmIvoryContainer,
    onSecondaryContainer = ForestGreenDark,
    tertiary = WarmGold,
    onTertiary = Charcoal,
    tertiaryContainer = WarmGoldLight,
    onTertiaryContainer = ForestGreenDark,
    background = WarmIvory,
    onBackground = Charcoal,
    surface = WarmIvorySurface,
    onSurface = Charcoal,
    surfaceVariant = WarmIvoryContainer,
    onSurfaceVariant = CharcoalMuted,
    outline = OutlineBorder,
    outlineVariant = OutlineBorderLight
)

private val KshetrajnaDarkColorScheme = darkColorScheme(
    primary = WarmGold,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = WarmIvory,
    secondary = MutedSage,
    onSecondary = ForestGreenDark,
    secondaryContainer = ForestGreen,
    onSecondaryContainer = WarmIvory,
    tertiary = WarmGoldLight,
    onTertiary = ForestGreenDark,
    background = ForestGreenDark,
    onBackground = WarmIvory,
    surface = ForestGreen,
    onSurface = WarmIvory,
    surfaceVariant = ForestGreenContainer,
    onSurfaceVariant = MutedSageLight,
    outline = ForestGreenLight,
    outlineVariant = ForestGreenContainer
)

@Composable
fun KshetrajnaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We strictly use the Kshetrajna exact brand palette rather than system dynamic colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KshetrajnaDarkColorScheme else KshetrajnaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
