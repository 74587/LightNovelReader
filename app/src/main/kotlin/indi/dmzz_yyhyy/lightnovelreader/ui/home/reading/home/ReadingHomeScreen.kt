package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ElasticPressContainer
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.ui.components.rememberSkeletonShimmer
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import indi.dmzz_yyhyy.lightnovelreader.utils.mainScaffoldPaddings
import indi.dmzz_yyhyy.lightnovelreader.utils.removeFromBookshelfAction
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import kotlinx.coroutines.delay
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReadingScreen(
    updateReadingBooks: () -> Unit,
    recentReadingBookInformationMap: Map<Int, BookInformation>,
    recentReadingUserReadingDataMap: Map<Int, UserReadingData>,
    recentReadingBookIds: List<Int>,
    onClickBook: (Int) -> Unit,
    onClickContinueReading: (Int, Int) -> Unit,
    onClickDownloadManager: () -> Unit,
    onClickStats: () -> Unit,
    onRemoveBook: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    loadBookInfo: (Int) -> Unit
) {
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        updateReadingBooks()
    }

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier.mainScaffoldPaddings()
        ) {
            TopBar(
                onClickDownloadManager = onClickDownloadManager,
                onClickStats = onClickStats
            )
            var showEmptyPage by remember { mutableStateOf(false) }

            LaunchedEffect(recentReadingBookIds) {
                if (recentReadingBookIds.isEmpty()) {
                    delay(140)
                    showEmptyPage = true
                } else {
                    showEmptyPage = false
                }
            }

            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = showEmptyPage,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                EmptyPage(
                    icon = painterResource(R.drawable.empty_90dp),
                    title = stringResource(R.string.nothing_here),
                    description = stringResource(R.string.nothing_here_desc_reading),
                )
            }

            ReadingContent(
                modifier = Modifier.fillMaxSize(),
                onClickBook = onClickBook,
                onClickContinueReading = onClickContinueReading,
                onRemoveBook = onRemoveBook,
                recentReadingBookInformationMap = recentReadingBookInformationMap,
                recentReadingUserReadingDataMap = recentReadingUserReadingDataMap,
                recentReadingBookIds = recentReadingBookIds,
                loadBookInfo = loadBookInfo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ReadingContent(
    modifier: Modifier,
    onClickBook: (Int) -> Unit,
    onClickContinueReading: (Int, Int) -> Unit,
    onRemoveBook: (Int) -> Unit,
    recentReadingBookInformationMap: Map<Int, BookInformation>,
    recentReadingUserReadingDataMap: Map<Int, UserReadingData>,
    recentReadingBookIds: List<Int>,
    loadBookInfo: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1000)
        started = true
    }

    val shimmerInstance = rememberSkeletonShimmer(
        baseColor = colorScheme.surfaceContainerLow,
        highlightColor = colorScheme.surfaceContainerHigh
    )

    val headerItems = recentReadingBookIds
        .take(3)
        .mapNotNull { id ->
            val info = recentReadingBookInformationMap[id]
            val user = recentReadingUserReadingDataMap[id]
            if (info != null && user != null && !info.isEmpty()) info to user else null
        }

    LazyColumn(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.unclippedBoundsInWindow()
                shimmerInstance.updateBounds(position)
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (
            recentReadingBookIds.isNotEmpty()
            && recentReadingUserReadingDataMap[recentReadingBookIds.first()] != null
            && recentReadingBookInformationMap[recentReadingBookIds.first()] != null
            ) {
            item {
                SectionHeader(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp),
                    text = stringResource(R.string.continue_reading)
                )
            }

            item {
                if (headerItems.isNotEmpty()) {
                    ReadingHeaderCardPager(
                        items = headerItems,
                        modifier = Modifier.animateItem()
                            .padding(horizontal = 8.dp),
                        onClickContinueReading = onClickContinueReading
                    )
                }
            }
        }
        if (recentReadingBookIds.isNotEmpty()) {
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(
                        R.string.recent_reads, recentReadingBookIds.size,
                    )
                )
            }
        }
        items(
            items = recentReadingBookIds,
            key = { it }
        ) { id ->
            LaunchedEffect(id) {
                if (recentReadingBookInformationMap[id] == null ||
                    recentReadingUserReadingDataMap[id] == null) {
                    loadBookInfo(id)
                }
            }

            val info = recentReadingBookInformationMap[id]
            val userData = recentReadingUserReadingDataMap[id]

            Crossfade(
                targetState = info != null && userData != null && !info.isEmpty(),
                label = "ReadingBookCardCrossfade"
            ) { loaded ->
                if (loaded && info != null && userData != null) {
                    ReadingBookCard(
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 12.dp),
                        bookInformation = info,
                        userReadingData = userData,
                        onClick = { onClickBook(info.id) },
                        swipeToLeftActions = listOf(
                            removeFromBookshelfAction.toSwipeAction {
                                onRemoveBook(id)
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.removed_item, recentReadingBookInformationMap[id]?.title),
                                    actionLabel = context.getString(R.string.undo),
                                ) {
                                    if (it == SnackbarResult.ActionPerformed) onRemoveBook(-id)
                                }
                            }
                        )
                    )
                } else {
                    ReadingBookCardSkeleton(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .then(if (started) Modifier.shimmer(shimmerInstance) else Modifier)
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun ReadingBookCardSkeleton(
    modifier: Modifier = Modifier
) {
    val roundedSmall = RoundedCornerShape(4.dp)
    val roundedLarge = RoundedCornerShape(8.dp)
    val baseColor = colorScheme.surfaceContainerLow


    Row(
        modifier = modifier
            .height(144.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(94.dp, 142.dp)
                .clip(roundedLarge)
                .background(baseColor)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(40.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.43f)
                    .height(20.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickDownloadManager: () -> Unit,
    onClickStats: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_reading),
                style = AppTypography.titleTopBar,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = onClickDownloadManager) {
                Icon(
                    painter = painterResource(R.drawable.download_24px), null
                )
            }
            IconButton(
                onClick = onClickStats
            ) {
                Icon(
                    painter = painterResource(R.drawable.analytics_24px),
                    contentDescription = stringResource(R.string.nav_statistics)
                )
            }
        }
    )
}

@Composable
private fun ReadingBookCard(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onClick: () -> Unit,
    swipeToRightActions: List<SwipeAction> = emptyList(),
    swipeToLeftActions: List<SwipeAction> = emptyList(),
) {
    val lineHeight = AppTypography.labelLarge.lineHeight
    val titleHeight = with(LocalDensity.current) { (lineHeight * 2.2f).toDp() }

    val lastRead = formTime(userReadingData.lastReadTime)
    val minutes = stringResource(R.string.read_minutes, userReadingData.totalReadTime / 60)
    val progress = "${(userReadingData.readingProgress * 100).toInt()}%"


    val infoText = remember(userReadingData) {
        "$lastRead • $minutes • $progress"
    }

    SwipeableActionsBox(
        startActions = swipeToRightActions,
        endActions = swipeToLeftActions
    ) {
        Row(
            modifier = modifier
                .height(146.dp)
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(onClick = onClick)
                .padding(4.dp)
        ) {
            Cover(
                width = 94.dp,
                height = 144.dp,
                url = bookInformation.coverUrl,
                rounded = 8.dp,
            )

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .height(titleHeight)
                        .wrapContentHeight(Alignment.CenterVertically),
                    text = bookInformation.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTypography.labelLarge.copy(
                        fontWeight = FontWeight.W600
                    )
                )

                Text(
                    text = bookInformation.author,
                    style = AppTypography.bodyMedium.copy(
                        fontWeight = FontWeight.W600,
                        color = colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = bookInformation.description.trim(),
                    style = AppTypography.bodyMedium.copy(color = colorScheme.secondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_schedule_24px),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colorScheme.secondary
                    )
                    Text(
                        text = infoText,
                        style = AppTypography.labelSmall.copy(color = colorScheme.secondary)
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    progress = { userReadingData.readingProgress }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingHeaderCardPager(
    items: List<Pair<BookInformation, UserReadingData>>,
    modifier: Modifier = Modifier,
    onClickContinueReading: (Int, Int) -> Unit
) {
    val pageCount = items.size.coerceIn(1, 3)
    val pagerState = rememberPagerState(initialPage = 0) { pageCount }

    val showIndicator = remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            showIndicator.value = true
        } else {
            delay(1800)
            showIndicator.value = false
        }
    }

    val isolatePager = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) = Offset.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ) = Offset(0f, available.y)

            override suspend fun onPostFling(consumed: Velocity, available: Velocity) = available
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surface)
            .nestedScroll(isolatePager)
    ) {
        VerticalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        ) { page ->
            val (book, user) = items[page]

            ElasticPressContainer(
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.14f))
                    .padding(8.dp)
            ) {
                ReadingHeaderCardPage(
                    info = book,
                    data = user,
                    onClickContinueReading = onClickContinueReading,
                    modifier = Modifier.matchParentSize(),
                    onClickOpenDetail = { }
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = showIndicator.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VerticalDotsIndicator(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .padding(end = 6.dp)
            )
        }
    }
}

@Composable
private fun VerticalDotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(pageCount) { idx ->
            val selected = idx == currentPage
            Box(
                Modifier
                    .size(width = 6.dp, height = if (selected) 14.dp else 6.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        if (selected) colorScheme.primary
                        else colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun ReadingHeaderCardPage(
    info: BookInformation,
    data: UserReadingData,
    onClickContinueReading: (Int, Int) -> Unit,
    onClickOpenDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val lineHeight = AppTypography.titleLarge.lineHeight
    val titleHeight = with(density) { (lineHeight * 2.2f).toDp() }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val dateText = data.lastReadTime.format(dateFormatter)
    val timeText = data.lastReadTime.format(timeFormatter)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(
            height = 172.dp,
            width = 118.dp,
            url = info.coverUrl,
            rounded = 8.dp
        )

        Column(
            modifier = Modifier
                .padding(start = 14.dp)
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(text = dateText, leadingPainter = painterResource(R.drawable.calendar_today_24px))
                InfoChip(text = timeText, leadingPainter = painterResource(R.drawable.schedule_90dp))
            }

            Text(
                text = info.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W700,
                style = AppTypography.titleLarge,
                lineHeight = lineHeight,
                modifier = Modifier
                    .height(titleHeight)
                    .wrapContentHeight(Alignment.CenterVertically)
            )

            Text(
                text = data.lastReadChapterTitle,
                maxLines = 1,
                style = AppTypography.labelMedium,
                color = colorScheme.tertiary,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onClickOpenDetail(info.id) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.view_list_24px),
                        contentDescription = null
                    )
                }

                Button(
                    modifier = Modifier.weight(3f),
                    onClick = { onClickContinueReading(info.id, data.lastReadChapterId) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.resume_last_reading),
                        fontWeight = FontWeight.W600,
                        style = AppTypography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }

    }
}


@Composable
private fun InfoChip(
    modifier: Modifier = Modifier,
    text: String,
    leadingPainter: Painter? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingPainter != null) {
            Icon(
                painter = leadingPainter,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(14.dp),
                tint = colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = text,
            style = AppTypography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}