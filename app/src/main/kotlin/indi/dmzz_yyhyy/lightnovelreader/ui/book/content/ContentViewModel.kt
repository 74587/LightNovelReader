package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.throttleLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ContentViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val textProcessingRepository: TextProcessingRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val _uiState = MutableContentScreenUiState()
    private var _bookId: Int = -1
    private val readingBookListUserData = userDataRepository.intListUserData(UserDataPath.ReadingBooks.path)
    private var loadChapterContentJob: Job? = null
    val uiState: ContentScreenUiState = _uiState
    val settingState = SettingState(userDataRepository, viewModelScope)
    val coroutineScope = CoroutineScope(Dispatchers.IO)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            textProcessingRepository.updateFlow.collect {
                if (_bookId != -1 && uiState.chapterContent.id != -1)
                    loadChapterContent(_bookId, uiState.chapterContent.id)
            }
        }
    }

    @Suppress("DuplicatedCode")
    fun init(bookId: Int, chapterId: Int) {
        if (bookId != _bookId) {
            viewModelScope.launch {
                val bookVolumes = bookRepository.getBookVolumes(bookId)
                _uiState.bookVolumes = bookVolumes.first()
                viewModelScope.launch(Dispatchers.IO) {
                    bookVolumes.collect {
                        if (it.volumes.isEmpty()) return@collect
                        _uiState.bookVolumes = it
                    }
                }
            }
        }
        _bookId = bookId
        loadChapterContent(bookId, chapterId)
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingData(bookId).collect {
                _uiState.userReadingData = it
            }
        }
    }

    private fun loadChapterContent(bookId: Int, chapterId: Int) {
        loadChapterContentJob?.cancel()
        loadChapterContentJob = viewModelScope.launch(Dispatchers.IO) {
            val chapterContent = bookRepository.getChapterContent(
                chapterId = chapterId,
                bookId = bookId
            )
            chapterContent.collect { content ->
                if (content.id == -1) return@collect
                _uiState.chapterContent = content
                _uiState.isLoading = _uiState.chapterContent.id == -1
                bookRepository.updateUserReadingData(bookId) {
                    it.copy(
                        lastReadTime = LocalDateTime.now(),
                        lastReadChapterId = chapterId,
                        lastReadChapterTitle = _uiState.chapterContent.title,
                        lastReadChapterProgress = if (it.lastReadChapterId == chapterId) it.lastReadChapterProgress else 0f,
                    )
                }
                if (content.hasNextChapter()) {
                    bookRepository.getChapterContent(
                        chapterId = chapterId,
                        bookId = bookId
                    )
                }
            }
        }
    }

    fun lastChapter() {
        if (!_uiState.chapterContent.hasLastChapter()) return
        _uiState.isLoading = true
        viewModelScope.launch {
            init(
                bookId = _bookId,
                chapterId = _uiState.chapterContent.lastChapter
            )
        }
    }

    fun nextChapter() {
        if (!_uiState.chapterContent.hasNextChapter()) return
        _uiState.isLoading = true
        viewModelScope.launch {
            init(
                bookId = _bookId,
                chapterId = _uiState.chapterContent.nextChapter
            )
        }
    }

    fun changeChapter(chapterId: Int) {
        _uiState.isLoading = true
        viewModelScope.launch {
            init(
                bookId = _bookId,
                chapterId = chapterId
            )
        }
    }

    private val _readingProgressChannel = Channel<Float>(Channel.CONFLATED)

    init {
        coroutineScope.launch(Dispatchers.IO) {
            val progressFlow = _readingProgressChannel.receiveAsFlow()

            merge(
                progressFlow.throttleLatest(1000),
                progressFlow.debounce(200)
            )
                .distinctUntilChanged()
                .collectLatest { progress ->
                    saveReadingProgress(progress)
                }
        }
    }

    private fun saveReadingProgress(progress: Float) {
        if (progress.isNaN()) return

        val chapterId = _uiState.chapterContent.id
        val currentTime = LocalDateTime.now()

        bookRepository.updateUserReadingData(_bookId) { userReadingData ->
            val isChapterCompleted = progress > 0.945 &&
                    !userReadingData.readCompletedChapterIds.contains(chapterId)

            userReadingData.copy(
                lastReadTime = currentTime,
                lastReadChapterId = chapterId,
                lastReadChapterProgress = progress.coerceIn(0f..1f),
                readingProgress = if (isChapterCompleted) {
                    (userReadingData.readCompletedChapterIds.size + 1) /
                            _uiState.bookVolumes.volumes.sumOf { it.chapters.size }.toFloat()
                } else {
                    userReadingData.readCompletedChapterIds.size /
                            _uiState.bookVolumes.volumes.sumOf { it.chapters.size }.toFloat()
                },
                readCompletedChapterIds = if (isChapterCompleted) {
                    userReadingData.readCompletedChapterIds + chapterId
                } else {
                    userReadingData.readCompletedChapterIds
                }
            )
        }
    }

    fun changeChapterReadingProgress(progress: Float) {
        if (progress.isNaN()) return
        _readingProgressChannel.trySend(progress)
    }

    fun updateTotalReadingTime(bookId: Int, totalReadingTime: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) {
                it.copy(
                    lastReadTime = LocalDateTime.now(),
                    totalReadTime = it.totalReadTime + totalReadingTime
                )
            }
        }
    }

    fun addToReadingBook(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBookListUserData.update {
                val newList = it.toMutableList()
                if (it.contains(bookId))
                    newList.remove(bookId)
                newList.add(bookId)
                return@update newList
            }
        }
    }
}