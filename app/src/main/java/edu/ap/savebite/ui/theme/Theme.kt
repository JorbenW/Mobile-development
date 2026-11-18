package edu.ap.savebite.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// shortcut: light only, dark mode comes with the house style polish on 16 December
private val LightColorScheme = lightColorScheme(
    primary = Tomato,
    onPrimary = Color.White,
    // The container colors are used by dialogs, menus and pickers;
    // without them Material falls back on its default purple
    primaryContainer = Mustard,
    onPrimaryContainer = Ink,
    secondary = Mustard,
    onSecondary = Ink,
    secondaryContainer = Mustard,
    onSecondaryContainer = Ink,
    tertiary = Mustard,
    onTertiary = Ink,
    tertiaryContainer = Mustard,
    onTertiaryContainer = Ink,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Cream,
    surfaceContainer = Cream,
    surfaceContainerHigh = Cream,
    surfaceContainerHighest = CreamDark,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Color.White,
    onSurfaceVariant = Slate,
    outline = Ink,
    error = Tomato,
    onError = Color.White,
    errorContainer = ErrorBackground,
    onErrorContainer = ErrorText
)

private val SaveBiteShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun SaveBiteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = SaveBiteShapes,
        content = content
    )
}
