package jp.aoto.zerosum.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Procedural neon palette shared by every screen. */
public object Palette {
    public val Background: Color = Color(0xFF080D18)
    public val Surface: Color = Color(0xFF111A2B)
    public val Cyan: Color = Color(0xFF55E6FF)
    public val Magenta: Color = Color(0xFFFF5DB1)
    public val Amber: Color = Color(0xFFFFC857)
    public val Green: Color = Color(0xFF6EF2A5)
    public val Red: Color = Color(0xFFFF6577)
    public val Text: Color = Color(0xFFE8F1FF)
    public val Muted: Color = Color(0xFF8391A8)
}

/** Fixed dark theme required by the product specification. */
@Composable
public fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.Cyan,
            secondary = Palette.Magenta,
            background = Palette.Background,
            surface = Palette.Surface,
            error = Palette.Red,
            onBackground = Palette.Text,
            onSurface = Palette.Text,
        ),
        content = content,
    )
}
