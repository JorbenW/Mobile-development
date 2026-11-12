package edu.ap.savebite.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import edu.ap.savebite.R

val Bricolage = FontFamily(Font(R.font.bricolage_grotesque_extrabold, FontWeight.ExtraBold))

val DmSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold)
)

private val heading = TextStyle(
    fontFamily = Bricolage,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.02).sp
)

private val default = Typography()

// Headings use Bricolage Grotesque, everything else DM Sans
val Typography = Typography(
    displayLarge = default.displayLarge.merge(heading),
    displayMedium = default.displayMedium.merge(heading),
    displaySmall = default.displaySmall.merge(heading),
    headlineLarge = default.headlineLarge.merge(heading),
    headlineMedium = default.headlineMedium.merge(heading),
    headlineSmall = default.headlineSmall.merge(heading).copy(fontSize = 24.sp),
    titleLarge = default.titleLarge.merge(heading),
    titleMedium = default.titleMedium.copy(fontFamily = DmSans, fontWeight = FontWeight.Bold),
    titleSmall = default.titleSmall.copy(fontFamily = DmSans, fontWeight = FontWeight.Bold),
    bodyLarge = default.bodyLarge.copy(fontFamily = DmSans),
    bodyMedium = default.bodyMedium.copy(fontFamily = DmSans),
    bodySmall = default.bodySmall.copy(fontFamily = DmSans),
    labelLarge = default.labelLarge.copy(fontFamily = DmSans, fontWeight = FontWeight.Bold),
    labelMedium = default.labelMedium.copy(fontFamily = DmSans, fontWeight = FontWeight.Bold),
    labelSmall = default.labelSmall.copy(fontFamily = DmSans, fontWeight = FontWeight.Bold)
)
