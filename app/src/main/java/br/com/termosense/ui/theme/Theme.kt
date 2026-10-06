package br.com.termosense.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TermoSenseDarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = OnPrimaryCyan,
    primaryContainer = PrimaryCyanContainer,
    onPrimaryContainer = SurfaceContainerLowest,
    secondary = SecondaryAmber,
    onSecondary = OnSecondaryAmber,
    secondaryContainer = SecondaryAmberContainer,
    onSecondaryContainer = SurfaceContainerLowest,
    tertiary = TertiaryGreen,
    onTertiary = OnTertiaryGreen,
    tertiaryContainer = TertiaryGreenContainer,
    onTertiaryContainer = SurfaceContainerLowest,
    background = BackgroundDark,
    onBackground = TextOnSurface,
    surface = SurfaceDark,
    onSurface = TextOnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = TextOnSurfaceVariant,
    surfaceTint = PrimaryCyan,
    inverseSurface = TextOnSurface,
    inverseOnSurface = SurfaceContainerHigh,
    error = ErrorRed,
    onError = OnError,
    errorContainer = ErrorContainer,
    outline = OutlineGray,
    outlineVariant = OutlineVariant
)

private val TermoSenseLightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = TextOnSurfaceLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = TextOnSurfaceLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = TextOnSurfaceLight,
    background = BackgroundLight,
    onBackground = TextOnSurfaceLight,
    surface = SurfaceLight,
    onSurface = TextOnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = TextOnSurfaceVariantLight,
    surfaceTint = PrimaryLight,
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextOnSurface,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TermoSenseDarkColorScheme else TermoSenseLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = (if (darkTheme) SurfaceContainerLowest else SurfaceContainerLight).toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TermoSenseTypography,
        content = content
    )
}
