package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.SharedContentKey
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeNavigateBar
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import indi.dmzz_yyhyy.lightnovelreader.utils.removeFromBookshelfAction
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import kotlinx.coroutines.delay
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReadingScreen(
    controller: NavController,
    selectedRoute: Any,
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
    animatedVisibilityScope: AnimatedVisibilityScope,
    loadBookInfo: (Int) -> Unit
) {
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        updateReadingBooks()
    }
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopBar(
                    scrollBehavior = pinnedScrollBehavior,
                    onClickDownloadManager = onClickDownloadManager,
                    onClickStats = onClickStats
                )
            },
            bottomBar = {
                HomeNavigateBar(
                    modifier = Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(SharedContentKey.HomeNavigateBar),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    selectedRoute = selectedRoute,
                    controller = controller
                )
            },
            snackbarHost = {
                SnackbarHost(LocalSnackbarHost.current) {
                    LnrSnackbar(it)
                }
            }
        ) {
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
                modifier = Modifier.padding(it),
                visible = showEmptyPage,
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                EmptyPage(
                    modifier = Modifier.padding(it),
                    icon = painterResource(R.drawable.empty_90dp),
                    title = stringResource(R.string.nothing_here),
                    description = stringResource(R.string.nothing_here_desc_reading),
                )
            }

            ReadingContent(
                modifier = Modifier.padding(it),
                onClickBook = onClickBook,
                onClickContinueReading = onClickContinueReading,
                onRemoveBook = onRemoveBook,
                recentReadingBookInformationMap = recentReadingBookInformationMap,
                recentReadingUserReadingDataMap = recentReadingUserReadingDataMap,
                recentReadingBookIds = recentReadingBookIds,
                scrollBehavior = pinnedScrollBehavior,
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
    scrollBehavior: TopAppBarScrollBehavior,
    loadBookInfo: (Int) -> Unit
) {
    val context = LocalContext.current
    val shimmerInstance = rememberShimmer(ShimmerBounds.Custom)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current

    LazyColumn(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.unclippedBoundsInWindow()
                shimmerInstance.updateBounds(position)
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (recentReadingBookIds.isNotEmpty()
            && recentReadingUserReadingDataMap[recentReadingBookIds.first()] != null
            && recentReadingBookInformationMap[recentReadingBookIds.first()] != null
            ) {
            item {
                Box(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = stringResource(R.string.continue_reading),
                        maxLines = 1,
                        style = AppTypography.titleSmall,
                        fontWeight = FontWeight.W600,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            item {
                ReadingHeaderCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    bookInformation = recentReadingBookInformationMap[recentReadingBookIds.first()]!!,
                    userReadingData = recentReadingUserReadingDataMap[recentReadingBookIds.first()]!!,
                    onClickContinueReading = onClickContinueReading
                )
            }
        }
        if (recentReadingBookIds.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = stringResource(
                            R.string.recent_reads, recentReadingBookIds.size,
                        ),
                        maxLines = 1,
                        style = AppTypography.titleSmall,
                        fontWeight = FontWeight.W600,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        items(recentReadingBookIds) { id ->
            LaunchedEffect(id) {
                loadBookInfo(id)
            }

            val info = recentReadingBookInformationMap[id]
            val userData = recentReadingUserReadingDataMap[id]

            Crossfade(
                targetState = info != null && userData != null && !info.isEmpty(),
                label = "ReadingBookCardCrossfade"
            ) { loaded ->
                if (loaded && info != null && userData != null) {
                    ReadingBookCard(
                        modifier = Modifier.animateItem()
                            .fillMaxWidth()
                            .background(colorScheme.background)
                            .padding(horizontal = 16.dp),
                        bookInformation = recentReadingBookInformationMap[id]!!,
                        userReadingData = recentReadingUserReadingDataMap[id]!!,
                        onClick = {
                            onClickBook(recentReadingBookInformationMap[id]!!.id)
                        },
                        swipeToLeftActions = listOf(
                            removeFromBookshelfAction.toSwipeAction {
                                onRemoveBook(id)
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.removed_item, recentReadingBookInformationMap[id]?.title),
                                    actionLabel = context.getString(R.string.undo),
                                ) {
                                    when (it) {
                                        SnackbarResult.Dismissed -> { }
                                        SnackbarResult.ActionPerformed -> onRemoveBook(-id)
                                    }
                                }
                            }
                        )
                    )
                } else {
                    ReadingBookCardSkeleton(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .shimmer(shimmerInstance)
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
    val skeletonColor = colorScheme.surfaceContainerHigh
    val skeletonRoundedCorner = RoundedCornerShape(4.dp)
    Row(
        modifier = modifier
            .height(144.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(94.dp, 142.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(skeletonColor)
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
                    .padding(top = 3.dp)
                    .height(40.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.43f)
                    .height(20.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)

            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(skeletonRoundedCorner)
                    .background(skeletonColor)

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
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
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadingBookCard(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onClick: () -> Unit,
    swipeToRightActions: List<SwipeAction> = listOf(),
    swipeToLeftActions: List<SwipeAction> = listOf(),
) {
    SwipeableActionsBox(
        startActions = swipeToRightActions,
        endActions = swipeToLeftActions
    ) {
        Box(
            modifier = modifier
                .height(144.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .combinedClickable(
                        onClick = onClick
                    )
                    .padding(4.dp),
            ) {
                Cover(
                    width = 94.dp,
                    height = 142.dp,
                    url = bookInformation.coverUrl,
                    rounded = 8.dp,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    val textStyle = AppTypography.labelLarge
                    val textLineHeight = textStyle.lineHeight
                    Text(
                        modifier = Modifier
                            .height(
                                with(LocalDensity.current) { (textLineHeight * 2.2f).toDp() }
                            )
                            .wrapContentHeight(Alignment.CenterVertically),
                        text = bookInformation.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.W600,
                        style = AppTypography.labelLarge,
                        lineHeight = textLineHeight,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bookInformation.author,
                            maxLines = 1,
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.W600,
                            color = colorScheme.primary
                        )
                    }
                    Text(
                        text = bookInformation.description.trim(),
                        maxLines = 2,
                        fontWeight = FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        style = AppTypography.bodyMedium,
                        color = colorScheme.secondary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row {
                            Icon(
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 2.dp)
                                    .size(14.dp)
                                    .align(Alignment.CenterVertically),
                                painter = painterResource(id = R.drawable.outline_schedule_24px),
                                contentDescription = null,
                                tint = colorScheme.secondary
                            )
                            val text = buildString {
                                append(formTime(userReadingData.lastReadTime))
                                append(" • ")
                                append(
                                    stringResource(
                                        R.string.read_minutes,
                                        userReadingData.totalReadTime / 60
                                    )
                                )
                                append(" • ")
                                append((userReadingData.readingProgress * 100).toInt())
                                append("%")
                            }
                            Text(
                                text = text,
                                modifier = Modifier.align(Alignment.CenterVertically),
                                style = AppTypography.labelSmall
                            )
                        }
                    }

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { userReadingData.readingProgress }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingHeaderCard(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onClickContinueReading: (Int, Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(178.dp)
            .padding(horizontal = 4.dp),
    ) {
        Box {
            Cover(
                height = 178.dp,
                width = 122.dp,
                url = bookInformation.coverUrl,
                rounded = 8.dp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = R.drawable.filled_menu_book_24px),
                    contentDescription = null,
                    tint = colorScheme.secondary
                )
                Text(
                    text = stringResource(
                        R.string.last_read_info,
                        formTime(userReadingData.lastReadTime)
                    ),
                    color = colorScheme.secondary,
                    style = AppTypography.labelMedium,
                )
            }
            val textStyle = AppTypography.titleLarge
            val textLineHeight = textStyle.lineHeight
            Text(
                modifier = Modifier
                    .height(
                        with(LocalDensity.current) { (textLineHeight * 2.2f).toDp() }
                    )
                    .wrapContentHeight(Alignment.CenterVertically),
                text = bookInformation.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W700,
                style = textStyle,
                lineHeight = textLineHeight,
            )
            Text(
                text = userReadingData.lastReadChapterTitle,
                maxLines = 1,
                style = AppTypography.labelMedium,
                color = colorScheme.primary,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onClickContinueReading(bookInformation.id, userReadingData.lastReadChapterId) }) {
                    Text(
                        text = stringResource(R.string.resume_last_reading),
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}