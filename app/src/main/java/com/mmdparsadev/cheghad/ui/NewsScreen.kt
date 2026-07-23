package com.mmdparsadev.cheghad.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.NewsArticle
import com.mmdparsadev.cheghad.data.models.NewsCategory
import com.mmdparsadev.cheghad.data.repository.NewsRepository
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
    disabledAgencies: Set<String> = emptySet()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NewsCategory.All) }
    var selectedAgencyId by remember { mutableStateOf<String?>(null) }
    var showOnlyBookmarks by remember { mutableStateOf(false) }
    var bookmarkedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedArticleForDetail by remember { mutableStateOf<NewsArticle?>(null) }

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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.news_subtitle),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Bookmark toggle button
                IconButton(
                    onClick = { showOnlyBookmarks = !showOnlyBookmarks },
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
            val agencyItems = listOf(null to stringResource(R.string.news_agency_all)) + activeAgencies.map { it.id to it.nameFa }
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

            // Articles List
            if (filteredArticles.isEmpty()) {
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredArticles, key = { it.id }) { article ->
                        val isBookmarked = bookmarkedIds.contains(article.id)
                        NewsArticleCard(
                            article = article,
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
                }
            }
        }
    }

    // Article Detail Bottom Sheet / Modal
    selectedArticleForDetail?.let { article ->
        ModalBottomSheet(
            onDismissRequest = { selectedArticleForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                            text = article.agency.nameFa,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = article.agency.brandColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = article.timeAgo,
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
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
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
                        modifier = Modifier.height(48.dp),
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

@Composable
fun NewsArticleCard(
    article: NewsArticle,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
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
                        text = article.agency.nameFa,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = article.agency.brandColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${article.timeAgo}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row {
                    IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
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
