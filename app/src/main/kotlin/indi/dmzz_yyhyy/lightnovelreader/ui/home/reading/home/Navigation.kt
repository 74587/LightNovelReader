package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager.navigateToDownloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.navigateToReadingStatsDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.readingHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Reading.Home> { entry ->
        val navController = LocalNavController.current
        val context = LocalContext.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val readingViewModel = hiltViewModel<ReadingHomeViewModel>(parentEntry)
        ReadingScreen(
            updateReadingBooks = readingViewModel::updateReadingBooks,
            recentReadingBookIds = readingViewModel.recentReadingBookIds,
            recentReadingUserReadingDataMap = readingViewModel.recentReadingUserReadingDataMap,
            recentReadingBookInformationMap = readingViewModel.recentReadingBookInformationMap,
            onClickDownloadManager = navController::navigateToDownloadManager,
            onClickBook = navController::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navController.navigateToBookDetailDestination(bookId)
                navController.navigateToBookReaderDestination(bookId, chapterId, context)
            },
            sharedTransitionScope = sharedTransitionScope,
            onClickStats = navController::navigateToReadingStatsDestination,
            loadBookInfo = readingViewModel::loadBookInfo,
            onRemoveBook = readingViewModel::removeFromReadingList
        )
    }
}