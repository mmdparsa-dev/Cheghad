package com.mmdparsadev.cheghad.ui

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.PriceDirection
import java.util.Locale

/**
 * PriceCard: کامپوننت نمایش قیمت ارز با پشتیبانی دوگانه از Mobile و Android TV
 */
@Composable
fun PriceCard(
    currency: CurrencyItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTvMode: Boolean = isTvDevice(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    // ۱. رصد وضعیت فوکوس (برای ریموت D-Pad تلویزیون و کیبورد)
    val isFocused by interactionSource.collectIsFocusedAsState()

    // ۲. پویانمایی بزرگ‌نمایی (Scale) هنگام فوکوس در حالت تلویزیون
    val animatedScale by animateFloatAsState(
        targetValue = if (isTvMode && isFocused) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "PriceCardScale"
    )

    // ۳. پویانمایی رنگ حاشیه (Border Highlight)
    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primary // حاشیه برجسته هنگام فوکوس
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "PriceCardBorderColor"
    )

    // ۴. ضخامت حاشیه در حالت فوکوس
    val animatedBorderWidth: Dp = if (isFocused) 3.dp else 1.dp

    // ۵. تغییر رنگ پس‌زمینه کارت هنگام فوکوس
    val animatedContainerColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(durationMillis = 200),
        label = "PriceCardContainerColor"
    )

    // ۶. تنظیم سایز و فونت‌ها متناسب با پلتفرم (موبایل / تلویزیون)
    val cardPadding = if (isTvMode) 20.dp else 14.dp
    val titleStyle = if (isTvMode) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
    val priceStyle = if (isTvMode) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge
    val changeStyle = if (isTvMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium

    OutlinedCard(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(if (isTvMode) 20.dp else 16.dp),
        border = BorderStroke(animatedBorderWidth, animatedBorderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = animatedContainerColor
        ),
        elevation = CardDefaults.outlinedCardElevation(
            defaultElevation = if (isFocused) 10.dp else 0.dp
        ),
        modifier = modifier
            .scale(animatedScale)
            .then(
                if (isTvMode) {
                    Modifier.width(280.dp).fillMaxHeight() // عرض مناسب برای افقی چیده شدن در TV
                } else {
                    Modifier.fillMaxWidth() // عرض کامل برای لیست عمودی موبایل
                }
            )
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = cardPadding,
                    bottom = if (isTvMode) 48.dp else cardPadding,
                    start = cardPadding,
                    end = cardPadding
                )
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = if (isTvMode) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp)
        ) {
            // هدر کارت: عنوان ارز و نماد
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currency.title,
                    style = titleStyle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currency.symbol,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // بخش قیمت فعلی و درصد تغییرات
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // نمایش قیمت به تومان
                Text(
                    text = "${formatPrice(currency.currentPrice)} تومان",
                    style = priceStyle,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // نشانه صعودی / نزولی بودن قیمت
                val (color, symbol) = when (currency.priceDirection) {
                    PriceDirection.Up -> Color(0xFF2E7D32) to "▲"
                    PriceDirection.Down -> Color(0xFFC62828) to "▼"
                    PriceDirection.Unchanged -> MaterialTheme.colorScheme.onSurfaceVariant to "▬"
                }

                Surface(
                    color = color.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = symbol,
                            style = changeStyle,
                            color = color
                        )
                        Text(
                            text = "${currency.changePercentage}%",
                            style = changeStyle,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
            
            if (isTvMode) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * تشخیص دقیق Android TV با استفاده از UiModeManager سیستم
 */
@Composable
fun isTvDevice(): Boolean {
    val context = LocalContext.current
    val uiModeManager = remember(context) {
        context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    }
    return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}

private fun formatPrice(price: Double): String {
    return String.format(Locale.US, "%,.0f", price)
}

/**
 * نمونه لیست عمودی مخصوص موبایل
 */
@Composable
fun MobileCurrencyListScreen(
    currencies: List<CurrencyItem>,
    onCurrencySelect: (CurrencyItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(currencies, key = { it.id }) { currency ->
            PriceCard(
                currency = currency,
                onClick = { onCurrencySelect(currency) },
                isTvMode = false
            )
        }
    }
}

/**
 * نمونه لیست افقی با TvLazyRow جهت هماهنگی ۱۰۰٪ با D-Pad و اسکرول خودکار (Center Focus)
 */
@Composable
fun TvCurrencyRowScreen(
    currencies: List<CurrencyItem>,
    onCurrencySelect: (CurrencyItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "قیمت لحظه‌ای ارزها",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 48.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        TvLazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(currencies, key = { it.id }) { currency ->
                PriceCard(
                    currency = currency,
                    onClick = { onCurrencySelect(currency) },
                    isTvMode = true
                )
            }
        }
    }
}
