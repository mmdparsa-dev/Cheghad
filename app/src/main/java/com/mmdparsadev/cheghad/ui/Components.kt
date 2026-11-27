package com.mmdparsadev.cheghad.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.isTvDevice
import com.mmdparsadev.cheghad.ui.viewmodel.CurrencyUiState

data class TvFocusState(
    val interactionSource: MutableInteractionSource,
    val isFocused: Boolean,
    val scale: Float,
    val borderWidth: Dp,
    val borderColor: Color
)

@Composable
fun rememberTvFocusState(
    isTv: Boolean = isTvDevice(),
    focusedScale: Float = 1.04f,
    focusedBorderWidth: Dp = 2.5.dp,
    unfocusedBorderWidth: Dp = 1.dp,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
): TvFocusState {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val tvFocused = isTv && isFocused
    val scale by animateFloatAsState(
        targetValue = if (tvFocused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "tvFocusScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (tvFocused) focusedBorderColor else unfocusedBorderColor,
        animationSpec = tween(durationMillis = 150),
        label = "tvFocusBorderColor"
    )
    return TvFocusState(
        interactionSource = interactionSource,
        isFocused = tvFocused,
        scale = scale,
        borderWidth = if (tvFocused) focusedBorderWidth else unfocusedBorderWidth,
        borderColor = borderColor
    )
}

@Composable
fun TvFocusableIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isTv: Boolean = isTvDevice(),
    shape: Shape = CircleShape,
    focusedScale: Float = 1.12f,
    focusedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
    content: @Composable () -> Unit
) {
    val tvFocus = rememberTvFocusState(
        isTv = isTv,
        focusedScale = focusedScale,
        focusedBorderWidth = 2.dp,
        unfocusedBorderWidth = 0.dp,
        unfocusedBorderColor = Color.Transparent
    )
    val tvBg by animateColorAsState(
        targetValue = if (tvFocus.isFocused) focusedContainerColor else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "tvIconBg"
    )
    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = tvFocus.interactionSource,
        modifier = modifier
            .zIndex(if (tvFocus.isFocused) 2f else 0f)
            .graphicsLayer {
                scaleX = tvFocus.scale
                scaleY = tvFocus.scale
            }
            .clip(shape)
            .then(
                if (tvFocus.isFocused) {
                    Modifier
                        .background(tvBg, shape)
                        .border(tvFocus.borderWidth, tvFocus.borderColor, shape)
                } else Modifier
            )
    ) {
        content()
    }
}

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
    val isTv = isTvDevice()
    val scrollState = rememberScrollState()
    val resolvedHeight = if (isTv) height.coerceAtLeast(42.dp) else height

    val baseModifier = if (isTv) {
        modifier.padding(vertical = 3.dp, horizontal = 2.dp)
    } else {
        modifier
    }

    val rowModifier = if (scrollable) {
        baseModifier.horizontalScroll(scrollState)
    } else {
        baseModifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until itemsCount) {
            val isSelected = index == selectedIndex
            val tvFocus = rememberTvFocusState(
                isTv = isTv,
                focusedScale = 1.05f,
                focusedBorderWidth = 2.5.dp,
                unfocusedBorderWidth = 0.dp,
                unfocusedBorderColor = Color.Transparent
            )

            val itemModifier = (if (scrollable) {
                Modifier.height(resolvedHeight)
            } else {
                Modifier
                    .weight(1f)
                    .height(resolvedHeight)
            })
                .zIndex(if (tvFocus.isFocused) 1f else 0f)
                .graphicsLayer {
                    scaleX = tvFocus.scale
                    scaleY = tvFocus.scale
                }

            // 1. Extract button shape definitions based on position within the group
            val buttonShapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                itemsCount - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onSelect(index) },
                // 2. Apply extracted shapes to the button
                shapes = buttonShapes,
                interactionSource = tvFocus.interactionSource,
                border = if (tvFocus.isFocused) {
                    BorderStroke(tvFocus.borderWidth, tvFocus.borderColor)
                } else null,
                modifier = itemModifier,
                contentPadding = PaddingValues(
                    horizontal = if (scrollable) (if (isTv) 18.dp else 16.dp) else 4.dp,
                    vertical = 0.dp
                )
            ) {
                content(index, isSelected)
            }
        }
    }
}

@Composable
fun ConnectivityStatusBanner(uiState: CurrencyUiState) {
    val isTv = isTvDevice()
    AnimatedVisibility(
        visible = uiState.isOffline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = if (isTv) 24.dp else 16.dp, vertical = if (isTv) 8.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(if (isTv) 20.dp else 16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (uiState.isLoading) R.string.status_updating
                        else R.string.status_offline_mode
                    ),
                    style = if (isTv) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
