package com.mmdparsadev.cheghad.ui.theme

import android.content.Context
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mmdparsadev.cheghad.R

// تعریف FontFamily با استفاده از نسخه‌های استاتیک (غیر وریبل) فونت وزیر برای سازگاری حداکثری
val VazirFontFamily: FontFamily = FontFamily(
    Font(resId = R.font.vazir_thin, weight = FontWeight.Thin),
    Font(resId = R.font.vazir_light, weight = FontWeight.Light),
    Font(resId = R.font.vazir_regular, weight = FontWeight.Normal),
    Font(resId = R.font.vazir_medium, weight = FontWeight.Medium),
    Font(resId = R.font.vazir_bold, weight = FontWeight.Bold),
    Font(resId = R.font.vazir_black, weight = FontWeight.ExtraBold), // نگاشت ExtraBold به Black برای حفظ ظاهر
    Font(resId = R.font.vazir_black, weight = FontWeight.Black)
)

fun initializeVazirFont(context: Context) {
    // برای پشتیبانی از نسخه‌های قبلی نگه داشته شده است
}

fun isPersianText(text: String): Boolean {
    return text.any { ch ->
        ch in '\u0600'..'\u06FF' ||
        ch in '\u0750'..'\u077F' ||
        ch in '\u08A0'..'\u08FF' ||
        ch in '\uFB50'..'\uFDFF' ||
        ch in '\uFE70'..'\uFEFF'
    }
}

fun getFontFamilyForText(text: String): FontFamily {
    return VazirFontFamily
}

// تنظیمات مشترک برای حذف پدینگ اضافی فونت و بهبود تراز عمودی در زبان فارسی
val defaultPlatformStyle = PlatformTextStyle(includeFontPadding = false)

// تایپوگرافی متریال ۳ با استفاده از فونت‌های استاتیک
val Typography: Typography
    get() = Typography(
        displayLarge = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, platformStyle = defaultPlatformStyle),
        displayMedium = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, platformStyle = defaultPlatformStyle),
        displaySmall = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, platformStyle = defaultPlatformStyle),
        headlineLarge = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, platformStyle = defaultPlatformStyle),
        headlineMedium = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, platformStyle = defaultPlatformStyle),
        headlineSmall = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, platformStyle = defaultPlatformStyle),
        titleLarge = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp, platformStyle = defaultPlatformStyle),
        titleMedium = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, platformStyle = defaultPlatformStyle),
        titleSmall = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, platformStyle = defaultPlatformStyle),
        bodyLarge = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp, platformStyle = defaultPlatformStyle),
        bodyMedium = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, platformStyle = defaultPlatformStyle),
        bodySmall = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, platformStyle = defaultPlatformStyle),
        labelLarge = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, platformStyle = defaultPlatformStyle),
        labelMedium = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, platformStyle = defaultPlatformStyle),
        labelSmall = TextStyle(fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, platformStyle = defaultPlatformStyle)
    )

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val resolvedFontFamily = fontFamily ?: getFontFamilyForText(text)
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = resolvedFontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style.merge(TextStyle(platformStyle = defaultPlatformStyle))
    )
}
