package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import io.nightfish.lightnovelreader.api.book.BookVolumes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MarkAllChaptersAsReadDialogViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    var bookVolumes by mutableStateOf(BookVolumes.empty())
        private set

    var bookId = ""
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookVolumesFlow(bookId, viewModelScope).collect {
                    if (it.volumes.isEmpty()) return@collect
                    bookVolumes = it
                }
            }
            field = value
        }

    fun markAllChaptersAsRead() {
        if (bookId.isBlank()) return
        Log.i("MarkAllAsReadDialog", "Marked all chapters of book ($bookId) as read")
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                val allChapterIds = bookVolumes.volumes.flatMap { it.chapters }.map { it.id }
                userReadingData.apply {
                    lastReadTime = LocalDateTime.now()
                    lastReadChapterProgress = 1f
                    readCompletedChapterIds.clear()
                    readCompletedChapterIds.addAll(allChapterIds)
                    readingProgress = if (allChapterIds.isEmpty()) 0f else 1f
                }
            }
        }
    }
}

