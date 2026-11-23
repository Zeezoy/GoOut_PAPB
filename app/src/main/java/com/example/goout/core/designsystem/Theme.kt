package com.example.goout.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.goout.domain.model.DensityStatus

/** Design tokens extracted from the SISRI/Go Out Figma file. */
object GoOutColors {
    val Primary = Color(0xFF004A76)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFCEE5FF)
    val Background = Color(0xFFF9F9FC)
    val SurfaceCard = Color(0xFFF3F3F6)
    val TextPrimary = Color(0xFF1A1C1E)
    val TextSecondary = Color(0xFF40474F)
    val TextTertiary = Color(0xFF717881)
    val Outline = Color(0xFFC0C7D1)
    val ChipNeutral = Color(0xFFE0E2EB)

    // Traffic density palette (Figma)
    val Lancar = Color(0xFF0F9D58)
    val LancarContainer = Color(0xFFE6F4EA)
    val OnLancarContainer = Color(0xFF0D652D)
    val Merayap = Color(0xFFF59E0B)
    val MerayapContainer = Color(0xFFFEF3C7)
    val OnMerayapContainer = Color(0xFF92400E)
    val Macet = Color(0xFFEA4335)
    val MacetContainer = Color(0xFFFCE8E6)
    val OnMacetContainer = Color(0xFFC5221F)
    val Unknown = Color(0xFF9AA0A6)
    val UnknownContainer = Color(0xFFE0E2EB)
    val OnUnknownContainer = Color(0xFF44474E)

    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)
    val LiveGreen = Color(0xFF005220)
}

/** Color of a density status for polylines/indicators. */
fun DensityStatus.statusColor(): Color = when (this) {
    DensityStatus.LANCAR -> GoOutColors.Lancar
    DensityStatus.MERAYAP -> GoOutColors.Merayap
    DensityStatus.MACET -> GoOutColors.Macet
    DensityStatus.UNKNOWN -> GoOutColors.Unknown
}

fun DensityStatus.containerColor(): Color = when (this) {
    DensityStatus.LANCAR -> GoOutColors.LancarContainer
    DensityStatus.MERAYAP -> GoOutColors.MerayapContainer
    DensityStatus.MACET -> GoOutColors.MacetContainer
    DensityStatus.UNKNOWN -> GoOutColors.UnknownContainer
}

fun DensityStatus.onContainerColor(): Color = when (this) {
    DensityStatus.LANCAR -> GoOutColors.OnLancarContainer
    DensityStatus.MERAYAP -> GoOutColors.OnMerayapContainer
    DensityStatus.MACET -> GoOutColors.OnMacetContainer
    DensityStatus.UNKNOWN -> GoOutColors.OnUnknownContainer
}

fun DensityStatus.label(): String = when (this) {
    DensityStatus.LANCAR -> "LANCAR"
    DensityStatus.MERAYAP -> "MERAYAP"
    DensityStatus.MACET -> "MACET"
    DensityStatus.UNKNOWN -> "TIDAK ADA DATA"
}

private val LightColors = lightColorScheme(
    primary = GoOutColors.Primary,
    onPrimary = GoOutColors.OnPrimary,
    primaryContainer = GoOutColors.PrimaryContainer,
    onPrimaryContainer = GoOutColors.Primary,
    background = GoOutColors.Background,
    onBackground = GoOutColors.TextPrimary,
    surface = Color.White,
    onSurface = GoOutColors.TextPrimary,
    surfaceVariant = GoOutColors.SurfaceCard,
    onSurfaceVariant = GoOutColors.TextSecondary,
    outline = GoOutColors.Outline,
    errorContainer = GoOutColors.ErrorContainer,
    onErrorContainer = GoOutColors.OnErrorContainer,
)

// Figma uses Roboto Flex; the Android system Roboto family is its direct
// equivalent on-device, so we rely on the default platform font.
private val GoOutTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.25.sp),
)

@Composable
fun GoOutTheme(content: @Composable () -> Unit) {
    // The Figma design specifies a light interface; dark mode falls back to
    // the same scheme to keep the brand look consistent.
    MaterialTheme(
        colorScheme = LightColors,
        typography = GoOutTypography,
        content = content,
    )
}
