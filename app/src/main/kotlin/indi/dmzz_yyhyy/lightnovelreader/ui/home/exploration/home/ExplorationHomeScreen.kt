package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationUiState
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.fadingEdge
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExplorationHomeScreen(
    explorationUiState: ExplorationUiState,
    explorationHomeUiState: ExplorationHomeUiState,
    onClickExpand: (String) -> Unit,
    onClickBook: (Int) -> Unit,
    init: () -> Unit,
    changePage: (Int) -> Unit,
    onClickSearch: () -> Unit,
    refresh: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        init()
    }

    with(sharedTransitionScope) {
        Column {
            TopBar(
                scrollBehavior = enterAlwaysScrollBehavior,
                onClickSearch = onClickSearch
            )
            ExplorationScreen(
                refresh = refresh,
                uiState = explorationUiState
            ) {
                Column {
                    PrimaryTabRow(selectedTabIndex = explorationHomeUiState.selectedPage) {
                        explorationHomeUiState.pageTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = explorationHomeUiState.selectedPage == index,
                                onClick = {
                                    changePage(index)
                                },
                                text = {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }

                    var showEmptyPage by remember { mutableStateOf(false) }

                    LaunchedEffect(explorationHomeUiState.explorationPageBooksRawList) {
                        if (explorationHomeUiState.explorationPageBooksRawList.isEmpty()) {
                            delay(140)
                            showEmptyPage = true
                        } else {
                            showEmptyPage = false
                        }
                    }

                    AnimatedVisibility(
                        visible = showEmptyPage,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Loading()
                    }
                    AnimatedContent(
                        targetState = explorationHomeUiState.explorationPageBooksRawList,
                        contentKey = { explorationHomeUiState.selectedPage },
                        transitionSpec = {
                            (fadeIn(initialAlpha = 0.7f)).togetherWith(fadeOut(targetAlpha = 0.7f))
                        },
                        label = "ExplorationPageBooksRawAnime"
                    ) {
                        ExplorationPage(
                            explorationPageBooksRawList = it,
                            onClickExpand = onClickExpand,
                            onClickBook = onClickBook,
                            nestedScrollConnection = enterAlwaysScrollBehavior.nestedScrollConnection,
                            refresh = refresh
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickSearch: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.nav_explore),
                style = AppTypography.titleTopBar,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            Box(Modifier.size(48.dp)) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(id = R.drawable.outline_explore_24px),
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = onClickSearch) {
                Icon(
                    painter = painterResource(id = R.drawable.search_24px),
                    contentDescription = "search"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExplorationPage(
    explorationPageBooksRawList: List<ExplorationBooksRow>,
    onClickExpand: (String) -> Unit,
    onClickBook: (Int) -> Unit,
    nestedScrollConnection: NestedScrollConnection,
    refresh: () -> Unit
) {
    val pullState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            refresh()
            scope.launch {
                pullState.animateToHidden()
                isRefreshing = false
            }
        },
        state = pullState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection),
            state = listState
        ) {
            items(
                items = explorationPageBooksRawList,
                key = { it.title }
            ) { explorationBooksRow ->
                Column(
                    modifier = Modifier
                        .animateItem()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                            .height(46.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(2f),
                            text = explorationBooksRow.title,
                            style = AppTypography.titleMedium,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (explorationBooksRow.expandable) {
                            IconButton(
                                modifier = Modifier.size(40.dp),
                                onClick = {
                                    explorationBooksRow.expandedPageDataSourceId?.let {
                                        onClickExpand(it)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_forward_24px),
                                    contentDescription = "expand"
                                )
                            }
                        }
                    }

                    val lazyRowState = rememberLazyListState()

                    CompositionLocalProvider(LocalOverscrollFactory provides null) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fadingEdge(
                                    Brush.horizontalGradient(
                                        0.01f to Color.Transparent,
                                        0.03f to Color.White,
                                        0.97f to Color.White,
                                        0.99f to Color.Transparent
                                    )
                                )
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            state = lazyRowState,
                            flingBehavior = rememberSnapFlingBehavior(lazyRowState)
                        ) {
                            item {
                                Box(modifier = Modifier.width(10.dp))
                            }

                            items(
                                items = explorationBooksRow.bookList,
                                key = { it.id }
                            ) { explorationDisplayBook ->
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onClickBook(explorationDisplayBook.id)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Cover(
                                            width = 98.dp,
                                            height = 138.dp,
                                            url = explorationDisplayBook.coverUrl,
                                            rounded = 6.dp
                                        )
                                    }
                                    Column(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .padding(horizontal = 2.dp)
                                            .padding(top = 8.dp, bottom = 2.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        val titleLineHeight = 16.sp
                                        val titleHeight = with(LocalDensity.current) {
                                            (titleLineHeight * 2.2f).toDp()
                                        }

                                        Text(
                                            modifier = Modifier
                                                .height(titleHeight)
                                                .wrapContentHeight(Alignment.Top),
                                            text = explorationDisplayBook.title,
                                            style = AppTypography.titleVerySmall.copy(
                                                letterSpacing = 0.5.sp
                                            ),
                                            lineHeight = titleLineHeight,
                                            fontWeight = FontWeight.W500,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (explorationDisplayBook.author.isNotEmpty()) {
                                            Text(
                                                text = explorationDisplayBook.author,
                                                style = AppTypography.titleVerySmall.copy(
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Box(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        HorizontalDivider()
                    }
                }
            }
            navigationBarSpacer()
            bottomBarSpacer()
        }
    }
}
