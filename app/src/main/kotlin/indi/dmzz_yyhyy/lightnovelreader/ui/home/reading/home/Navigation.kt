package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.ChapterSelectorBottomSheet
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager.navigateToDownloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.navigateToReadingStatsDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
fun NavGraphBuilder.readingHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Reading.Home> { entry ->
        val navController = LocalNavController.current
        val context = LocalContext.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val viewModel = hiltViewModel<ReadingHomeViewModel>(parentEntry)

        val chapterSheetUi = viewModel.chapterSheetUi
        val volumesMap = viewModel.bookVolumesMap
        val chapterSheetState = rememberModalBottomSheetState()

        ReadingScreen(
            updateReadingBooks = viewModel::updateReadingBooks,
            recentReadingBookIds = viewModel.recentReadingBookIds,
            recentReadingUserReadingDataMap = viewModel.recentReadingUserReadingDataMap,
            recentReadingBookInformationMap = viewModel.recentReadingBookInformationMap,
            onClickDownloadManager = navController::navigateToDownloadManager,
            onClickBook = navController::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navController.navigateToBookDetailDestination(bookId)
                navController.navigateToBookReaderDestination(bookId, chapterId, context)
            },
            sharedTransitionScope = sharedTransitionScope,
            onClickStats = navController::navigateToReadingStatsDestination,
            loadBookInfo = viewModel::loadBookInfo,
            onRemoveBook = viewModel::removeFromReadingList,
            onClickOpenChapters = viewModel::openChapters
        )

        if (chapterSheetUi != null) {
            volumesMap[chapterSheetUi.bookId]?.let { volumes ->
                ChapterSelectorBottomSheet(
                    sheetState = chapterSheetState,
                    selectedVolumeId = chapterSheetUi.selectedVolumeId,
                    bookVolumes = volumes,
                    readingChapterId = chapterSheetUi.readingChapterId,
                    onDismissRequest = viewModel::closeContents,
                    onClickChapter = { chapterId ->
                        navController.navigateToBookReaderDestination(
                            chapterSheetUi.bookId,
                            chapterId,
                            context
                        )
                        viewModel.closeContents()
                    },
                    onChangeSelectedVolumeId = viewModel::setVolume
                )
            }
        }
    }
}
