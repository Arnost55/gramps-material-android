package app.grampsmaterial.core_ui.theme

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

private val Teal = Color(0xFF67D5C2)
private val TealDark = Color(0xFF006B5D)
private val Cyan = Color(0xFF6FD8F5)
private val Charcoal = Color(0xFF101515)
private val CharcoalSurface = Color(0xFF171D1C)
private val CharcoalVariant = Color(0xFF263230)

private val GrampsDarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = Color(0xFF8FF8E3),
    secondary = Cyan,
    onSecondary = Color(0xFF003640),
    secondaryContainer = Color(0xFF104C59),
    onSecondaryContainer = Color(0xFFB8EFFF),
    tertiary = Color(0xFFFFD18B),
    onTertiary = Color(0xFF462A00),
    tertiaryContainer = Color(0xFF624000),
    onTertiaryContainer = Color(0xFFFFDEA9),
    background = Charcoal,
    onBackground = Color(0xFFE0E7E4),
    surface = CharcoalSurface,
    onSurface = Color(0xFFE0E7E4),
    surfaceVariant = CharcoalVariant,
    onSurfaceVariant = Color(0xFFBFCBC7),
    outline = Color(0xFF899590)
)

private val GrampsLightColors = lightColorScheme(
    primary = TealDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8FF8E3),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF006679),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFADECFD),
    onSecondaryContainer = Color(0xFF001F27),
    tertiary = Color(0xFF805600),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDEA9),
    onTertiaryContainer = Color(0xFF291800),
    background = Color(0xFFF8FBF8),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFF8FBF8),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDCE5E1),
    onSurfaceVariant = Color(0xFF404945)
)

@Composable
fun GrampsMaterialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseColors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> GrampsDarkColors
        else -> GrampsLightColors
    }
    val colors = if (darkTheme && amoledMode) {
        baseColors.copy(background = Color.Black, surface = Color.Black)
    } else {
        baseColors
    }

    MaterialTheme(colorScheme = colors, content = content)
}
