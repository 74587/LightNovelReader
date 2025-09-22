package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedTextLine
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookStatusIcon
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.navigateToSettingsTextFormattingRulesDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.fadingEdge
import indi.dmzz_yyhyy.lightnovelreader.utils.isScrollingUp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onClickExportToEpub: (ExportSettings) -> Unit,
    onClickBackButton: () -> Unit,
    onClickChapter: (Int) -> Unit,
    onClickReadFromStart: () -> Unit,
    onClickContinueReading: () -> Unit,
    cacheBook: (Int) -> Unit,
    requestAddBookToBookshelf: (Int) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCover: (String) -> Unit,
    onClickMarkAllRead: () -> Unit
) {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showExportBottomSheet by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    val exportBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val lazyListState = rememberLazyListState()
    val volumesEmpty = uiState.bookVolumes.volumes.isEmpty()

    val isCollapsed by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 ||
                    lazyListState.firstVisibleItemScrollOffset > 650
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                title = uiState.bookInformation.title,
                volumesEmpty = volumesEmpty,
                onClickBackButton = onClickBackButton,
                onClickExport = { showExportBottomSheet = true },
                onClickTextFormatting = {
                    navController.navigateToSettingsTextFormattingRulesDestination(uiState.bookInformation.id)
                },
                onClickMarkAllRead = onClickMarkAllRead,
                scrollBehavior = scrollBehavior,
                isCollapsed = isCollapsed
            )
        },
        snackbarHost = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(
                    visible = lazyListState.canScrollForward && lazyListState.isScrollingUp().value && uiState.bookVolumes.volumes.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .padding(end = 24.dp, start = 16.dp)
                            .padding(vertical = 14.dp),
                        onClick = if (uiState.userReadingData.lastReadChapterId == -1) onClickReadFromStart
                        else onClickContinueReading,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.filled_menu_book_24px),
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(if (uiState.userReadingData.lastReadChapterId == -1) stringResource(R.string.start_reading)
                            else stringResource(id = R.string.continue_reading))
                        }
                    )
                }
                AnimatedVisibility(
                    LocalSnackbarHost.current.currentSnackbarData != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    SnackbarHost(
                        hostState = LocalSnackbarHost.current
                    )
                }
                Spacer(Modifier.height(42.dp))
            }
        }
    ) { paddingValues ->
        val bookIsEmpty = uiState.bookInformation.title.isEmpty()
        val infoBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        var showInfoBottomSheet by remember { mutableStateOf(false) }

        Crossfade(
            targetState = uiState.isLoading || bookIsEmpty,
            animationSpec = tween(300),
            label = "DetailScreenCrossfade"
        ) { empty ->
            if (empty) {
                DetailContentSkeleton(Modifier.padding(paddingValues))
            } else {
                DetailContent(
                    modifier = Modifier.padding(paddingValues),
                    uiState = uiState,
                    onClickChapter = onClickChapter,
                    lazyListState = lazyListState,
                    cacheBook = cacheBook,
                    requestAddBookToBookshelf = requestAddBookToBookshelf,
                    onClickTag = onClickTag,
                    onClickCover = onClickCover,
                    onClickShowInfo = { showInfoBottomSheet = true }
                )
            }
        }

        AnimatedVisibility(visible = showExportBottomSheet) {
            ExportBottomSheet(
                sheetState = exportBottomSheetState,
                bookVolumes = uiState.bookVolumes,
                settings = exportSettings,
                onSettingsChange = { exportSettings = it },
                onDismissRequest = { showExportBottomSheet = false },
                onClickExport = onClickExportToEpub
            )
        }
        AnimatedVisibility(visible = showInfoBottomSheet) {
            BookInfoBottomSheet(
                bookInformation = uiState.bookInformation,
                bookVolumes = uiState.bookVolumes,
                sheetState = infoBottomSheetState,
                onDismissRequest = { showInfoBottomSheet = false }
            )
        }

    }
}

@Composable
private fun DetailContentSkeleton(modifier: Modifier = Modifier) {
    val skeletonColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val rounded = RoundedCornerShape(6.dp)
    val shimmerInstance = rememberShimmer(ShimmerBounds.Custom)

    Column(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.unclippedBoundsInWindow()
                shimmerInstance.updateBounds(position)
            }
            .shimmer(shimmerInstance),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(188.dp)
                .padding(horizontal = itemHorizontalPadding, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 122.dp, height = 178.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(skeletonColor)
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(20.dp)
                            .clip(rounded)
                            .background(skeletonColor)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(skeletonColor)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),

            ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .padding(vertical = itemVerticalPadding)
                        .clip(RoundedCornerShape(8.dp))
                        .background(skeletonColor)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(24.dp)
                    .clip(rounded)
                    .background(skeletonColor)
            )
            Spacer(Modifier.height(10.dp))
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(rounded)
                        .background(skeletonColor)
                )
            }
        }

    }
}

private val itemHorizontalPadding = 18.dp
private val itemVerticalPadding = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    lazyListState: LazyListState,
    onClickChapter: (Int) -> Unit,
    cacheBook: (Int) -> Unit,
    requestAddBookToBookshelf: (Int) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCover: (String) -> Unit,
    onClickShowInfo: () -> Unit
) {
    var hideReadChapters by remember { mutableStateOf(false) }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        item {
            BookCardBlock(
                bookInformation = uiState.bookInformation,
                totalReadTime = uiState.userReadingData.totalReadTime,
                modifier = Modifier
                    .graphicsLayer {
                        translationY = lazyListState.firstVisibleItemScrollOffset * 0.5f
                    }
                    .fillMaxWidth(),
                onClickCover = onClickCover
            )
        }
        item {
            TagsBlock(
                bookInformation = uiState.bookInformation,
                onClickTag = onClickTag
            )
        }
        item {
            QuickOperationsBlock(
                isInBookshelf = uiState.isInBookshelf,
                isCached = uiState.isCached,
                downloadItem = uiState.downloadItem,
                onClickAddToBookShelf = { requestAddBookToBookshelf(uiState.bookInformation.id) },
                onClickCache = { cacheBook(uiState.bookInformation.id) },
                onClickShowInfo = onClickShowInfo
            )
        }
        item {
            IntroBlock(uiState.bookInformation.description)
        }
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_contents),
                    style = AppTypography.titleLarge,
                    fontWeight = FontWeight.W600
                )
                Spacer(Modifier.width(12.dp))
                AssistChip(
                    onClick = { hideReadChapters = !hideReadChapters },
                    label = {
                        Text(
                            text = stringResource(
                                if (hideReadChapters) R.string.show_read
                                else R.string.hide_read
                            )
                        )
                    },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.scale(0.75f),
                            painter = painterResource(
                                if (hideReadChapters) R.drawable.filled_menu_book_24px
                                else R.drawable.done_all_24px
                            ),
                            contentDescription = "Toggle Hide Read"
                        )
                    },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = uiState.bookVolumes.volumes.isEmpty(),
                enter = fadeIn(tween(300, 1000)),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Loading()
                }
            }
        }

        items(
            items = uiState.bookVolumes.volumes,
            key = { it.volumeId }
        ) { volume ->
            VolumeItem(
                volume = volume,
                hideReadChapters = hideReadChapters,
                readCompletedChapterIds = uiState.userReadingData.readCompletedChapterIds,
                onClickChapter = onClickChapter,
                volumesSize = uiState.bookVolumes.volumes.size,
                lastReadingChapterId = uiState.userReadingData.lastReadChapterId
            )
        }

        item {
            Spacer(Modifier.height(48.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    volumesEmpty: Boolean,
    onClickBackButton: () -> Unit,
    onClickExport: () -> Unit,
    onClickTextFormatting: () -> Unit,
    onClickMarkAllRead: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    isCollapsed: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val detailTitle = stringResource(R.string.detail_title)

    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedTextLine(
                    text = if (isCollapsed) title else detailTitle,
                    style = AppTypography.titleTopBar,
                    fontWeight = FontWeight.Normal,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        },
        actions = {
            IconButton(
                enabled = !volumesEmpty,
                onClick = onClickExport
            ) {
                Icon(painterResource(id = R.drawable.file_export_24px), "export to epub")
            }
            IconButton(
                enabled = !volumesEmpty,
                onClick = onClickTextFormatting
            ) {
                Icon(painterResource(id = R.drawable.find_replace_24px), "text formating")
            }
            Box {
                IconButton(
                    enabled = !volumesEmpty,
                    onClick = { menuExpanded = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vert_24px),
                        contentDescription = "more"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "全部标为已读",
                                style = AppTypography.dropDownItem
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onClickMarkAllRead()
                        }
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun BookCardBlock(
    bookInformation: BookInformation,
    totalReadTime: Int,
    modifier: Modifier,
    onClickCover: (String) -> Unit
) {
    val updateText = if (bookInformation.isComplete) {
        stringResource(R.string.book_completed)
    } else {
        stringResource(
            R.string.book_info_update_date,
            bookInformation.lastUpdated.year,
            bookInformation.lastUpdated.monthValue,
            bookInformation.lastUpdated.dayOfMonth
        )
    }
    val wordCountText = stringResource(
        R.string.book_info_word_count_kilo,
        NumberFormat.getNumberInstance(Locale.getDefault()).format(bookInformation.wordCount / 1000)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(188.dp)
            .padding(horizontal = itemHorizontalPadding, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable(
                    onClick = {
                        onClickCover(bookInformation.coverUrl)
                    }
                )
        ) {
            Cover(
                height = 178.dp,
                width = 122.dp,
                url = bookInformation.coverUrl,
                rounded = 8.dp
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = bookInformation.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600,
                style = AppTypography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (bookInformation.subtitle.isNotEmpty()) {
                Text(
                    text = bookInformation.subtitle,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.secondary,
                    style = AppTypography.labelMedium
                )
            }
            Text(
                text = bookInformation.author,
                maxLines = 1,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.primary,
                style = AppTypography.labelLarge
            )
            Column {
                InfoRow(
                    icon = { BookStatusIcon(bookInformation.isComplete) },
                    text = updateText
                )
                Spacer(Modifier.height(2.dp))
                InfoRow(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.text_snippet_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                    },
                    text = wordCountText
                )
                if (totalReadTime > 59) {
                    Spacer(Modifier.height(2.dp))
                    InfoRow(
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.schedule_90dp),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                        },
                        text = stringResource(
                            R.string.read_minutes,
                            totalReadTime / 60
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            maxLines = 1,
            style = AppTypography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}


@Composable
private fun TagsBlock(
    bookInformation: BookInformation,
    onClickTag: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = itemVerticalPadding, horizontal = itemHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            if (bookInformation.publishingHouse.isNotEmpty()) {
                item {
                    SuggestionChip(label = { Text(bookInformation.publishingHouse) }, onClick = {})
                }
            }
            items(bookInformation.tags, key = { it }) { tag ->
                SuggestionChip(label = { Text(tag) }, onClick = { onClickTag(tag) })
            }
        }
    )
}

@Composable
fun QuickOperationButton(
    icon: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        contentPadding = PaddingValues(12.dp),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(0.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickOperationsBlock(
    isInBookshelf: Boolean,
    isCached: Boolean,
    downloadItem: DownloadItem?,
    onClickAddToBookShelf: () -> Unit,
    onClickCache: () -> Unit,
    onClickShowInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding)
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInBookshelf) {
            QuickOperationButton(
                icon = painterResource(R.drawable.filled_bookmark_add_24px),
                title = stringResource(R.string.activity_collections),
                onClick = onClickAddToBookShelf,
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickOperationButton(
                icon = painterResource(R.drawable.bookmark_add_24px),
                title = stringResource(R.string.add_to_bookshelf),
                onClick = onClickAddToBookShelf,
                modifier = Modifier.weight(1f)
            )
        }

        if (isCached) {
            QuickOperationButton(
                icon = painterResource(R.drawable.filled_cloud_download_24px),
                title = if (downloadItem == null || downloadItem.progress == 1f)
                    stringResource(R.string.cached)
                else
                    "${(downloadItem.progress * 100).toInt()}%",
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickOperationButton(
                icon = painterResource(R.drawable.cloud_download_24px),
                title = if (downloadItem == null)
                    stringResource(R.string.cached_false)
                else
                    "${(downloadItem.progress * 100).toInt()}%",
                onClick = if (downloadItem == null) onClickCache else { {} },
                modifier = Modifier.weight(1f)
            )
        }

        QuickOperationButton(
            icon = painterResource(R.drawable.info_24px),
            title = stringResource(R.string.action_show_info),
            onClick = onClickShowInfo,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IntroBlock(description: String) {
    var expanded by remember { mutableStateOf(false) }
    var overflowed by remember(description.length) {
        mutableStateOf(false)
    }

    val fadingBrush = remember {
        Brush.verticalGradient(
            0.7f to Color.White,
            1f to Color.Transparent
        )
    }
    val whiteBrush = remember {
        Brush.verticalGradient(
            listOf(Color.White, Color.White)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(R.string.detail_introduction),
            style = AppTypography.titleLarge,
            fontWeight = FontWeight.W600
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .fadingEdge(
                    if (!expanded && overflowed) fadingBrush else whiteBrush
                ),
            text = description,
            style = AppTypography.bodyLarge,
            maxLines = if (!expanded) 4 else 99,
            onTextLayout = { result ->
                if (!overflowed && result.hasVisualOverflow) {
                    overflowed = true
                }
            },
            color = MaterialTheme.colorScheme.onSurface
        )

        if (overflowed) {
            val rotation by animateFloatAsState(if (expanded) 0f else 180f)
            Button(
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors().copy(containerColor = Color.Transparent),
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    modifier = Modifier.rotate(rotation),
                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
private fun VolumeItem(
    volume: Volume,
    hideReadChapters: Boolean = false,
    readCompletedChapterIds: List<Int>,
    onClickChapter: (Int) -> Unit,
    volumesSize: Int,
    lastReadingChapterId: Int
) {
    val readIds = remember(readCompletedChapterIds) { readCompletedChapterIds.toSet() }
    val (readCount, totalCount) = remember(volume.volumeId, readIds) {
        val count = volume.chapters.count { it.id in readIds }
        count to volume.chapters.size
    }
    val isFullyRead = readCount >= totalCount
    var expanded by rememberSaveable {
        mutableStateOf(readCount < totalCount || volumesSize > 8)
    }
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, animationSpec = tween(200))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.clickable { expanded = !expanded }.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(5f).padding(vertical = 12.dp)) {
                Text(
                    text = volume.volumeTitle,
                    style = AppTypography.titleMedium,
                    color = if (isFullyRead) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFullyRead) stringResource(R.string.info_reading_finished)
                    else stringResource(R.string.info_reading_progress, readCount, totalCount),
                    style = AppTypography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (!hideReadChapters || !isFullyRead) {
                Spacer(Modifier.weight(1f))
                Icon(
                    modifier = Modifier.size(16.dp).rotate(rotation),
                    painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                    contentDescription = null
                )
                Spacer(Modifier.width(12.dp))
            }
        }
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(250))) {
            if (expanded) {
                volume.chapters.forEach { chapter ->
                    val visible = !(hideReadChapters && chapter.id in readIds)
                    if (visible) {
                        ChapterItem(
                            chapter = chapter,
                            isRead = chapter.id in readIds,
                            isLastRead = chapter.id == lastReadingChapterId,
                            onClick = { onClickChapter(chapter.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterInformation,
    isRead: Boolean,
    isLastRead: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = 12.dp,
                bottom = if (isLastRead) 6.dp else 12.dp
            )
    ) {
        Column {
            Text(
                text = chapter.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = AppTypography.titleSmall,
                fontWeight = if (isRead) FontWeight.Normal else FontWeight.W600,
                color = if (isRead) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurface
            )
            if (isLastRead) {
                Text(
                    text = stringResource(R.string.last_read),
                    maxLines = 1,
                    style = AppTypography.titleSmall,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}