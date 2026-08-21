package com.mmdparsadev.cheghad.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.isTvDevice
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mmdparsadev.cheghad.ui.theme.getFontFamilyForText

/**
 * Official Material 3 Expressive Loading Indicator.
 * Uses androidx.compose.material3.LoadingIndicator which morphs between shapes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    progress: Float? = null, // null for indeterminate, 0f..1f for determinate
    isRefreshing: Boolean = true
) {
    if (progress != null && !isRefreshing) {
        val coercedProgress = progress.coerceIn(0f, 1f)
        LoadingIndicator(
            progress = { coercedProgress },
            modifier = modifier,
            color = color
        )
    } else {
        LoadingIndicator(
            modifier = modifier,
            color = color
        )
    }
}

/**
 * Material 3 Expressive Pull-to-Refresh Container Indicator.
 * Displays a floating capsule surface with the loading indicator and smooth spring dynamics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressivePullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.motionScheme
    val distanceFraction = state.distanceFraction.coerceIn(0f, 2f)

    // Animated scale and opacity based on pull distance or refresh state
    val containerScale by animateFloatAsState(
        targetValue = if (isRefreshing) 1f else (distanceFraction * 0.95f).coerceIn(0f, 1.15f),
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "containerScale"
    )

    val alphaFraction by animateFloatAsState(
        targetValue = if (isRefreshing) 1f else distanceFraction.coerceIn(0f, 1f),
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "alphaFraction"
    )

    if (alphaFraction > 0.01f || isRefreshing) {
        Box(
            modifier = modifier
                .zIndex(10f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = (12.dp * distanceFraction.coerceAtMost(1.5f)))
                    .scale(containerScale)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ExpressiveLoadingIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary,
                        progress = if (isRefreshing) null else distanceFraction.coerceIn(0f, 1f),
                        isRefreshing = isRefreshing
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isRefreshing) stringResource(R.string.pull_refresh_updating) else if (distanceFraction >= 1f) stringResource(R.string.pull_refresh_release) else stringResource(R.string.pull_refresh_pull),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = getFontFamilyForText(stringResource(R.string.pull_refresh_updating))
                    )
                }
            }
        }
    }
}

/**
 * Expressive Wrapper for PullToRefreshBox using official M3 Expressive LoadingIndicator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressivePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isTv = isTvDevice()

    if (isTv) {
        Box(modifier = modifier, content = content)
    } else {
        val state = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
            state = state,
            indicator = {
                ExpressivePullToRefreshIndicator(
                    state = state,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            },
            content = content
        )
    }
}
