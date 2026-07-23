package com.mmdparsadev.cheghad.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpressiveConnectedButtonGroup(
    modifier: Modifier = Modifier,
    itemsCount: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    spacing: Dp = 4.dp,
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
        val cornerFull = height / 2
        val cornerFlat = 4.dp

        for (index in 0 until itemsCount) {
            val isSelected = index == selectedIndex
            val isPrevSelected = selectedIndex == index - 1
            val isNextSelected = selectedIndex == index + 1

            val topStartAnimated by animateDpAsState(
                targetValue = if (isSelected || index == 0 || isPrevSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "topStart_$index"
            )
            val bottomStartAnimated by animateDpAsState(
                targetValue = if (isSelected || index == 0 || isPrevSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "bottomStart_$index"
            )
            val topEndAnimated by animateDpAsState(
                targetValue = if (isSelected || index == itemsCount - 1 || isNextSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "topEnd_$index"
            )
            val bottomEndAnimated by animateDpAsState(
                targetValue = if (isSelected || index == itemsCount - 1 || isNextSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "bottomEnd_$index"
            )

            val animatedShape = RoundedCornerShape(
                topStart = topStartAnimated,
                bottomStart = bottomStartAnimated,
                topEnd = topEndAnimated,
                bottomEnd = bottomEndAnimated
            )

            val itemModifier = if (scrollable) {
                Modifier.height(height)
            } else {
                Modifier
                    .weight(1f)
                    .height(height)
            }

            Button(
                onClick = { onSelect(index) },
                shape = animatedShape,
                modifier = itemModifier,
                contentPadding = PaddingValues(horizontal = if (scrollable) 16.dp else 4.dp, vertical = 0.dp),
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 4.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                content(index, isSelected)
            }
        }
    }
}
