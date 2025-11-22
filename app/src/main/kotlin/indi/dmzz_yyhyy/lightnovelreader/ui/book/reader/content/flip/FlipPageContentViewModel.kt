package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt

class FlipPageContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val updateReadingProgress: (String, Float) -> Unit,
    val contentComponentRepository: ContentComponentRepository
) : ContentViewModel {
    private var notRecoveredProgress = 0f
    private var collectProgressJob: Job? = null
    override val uiState: MutableFlipPageContentUiState = MutableFlipPageContentUiState(
        loadLastChapter = ::loadLastChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter,
        updatePageState = ::updatePagerState,
        getContentData = contentComponentRepository::getContentDataFromJson
    )

    init {
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.pagerState }.collect { pagerState ->
                collectProgressJob?.cancel()
                collectProgressJob = coroutineScope.launch(Dispatchers.IO) {
                    snapshotFlow { pagerState.settledPage }.collect {
                        val progress = if (pagerState.pageCount == 0) 0f
                        else ((it + 1) / pagerState.pageCount.toFloat()).coerceIn(0f, 1f)
                        uiState.readingProgress = progress
                        updateReadingProgress(uiState.readingChapterContent.id, progress)
                    }
                }
            }
        }
    }

    fun updatePagerState(pagerState: PagerState) {
        uiState.pagerState = pagerState
        if (pagerState.pageCount == 0) return
        if (notRecoveredProgress <= 0f) return
        val recovered = notRecoveredProgress.coerceIn(0f, 1f)
        notRecoveredProgress = 0f
        coroutineScope.launch {
            val target = ((pagerState.pageCount * recovered).roundToInt() - 1)
                .coerceIn(0, pagerState.pageCount - 1)
            uiState.pagerState.scrollToPage(target)
        }
    }

    override fun changeBookId(id: String) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        if (!uiState.readingChapterContent.hasNextChapter()) return
        changeChapter(
            id = uiState.readingChapterContent.nextChapter
        )
    }

    override fun loadLastChapter() {
        if (!uiState.readingChapterContent.hasPrevChapter()) return
        changeChapter(
            id = uiState.readingChapterContent.lastChapter
        )
    }

    override fun changeChapter(id: String) {
        if (id.isBlank()) {
            Log.e("FlipPageContentViewModel", "a id less than 0 was transferred")
            return
        }
        notRecoveredProgress = 0f
        uiState.readingProgress = 0f
        uiState.readingChapterContent = bookRepository.getStateChapterContent(
            id,
            uiState.bookId,
            coroutineScope,
            WebDataSourcePriority.High
        )
        uiState.readingChapterContent
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.readingChapterContent.title }.collect { title ->
                bookRepository.updateUserReadingData(uiState.bookId) {
                    it.apply {
                        lastReadChapterProgress =
                            if (it.lastReadChapterId == id) it.lastReadChapterProgress else 0f
                        lastReadTime = LocalDateTime.now()
                        lastReadChapterId = id
                        lastReadChapterTitle = title
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.readingChapterContent.nextChapter }.collect {
                if (uiState.readingChapterContent.hasNextChapter()) {
                    bookRepository.getChapterContent(
                        chapterId = it,
                        bookId = uiState.bookId,
                    )
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingData(uiState.bookId).let {
                if (it.lastReadChapterId == uiState.readingChapterContent.id)
                    notRecoveredProgress = it.lastReadChapterProgress
            }
        }
    }
}