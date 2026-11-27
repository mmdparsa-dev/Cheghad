package com.mmdparsadev.cheghad.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.mmdparsadev.cheghad.MainActivity
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.NewsArticle
import com.mmdparsadev.cheghad.data.models.NewsCategory
import com.mmdparsadev.cheghad.data.repository.NewsRepository
import com.mmdparsadev.cheghad.formatTimeAgo
import com.mmdparsadev.cheghad.isTvDevice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    innerPadding: PaddingValues,
    digitType: String = "fa",
    newsArticles: List<NewsArticle>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    disabledCategories: Set<String> = emptySet(),
    disabledAgencies: Set<String> = emptySet(),
    newsEnabled: Boolean = true,
    onOpenAgenciesSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val isTv = isTvDevice()
    val isEnglish = digitType == "en"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NewsCategory.All) }
    var selectedAgencyId by remember { mutableStateOf<String?>(null) }
    var showOnlyBookmarks by remember { mutableStateOf(false) }
    var bookmarkedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedArticleForDetail by remember { mutableStateOf<NewsArticle?>(null) }

    val allAgenciesDisabled = remember(disabledAgencies) {
        disabledAgencies.size >= NewsRepository.AGENCIES.size
    }
    val allCategoriesDisabled = remember(disabledCategories) {
        disabledCategories.size >= NewsCategory.entries.count { it != NewsCategory.All }
    }

    // Filter articles based on category, agency, search query, bookmarks, and disabled settings
    val filteredArticles = remember(newsArticles, selectedCategory, selectedAgencyId, searchQuery, showOnlyBookmarks, bookmarkedIds, disabledCategories, disabledAgencies) {
        newsArticles.filter { article ->
            val notDisabledCategory = !disabledCategories.contains(article.category.name)
            val notDisabledAgency = !disabledAgencies.contains(article.agency.id)
            val matchesCategory = selectedCategory == NewsCategory.All || article.category == selectedCategory
            val matchesAgency = selectedAgencyId == null || article.agency.id == selectedAgencyId
            val matchesSearch = searchQuery.isEmpty() ||
                    article.title.contains(searchQuery, ignoreCase = true) ||
                    article.summary.contains(searchQuery, ignoreCase = true) ||
                    article.agency.nameFa.contains(searchQuery, ignoreCase = true)
            val matchesBookmark = !showOnlyBookmarks || bookmarkedIds.contains(article.id)

            notDisabledCategory && notDisabledAgency && matchesCategory && matchesAgency && matchesSearch && matchesBookmark
        }.sortedByDescending { it.pubTimestamp }
    }

    if (!newsEnabled || allAgenciesDisabled || allCategoriesDisabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(if (isTv) 96.dp else 80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.news_disabled_status),
                    fontSize = if (isTv) 22.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.news_enable_description),
                    fontSize = if (isTv) 15.sp else 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                val enableBtnFocus = rememberTvFocusState(
                    isTv = isTv,
                    focusedScale = 1.05f,
                    focusedBorderWidth = 2.5.dp,
                    unfocusedBorderWidth = 0.dp,
                    focusedBorderColor = MaterialTheme.colorScheme.inversePrimary,
                    unfocusedBorderColor = Color.Transparent
                )
                Button(
                    onClick = onOpenAgenciesSettings,
                    interactionSource = enableBtnFocus.interactionSource,
                    border = if (enableBtnFocus.isFocused) BorderStroke(enableBtnFocus.borderWidth, enableBtnFocus.borderColor) else null,
                    modifier = Modifier.scale(enableBtnFocus.scale),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.news_enable_action))
                }
            }
        }
    } else {
        ExpressivePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isTv) 32.dp else 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.news_title),
                            fontSize = if (isTv) 22.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.news_subtitle),
                            fontSize = if (isTv) 13.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    // Bookmark toggle button
                    TvFocusableIconButton(
                        onClick = { showOnlyBookmarks = !showOnlyBookmarks },
                        isTv = isTv,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (showOnlyBookmarks) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = if (showOnlyBookmarks) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.news_bookmarks),
                            tint = if (showOnlyBookmarks) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = {
                        Text(
                            stringResource(R.string.news_search_placeholder),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips Row (Expressive Connected Button Group)
                val categories = remember(disabledCategories) {
                    listOf(
                        NewsCategory.All to R.string.news_category_all,
                        NewsCategory.Economic to R.string.news_category_economic,
                        NewsCategory.CurrencyGold to R.string.news_category_currency,
                        NewsCategory.Bourse to R.string.news_category_bourse,
                        NewsCategory.Crypto to R.string.news_category_crypto,
                        NewsCategory.World to R.string.news_category_world
                    ).filter { (cat, _) -> cat == NewsCategory.All || !disabledCategories.contains(cat.name) }
                }

                // Reset selected category if it's no longer available
                LaunchedEffect(categories) {
                    if (categories.none { it.first == selectedCategory }) {
                        selectedCategory = NewsCategory.All
                    }
                }

                val selectedCategoryIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

                ExpressiveConnectedButtonGroup(
                    itemsCount = categories.size,
                    selectedIndex = selectedCategoryIndex,
                    onSelect = { selectedCategory = categories[it].first },
                    scrollable = true,
                    height = 40.dp,
                    spacing = 4.dp
                ) { index, isSelected ->
                    Text(
                        text = stringResource(categories[index].second),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Agencies Filter Row (Expressive Connected Button Group)
                val activeAgencies = remember(disabledAgencies) {
                    NewsRepository.AGENCIES.filter { agency -> !disabledAgencies.contains(agency.id) }
                }
                val agencyItems = listOf(null to stringResource(R.string.news_agency_all)) + activeAgencies.map {
                    it.id to if (isEnglish) it.nameEn else it.nameFa
                }

                // Reset selected agency if it's no longer available
                LaunchedEffect(agencyItems) {
                    if (selectedAgencyId != null && agencyItems.none { it.first == selectedAgencyId }) {
                        selectedAgencyId = null
                    }
                }

                val selectedAgencyIndex = agencyItems.indexOfFirst { it.first == selectedAgencyId }.coerceAtLeast(0)

                ExpressiveConnectedButtonGroup(
                    itemsCount = agencyItems.size,
                    selectedIndex = selectedAgencyIndex,
                    onSelect = { selectedAgencyId = agencyItems[it].first },
                    scrollable = true,
                    height = 36.dp,
                    spacing = 4.dp
                ) { index, isSelected ->
                    Text(
                        text = agencyItems[index].second,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Articles Area with Animation
                AnimatedContent(
                    targetState = Triple(filteredArticles, selectedCategory, selectedAgencyId),
                    transitionSpec = {
                        val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                        (slideInHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> (width * 0.05f).toInt() } + fadeIn(animationSpec = tween(300)))
                            .togetherWith(
                                slideOutHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> -(width * 0.05f).toInt() } + fadeOut(animationSpec = tween(150))
                            )
                    },
                    label = "NewsListAnimation"
                ) { (articles, _, _) ->
                    if (articles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Article,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.news_no_results),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        val articleItemContent: @Composable (NewsArticle) -> Unit = { article ->
                            val isBookmarked = bookmarkedIds.contains(article.id)
                            NewsArticleCard(
                                article = article,
                                isEnglish = isEnglish,
                                isBookmarked = isBookmarked,
                                onBookmarkToggle = {
                                    bookmarkedIds = if (isBookmarked) {
                                        bookmarkedIds - article.id
                                    } else {
                                        bookmarkedIds + article.id
                                    }
                                },
                                onShare = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.link}")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, article.title))
                                },
                                onClick = {
                                    selectedArticleForDetail = article
                                }
                            )
                        }

                        if (isTv) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp)
                            ) {
                                gridItems(articles, key = { it.id }) { article ->
                                    articleItemContent(article)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(articles, key = { it.id }) { article ->
                                    articleItemContent(article)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Article Detail Bottom Sheet / Modal
    selectedArticleForDetail?.let { article ->
        ModalBottomSheet(
            onDismissRequest = { selectedArticleForDetail = null },
            sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isTv) Modifier.widthIn(max = 680.dp) else Modifier)
                        .padding(24.dp)
                ) {
                    // Agency & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = article.agency.brandColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isEnglish) article.agency.nameEn else article.agency.nameFa,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = article.agency.brandColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = formatTimeAgo(context, article.pubTimestamp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = article.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary / Body
                    Text(
                        text = article.summary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    val readBtnFocus = rememberTvFocusState(
                        isTv = isTv,
                        focusedScale = 1.03f,
                        focusedBorderWidth = 2.5.dp,
                        unfocusedBorderWidth = 0.dp,
                        focusedBorderColor = MaterialTheme.colorScheme.inversePrimary,
                        unfocusedBorderColor = Color.Transparent
                    )
                    val shareBtnFocus = rememberTvFocusState(
                        isTv = isTv,
                        focusedScale = 1.05f,
                        focusedBorderWidth = 2.5.dp,
                        unfocusedBorderWidth = 0.dp,
                        unfocusedBorderColor = Color.Transparent
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) {
                                }
                            },
                            interactionSource = readBtnFocus.interactionSource,
                            border = if (readBtnFocus.isFocused) BorderStroke(readBtnFocus.borderWidth, readBtnFocus.borderColor) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .scale(readBtnFocus.scale),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.news_read_more))
                        }

                        FilledTonalButton(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.link}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, article.title))
                            },
                            interactionSource = shareBtnFocus.interactionSource,
                            border = if (shareBtnFocus.isFocused) BorderStroke(shareBtnFocus.borderWidth, shareBtnFocus.borderColor) else null,
                            modifier = Modifier
                                .height(48.dp)
                                .scale(shareBtnFocus.scale),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    isEnglish: Boolean,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isTv = isTvDevice()
    val tvFocus = rememberTvFocusState(
        isTv = isTv,
        focusedScale = 1.02f,
        focusedBorderWidth = 2.5.dp,
        unfocusedBorderWidth = 1.dp,
        unfocusedBorderColor = Color.Transparent
    )
    Card(
        onClick = onClick,
        interactionSource = tvFocus.interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (tvFocus.isFocused) 2f else 0f)
            .scale(tvFocus.scale),
        shape = RoundedCornerShape(28.dp),
        border = if (tvFocus.isFocused) BorderStroke(tvFocus.borderWidth, tvFocus.borderColor) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (tvFocus.isFocused) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Agency badge + relative time + bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(article.agency.brandColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEnglish) article.agency.nameEn else article.agency.nameFa,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = article.agency.brandColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${formatTimeAgo(context, article.pubTimestamp)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row {
                    TvFocusableIconButton(
                        onClick = onBookmarkToggle,
                        isTv = isTv,
                        modifier = Modifier.size(if (isTv) 36.dp else 32.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TvFocusableIconButton(
                        onClick = onShare,
                        isTv = isTv,
                        modifier = Modifier.size(if (isTv) 36.dp else 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = article.summary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
        }
    }
}
