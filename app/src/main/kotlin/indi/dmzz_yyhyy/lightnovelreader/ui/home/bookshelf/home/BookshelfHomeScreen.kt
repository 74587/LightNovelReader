package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.work.SaveBookshelfWork
import indi.dmzz_yyhyy.lightnovelreader.ui.SharedContentKey
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BookCardItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeNavigateBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BookshelfHomeScreen(
    controller: NavController,
    selectedRoute: Any,
    init: () -> Unit,
    changePage: (Int) -> Unit,
    changeBookSelectState: (Int) -> Unit,
    uiState: BookshelfHomeUiState,
    onClickCreate: () -> Unit,
    onClickEdit: (Int) -> Unit,
    onClickBook: (Int) -> Unit,
    onClickEnableSelectMode: () -> Unit,
    onClickDisableSelectMode: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickPin: () -> Unit,
    onClickRemove: () -> Unit,
    onClickMarkSelectedBooks: () -> Unit,
    saveAllBookshelfJsonData: (Uri) -> Unit,
    saveBookshelfJsonData: (Uri) -> Unit,
    importBookshelf: (Uri) -> Unit,
    clearToast: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val animatedBackgroundColor by animateColorAsState(
        if (!uiState.selectMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer,
        label = "TopBarBackgroundColor"
    )
    val saveAllBookshelfLauncher = launcher(saveAllBookshelfJsonData)
    val saveThisBookshelfLauncher = launcher(saveBookshelfJsonData)
    val importBookshelfLauncher = launcher(importBookshelf)
    val lazyListState = rememberLazyListState()
    var updatedBooksExpanded by remember { mutableStateOf(true) }
    var pinnedBooksExpanded by remember { mutableStateOf(true) }
    var allBooksExpanded by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        init.invoke()
    }
    LaunchedEffect(uiState.toast) {
        if (uiState.toast.isEmpty()) return@LaunchedEffect
        Toast.makeText(context, uiState.toast, Toast.LENGTH_SHORT).show()
        clearToast()
    }
    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopBar(
                    scrollBehavior = enterAlwaysScrollBehavior,
                    backgroundColor = animatedBackgroundColor,
                    selectMode = uiState.selectMode,
                    uiState = uiState,
                    onClickCreate = onClickCreate,
                    onClickSearch = {  },
                    onClickEdit = { onClickEdit(uiState.selectedBookshelfId) },
                    onClickDisableSelectMode = onClickDisableSelectMode,
                    onClickSelectAll = onClickSelectAll,
                    onClickPin = onClickPin,
                    onClickRemove = onClickRemove,
                    onClickBookmark = onClickMarkSelectedBooks,
                    onClickShareBookshelf = {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.applicationInfo.processName}.provider",
                            File(context.cacheDir, "LightNovelReaderBookshelfData.lnr")
                        )
                        val workRequest = OneTimeWorkRequestBuilder<SaveBookshelfWork>()
                            .setInputData(
                                workDataOf(
                                    "bookshelfId" to uiState.selectedBookshelfId,
                                    "uri" to uri.toString(),
                                )
                            )
                            .build()
                        workManager.enqueueUniqueWork(
                            uri.toString(),
                            ExistingWorkPolicy.KEEP,
                            workRequest
                        )
                        scope.launch(Dispatchers.IO) {
                            workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                                when (it?.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        ShareCompat.IntentBuilder(context)
                                            .setType("application/zip")
                                            .setSubject("分享文件")
                                            .addStream(uri)
                                            .setChooserTitle("分享书架")
                                            .startChooser()
                                    }

                                    else -> return@collect
                                }
                            }
                        }
                    },
                    onClickSaveThisBookshelf = {
                        createBookshelfDataFile(
                            uiState.selectedBookshelf.name,
                            saveThisBookshelfLauncher
                        )
                    },
                    onClickSaveAllBookshelf = {
                        createBookshelfDataFile(
                            "bookshelves",
                            saveAllBookshelfLauncher
                        )
                    },
                    onClickImportBookshelf = { selectBookshelfDataFile(importBookshelfLauncher) }
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                if (uiState.bookshelfList.size > 4) {
                    ScrollableTabRow(
                        selectedTabIndex = uiState.selectedTabIndex,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[uiState.selectedTabIndex])
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(MaterialTheme.colorScheme.secondary),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                    ) {
                        uiState.bookshelfList.forEach { bookshelf ->
                            Tab(
                                selected = uiState.selectedBookshelfId == bookshelf.id,
                                onClick = { if (!uiState.selectMode) changePage(bookshelf.id) },
                                text = {
                                    Text(
                                        text = bookshelf.name,
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                } else {
                    PrimaryTabRow(
                        selectedTabIndex = uiState.selectedTabIndex
                    ) {
                        uiState.bookshelfList.forEach { bookshelf ->
                            Tab(
                                selected = uiState.selectedBookshelfId == bookshelf.id,
                                onClick = { if (!uiState.selectMode) changePage(bookshelf.id) },
                                text = {
                                    Text(
                                        text = bookshelf.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.selectedBookshelf.allBookIds.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    EmptyPage(
                        icon = painterResource(R.drawable.bookmarks_90px),
                        titleId = R.string.nothing_here,
                        descriptionId = R.string.nothing_here_desc_bookshelf
                    )
                }

                val onLongPress: (Int) -> Unit = { bookId ->
                    onClickEnableSelectMode.invoke()
                    changeBookSelectState(bookId)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(enterAlwaysScrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    state = lazyListState
                ) {
                    if (uiState.selectedBookshelf.updatedBookIds.isNotEmpty() && !uiState.selectMode)
                        item {
                            CollapseGroupTitle(
                                modifier = Modifier.animateItem(),
                                icon = painterResource(R.drawable.keep_24px),
                                title = stringResource(
                                    R.string.bookshelf_group_title_updated,
                                    uiState.selectedBookshelf.updatedBookIds.size
                                ),
                                expanded = updatedBooksExpanded,
                                onClickExpand = { updatedBooksExpanded = !updatedBooksExpanded }
                            )
                        }
                    if (updatedBooksExpanded && !uiState.selectMode) {
                        items(uiState.selectedBookshelf.updatedBookIds.reversed()) { updatedBookId ->
                            uiState.bookInformationMap[updatedBookId]?.let {
                                BookCardItem(
                                    bookInformation = it,
                                    selected = uiState.selectedBookIds.contains(it.id),
                                    latestChapterTitle = uiState.bookLastChapterTitleMap[updatedBookId],
                                    onClick = {
                                        if (!uiState.selectMode)
                                            onClickBook(it.id)
                                        else changeBookSelectState(it.id)
                                    },
                                    onLongPress = { }
                                )
                            }
                        }
                    }
                    if (uiState.selectedBookshelf.pinnedBookIds.isNotEmpty())
                        item {
                            CollapseGroupTitle(
                                modifier = Modifier.animateItem(),
                                icon = painterResource(R.drawable.keep_24px),
                                title = stringResource(
                                    R.string.bookshelf_group_title_pinned,
                                    uiState.selectedBookshelf.pinnedBookIds.size
                                ),
                                expanded = pinnedBooksExpanded,
                                onClickExpand = { pinnedBooksExpanded = !pinnedBooksExpanded }
                            )
                        }
                    if (pinnedBooksExpanded) {
                        items(uiState.selectedBookshelf.pinnedBookIds.reversed()) { pinnedBookId ->
                            uiState.bookInformationMap[pinnedBookId]?.let {
                                BookCardItem(
                                    bookInformation = it,
                                    selected = uiState.selectedBookIds.contains(it.id),
                                    onClick = {
                                        if (!uiState.selectMode)
                                            onClickBook(it.id)
                                        else changeBookSelectState(it.id)
                                    },
                                    onLongPress = { onLongPress(it.id) }
                                )
                            }
                        }
                    }
                    if (uiState.selectedBookshelf.allBookIds.isNotEmpty())
                        item {
                            CollapseGroupTitle(
                                modifier = Modifier.animateItem(),
                                icon = painterResource(R.drawable.outline_bookmark_24px),
                                title = stringResource(
                                    R.string.bookshelf_group_title_all,
                                    uiState.selectedBookshelf.allBookIds.size
                                ),
                                expanded = allBooksExpanded,
                                onClickExpand = { allBooksExpanded = !allBooksExpanded }
                            )
                        }
                    if (allBooksExpanded) {
                        items(uiState.selectedBookshelf.allBookIds.reversed()) { bookId ->
                            uiState.bookInformationMap[bookId]?.let {
                                BookCardItem(
                                    bookInformation = it,
                                    selected = uiState.selectedBookIds.contains(it.id),
                                    onClick = {
                                        if (!uiState.selectMode)
                                            onClickBook(it.id)
                                        else changeBookSelectState(it.id)
                                    },
                                    onLongPress = { onLongPress(it.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollapseGroupTitle(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    expanded: Boolean,
    onClickExpand: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = icon,
            contentDescription = null
        )
        AnimatedText(
            modifier = Modifier.weight(2f),
            text = title,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.W700,
            fontSize = 15.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClickExpand) {
            Icon(
                modifier = Modifier.rotate(if (expanded) 0f else 180f),
                painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                contentDescription = "expand"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    backgroundColor: Color,
    selectMode: Boolean,
    uiState: BookshelfHomeUiState,
    onClickCreate: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onClickSearch: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDisableSelectMode: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickPin: () -> Unit,
    onClickRemove: () -> Unit,
    onClickBookmark: () -> Unit,
    onClickShareBookshelf: () -> Unit,
    onClickSaveThisBookshelf: () -> Unit,
    onClickSaveAllBookshelf: () -> Unit,
    onClickImportBookshelf: () -> Unit
) {
    val localDensity = LocalDensity.current
    var mainMenuExpended by remember { mutableStateOf(false) }
    var exportImportMenuExpended by remember { mutableStateOf(false) }
    var mainMenuWidth by remember { mutableStateOf(0.dp) }
    var mainMenuItemHeight by remember { mutableStateOf(0.dp) }
    var exportImportMenuWidth by remember { mutableStateOf(0.dp) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Box(Modifier.align(Alignment.TopEnd)) {
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        with(localDensity) {
                            mainMenuWidth = layoutCoordinates.size.width.toDp()
                            mainMenuItemHeight = layoutCoordinates.size.height.toDp().div(4)
                        }
                    },
                offset = DpOffset(0.dp, (-1).dp),
                expanded = mainMenuExpended,
                onDismissRequest = { mainMenuExpended = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.bookshelf_create_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = onClickCreate
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.bookshelf_settings),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = onClickEdit
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.share_bookshelf),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    onClick = onClickShareBookshelf
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.import_and_export),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.arrow_right_24px),
                            contentDescription = null
                        )
                    },
                    onClick = { exportImportMenuExpended = true }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = exportImportMenuWidth + mainMenuWidth + 12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        with(localDensity) {
                            exportImportMenuWidth = layoutCoordinates.size.width.toDp()
                        }
                    },
                offset = DpOffset(0.dp, mainMenuItemHeight.times(3.5f)),
                expanded = exportImportMenuExpended,
                onDismissRequest = { exportImportMenuExpended = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_to_lnr_file)) },
                    onClick = {
                        onClickSaveThisBookshelf()
                        exportImportMenuExpended = false
                        mainMenuExpended = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_all_to_lnr_file)) },
                    onClick = {
                        onClickSaveAllBookshelf()
                        exportImportMenuExpended = false
                        mainMenuExpended = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_from_file)) },
                    onClick = {
                        onClickImportBookshelf()
                        exportImportMenuExpended = false
                        mainMenuExpended = false
                    }
                )
            }
        }
    }

    MediumTopAppBar(
        title = {
            AnimatedText(
                text = if (selectMode) stringResource(R.string.nav_bookshelf_select_mode, uiState.selectedBookIds.size)
                    else stringResource(R.string.nav_bookshelf),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            AnimatedVisibility(visible = selectMode) {
                IconButton(onClickDisableSelectMode) {
                    Icon(
                        painter = painterResource(R.drawable.cancel_24px),
                        contentDescription = "cancel"
                    )
                }
            }
        },
        actions = {
            if (!selectMode) {
                IconButton(onClickCreate) {
                    Icon(
                        painter = painterResource(R.drawable.library_add_24px),
                        contentDescription = "create"
                    )
                }
                /*IconButton(onClickSearch) {
                    Icon(
                        painter = painterResource(R.drawable.search_24px),
                        contentDescription = "search"
                    )
                }*/
                IconButton(onClick = { mainMenuExpended = true }) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert_24px),
                        contentDescription = stringResource(R.string.more)
                    )
                }
            } else {
                IconButton(onClickSelectAll) {
                    Icon(
                        painter = painterResource(R.drawable.select_all_24px),
                        contentDescription = "select all"
                    )
                }
                IconButton(onClickPin) {
                    Icon(
                        painter = painterResource(R.drawable.keep_24px),
                        contentDescription = "pin"
                    )
                }
                IconButton(onClickRemove) {
                    Icon(
                        painter = painterResource(R.drawable.bookmark_remove_24px),
                        contentDescription = "remove"
                    )
                }
                IconButton(onClickBookmark) {
                    Icon(
                        painter = painterResource(R.drawable.outline_bookmark_24px),
                        contentDescription = "bookmark"
                    )
                }
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor
        )
    )
}

@Suppress("DuplicatedCode")
fun createBookshelfDataFile(fileName: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        putExtra(Intent.EXTRA_TITLE, "$fileName.lnr")
    }
    launcher.launch(Intent.createChooser(intent, "选择一位置"))
}

@Composable
fun launcher(block: (Uri) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data?.let { uri ->
                block(uri)
            }
        }
    }
}

@Suppress("DuplicatedCode")
fun selectBookshelfDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
    }
    launcher.launch(Intent.createChooser(intent, "选择数据文件"))
}