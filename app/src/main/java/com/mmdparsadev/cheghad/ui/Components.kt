package com.mmdparsadev.cheghad.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveConnectedButtonGroup(
    modifier: Modifier = Modifier,
    itemsCount: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    spacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    height: Dp = 44.dp,
    scrollable: Boolean = false,
    content: @Composable (index: Int, isSelected: Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    val rowModifier = if (scrollable) {
        modifier.horizontalScroll(scrollState)
    } else {
        modifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until itemsCount) {
            val isSelected = index == selectedIndex

            val itemModifier = if (scrollable) {
                Modifier.height(height)
            } else {
                Modifier
                    .weight(1f)
                    .height(height)
            }

            // ۱. استخراج متغیر شکل (Shape) اصلی بر اساس جایگاه دکمه در گروه
            val targetShape = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes().shape
                itemsCount - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes().shape
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onSelect(index) },
                // ۲. مقداردهی استاندارد به پارامتر shapes با استفاده از ToggleButtonDefaults
                shapes = ToggleButtonDefaults.shapes(
                    shape = targetShape,
                    checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape
                ),
                modifier = itemModifier,
                contentPadding = PaddingValues(
                    horizontal = if (scrollable) 16.dp else 4.dp,
                    vertical = 0.dp
                )
            ) {
                content(index, isSelected)
            }
        }
    }
}
