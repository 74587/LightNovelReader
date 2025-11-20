package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ChapterSheetUi(
    val bookId: Int,
    val readingChapterId: Int,
    val selectedVolumeId: Int = -1
)

@HiltViewModel
class ReadingHomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {

    private val readingBooksUserData =
        userDataRepository.intListUserData(UserDataPath.ReadingBooks.path)

    var recentReadingBookIds: List<Int> by mutableStateOf(listOf())
        private set

    private val _recentReadingBookInformationMap = mutableStateMapOf<Int, BookInformation>()
    private val _recentReadingUserReadingDataMap = mutableStateMapOf<Int, UserReadingData>()
    val recentReadingBookInformationMap: Map<Int, BookInformation> = _recentReadingBookInformationMap
    val recentReadingUserReadingDataMap: Map<Int, UserReadingData> = _recentReadingUserReadingDataMap

    private val loadingIds = mutableSetOf<Int>()

    private val _bookVolumesMap = mutableStateMapOf<Int, BookVolumes>()
    val bookVolumesMap: Map<Int, BookVolumes> = _bookVolumesMap

    var chapterSheetUi by mutableStateOf<ChapterSheetUi?>(null)
        private set

    fun openChapters(bookId: Int) {
        val userData = recentReadingUserReadingDataMap[bookId] ?: return
        chapterSheetUi = ChapterSheetUi(bookId, userData.lastReadChapterId)

        if (_bookVolumesMap.containsKey(bookId)) return
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumesFlow(bookId, viewModelScope)
                .collect { volumes ->
                    _bookVolumesMap[bookId] = volumes
                    if (volumes.volumes.isNotEmpty()) cancel()
                }
        }
    }

    fun setVolume(volumeId: Int) {
        chapterSheetUi = chapterSheetUi?.copy(selectedVolumeId = volumeId)
    }

    fun closeContents() {
        chapterSheetUi = null
    }

    fun updateReadingBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = readingBooksUserData
                .getOrDefault(emptyList())
                .reversed()
                .filter { it != -1 }

            withContext(Dispatchers.Main) {
                recentReadingBookIds = ids
            }
        }
    }

    fun loadBookInfo(id: Int) {
        val hasInfo = _recentReadingBookInformationMap[id] != null

        val canLoad = synchronized(loadingIds) {
            if (loadingIds.contains(id)) {
                false
            } else {
                loadingIds.add(id)
                true
            }
        }
        if (!canLoad) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val info: BookInformation? =
                    if (!hasInfo) bookRepository.getStateBookInformation(id, viewModelScope)
                    else null

                val userData: UserReadingData =
                    bookRepository.getStateUserReadingData(id, viewModelScope)

                withContext(Dispatchers.Main) {
                    if (info != null) {
                        _recentReadingBookInformationMap[id] = info
                    }
                    _recentReadingUserReadingDataMap[id] = userData
                }
            } finally {
                synchronized(loadingIds) {
                    loadingIds.remove(id)
                }
            }
        }
    }

    fun removeFromReadingList(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentList = readingBooksUserData.getOrDefault(emptyList()).toMutableList()

            if (bookId >= 0) {
                currentList.remove(bookId)
            } else {
                val restoredId = -bookId
                if (!currentList.contains(restoredId)) {
                    currentList.add(0, restoredId)
                }
            }

            readingBooksUserData.set(currentList)
            updateReadingBooks()
        }
    }
}
