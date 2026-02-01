package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.debugPrint
import indi.dmzz_yyhyy.lightnovelreader.utils.throttleLatest
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ScrollContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val settingState: SettingState,
    val contentComponentRepository: ContentComponentRepository,
    val updateReadingProgress: (String, Float) -> Unit
) : ContentViewModel {
    private var progressScrollLoadJob: Job? = null
    private val loadChapterJobs: MutableList<Job> = mutableListOf()
    private var lazyColumnSize = IntSize(0, 0)
    private var lastWriteReadingProgress = 0L

    override val uiState: MutableScrollContentUiSate = MutableScrollContentUiSate(
        loadLastChapter = ::loadLastChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter,
        setLazyColumnSize = {
            lazyColumnSize = it
        },
        writeProgressRightNow = ::writeProgressRightNow,
        getContentData =  {
            contentComponentRepository.getContentDataFromJson(it)
        }
    )

    init {
        coroutineScope.launch {
            settingState.isUsingContinuousScrollingUserData.getFlowWithDefault(true).collect {
                if (it) {
                    progressScrollLoad()
                    if (uiState.contentList.size == 1) {
                        coroutineScope.launch(Dispatchers.Main) { changeChapter(uiState.readingContentId) }
                    }
                } else {
                    progressScrollLoadJob?.cancel()
                    if (uiState.contentList.size > 1) {
                        coroutineScope.launch(Dispatchers.Main) { changeChapter(uiState.readingContentId) }
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.firstVisibleItemScrollOffset }
                .throttleLatest(120L)
                .collect {
                    val layoutInfo = uiState.lazyListState.layoutInfo
                    val chapterId = uiState.readingChapterContent.id
                    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId } ?: return@collect

                    val newProgress = 1f.coerceAtMost((-item.offset + lazyColumnSize.height).toFloat() / item.size)
                    if (newProgress == uiState.readingProgress) return@collect
                    uiState.readingProgress = newProgress

                    val now = System.currentTimeMillis()
                    val scrolling = uiState.lazyListState.isScrollInProgress

                    if (scrolling && now - lastWriteReadingProgress < 2500 && newProgress < 1f) return@collect
                    lastWriteReadingProgress = now

                    coroutineScope.launch(Dispatchers.IO) { updateReadingProgress(chapterId, newProgress) }
                }
        }

        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { scrolling ->
                    if (!scrolling) {
                        val layoutInfo = uiState.lazyListState.layoutInfo
                        val chapterId = uiState.readingChapterContent.id
                        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId } ?: return@collect

                        val finalProgress = 1f.coerceAtMost(
                            (-item.offset + lazyColumnSize.height).toFloat() / item.size.coerceAtLeast(1)
                        )

                        if (uiState.readingProgress != finalProgress) {
                            uiState.readingProgress = finalProgress
                        }
                        coroutineScope.launch(Dispatchers.IO) { updateReadingProgress(chapterId, uiState.readingProgress) }
                        lastWriteReadingProgress = System.currentTimeMillis()
                    }
                }
        }
    }


    private fun writeProgressRightNow() {
        updateReadingProgress(uiState.readingChapterContent.id, uiState.readingProgress)
    }

    private fun progressScrollLoad() {
        progressScrollLoadJob?.cancel()
        progressScrollLoadJob = coroutineScope.launch {
            snapshotFlow { uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0) }.collect { itemInfo ->
                if (itemInfo?.key == uiState.readingChapterContent.lastChapter &&
                    lazyColumnSize.height != 0 &&
                    itemInfo.offset <= -lazyColumnSize.height &&
                    !uiState.readingChapterContent.isEmpty() &&
                    uiState.readingChapterContent.hasPrevChapter()
                ) {
                    if (uiState.contentList.getOrNull(uiState.contentList.size - 1)?.id == uiState.readingChapterContent.nextChapter)
                        uiState.contentList.removeAt(uiState.contentList.size - 1)
                    uiState.readingContentId = uiState.readingChapterContent.lastChapter
                    if (uiState.readingChapterContent.hasPrevChapter())
                        uiState.contentList.add(
                            0,
                            bookRepository.getStateChapterContent(
                                uiState.readingChapterContent.lastChapter,
                                uiState.bookId,
                                coroutineScope
                            )
                        )
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                        }
                    }
                    return@collect
                }
                if (
                    itemInfo?.key == uiState.readingChapterContent.nextChapter &&
                    !uiState.readingChapterContent.isEmpty() &&
                    uiState.readingChapterContent.hasNextChapter()
                ) {
                    if (uiState.contentList.getOrNull(0)?.id == uiState.readingChapterContent.lastChapter)
                        uiState.contentList.removeAt(0)
                    uiState.readingContentId = uiState.readingChapterContent.nextChapter
                    if (uiState.readingChapterContent.hasPrevChapter())
                        uiState.contentList.add(
                            bookRepository.getStateChapterContent(
                                uiState.readingChapterContent.nextChapter,
                                uiState.bookId,
                                coroutineScope
                            )
                        )
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                        }
                    }
                    return@collect
                }
            }
        }
    }

    override fun changeBookId(id: String) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        if (!uiState.readingChapterContent.hasNextChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.nextChapter
            )
        }
    }

    override fun loadLastChapter() {
        if (!uiState.readingChapterContent.hasPrevChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.lastChapter
            )
        }
    }

    @Suppress("AssignedValueIsNeverRead")
    override fun changeChapter(id: String) {
        loadChapterJobs.forEach(Job::cancel)
        uiState.contentList.clear()
        uiState.readingContentId = id
        uiState.readingProgress = 0f
        uiState.lazyListState = LazyListState()
        coroutineScope.launch(Dispatchers.IO) {
            val chapterContent = bookRepository.getChapterContent(id, uiState.bookId, WebDataSourcePriority.High)
            bookRepository.updateUserReadingData(uiState.bookId) {
                it.apply {
                    lastReadTime = LocalDateTime.now()
                    lastReadChapterId = id
                    lastReadChapterTitle = chapterContent.title
                }
            }
        }.let(loadChapterJobs::add)
        coroutineScope.launch(Dispatchers.IO) {
            val isCont = settingState.isUsingContinuousScrollingUserData.getOrDefault(true)
            bookRepository.getChapterContentFlow(id, uiState.bookId, coroutineScope).collect { chapterContent ->
                if (chapterContent.isEmpty() || chapterContent.id != uiState.readingContentId) return@collect
                if (chapterContent.content == uiState.readingChapterContent.content &&
                    chapterContent.title == uiState.readingChapterContent.title &&
                    chapterContent.lastChapter == uiState.readingChapterContent.lastChapter &&
                    chapterContent.nextChapter == uiState.readingChapterContent.nextChapter
                ) return@collect

                uiState.contentList.clear()
                if (chapterContent.hasPrevChapter() && isCont) {
                    uiState.contentList.add(
                        bookRepository.getStateChapterContent(
                            chapterContent.lastChapter,
                            uiState.bookId,
                            coroutineScope
                        )
                    )
                } else if (chapterContent.hasPrevChapter()) {
                    bookRepository.getChapterContent(chapterContent.lastChapter, uiState.bookId)
                }
                uiState.contentList.add(chapterContent)
                val userReadingData = bookRepository.getUserReadingData(uiState.bookId)
                coroutineScope.launch {
                    var flag = false
                    val itemIndex = uiState.contentList.indexOfFirst { it.id == id }
                    snapshotFlow { uiState.contentList[itemIndex] }.collect { content ->
                        if (flag) return@collect
                        if (content.isEmpty()) return@collect
                        if (itemIndex >= 0) {
                            uiState.lazyListState.requestScrollToItem(itemIndex)
                            val item = uiState.lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return@collect
                            val offset = -((item.size * (userReadingData.currentChapterReadingProgressMap[id] ?: 0f)).toInt() - lazyColumnSize.height)
                            uiState.lazyListState.requestScrollToItem(itemIndex, offset.debugPrint("ciallo"))
                        }
                        flag = true
                    }
                }.let(loadChapterJobs::add)
                if (chapterContent.hasNextChapter() && isCont) {
                    uiState.contentList.add(
                        bookRepository.getStateChapterContent(
                            chapterContent.nextChapter,
                            uiState.bookId,
                            coroutineScope,
                            WebDataSourcePriority.High
                        )
                    )
                } else if (chapterContent.hasNextChapter()) {
                    bookRepository.getChapterContent(chapterContent.nextChapter, uiState.bookId)
                }
                uiState.readingProgress = userReadingData.currentChapterReadingProgressMap[id] ?: 0f
            }
        }.let(loadChapterJobs::add)
    }
}