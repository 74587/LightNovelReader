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
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingHomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val readingBooksUserData = userDataRepository.intListUserData(UserDataPath.ReadingBooks.path)
    var recentReadingBookIds: List<Int> by mutableStateOf(listOf())
        private set
    private val _recentReadingBookInformationMap = mutableStateMapOf<Int, BookInformation>()
    private val _recentReadingUserReadingDataMap = mutableStateMapOf<Int, UserReadingData>()
    val recentReadingBookInformationMap: Map<Int, BookInformation> = _recentReadingBookInformationMap
    val recentReadingUserReadingDataMap: Map<Int, UserReadingData> = _recentReadingUserReadingDataMap

    fun updateReadingBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            recentReadingBookIds = readingBooksUserData
                .getOrDefault(emptyList())
                .reversed()
                .filter { it != -1 }
        }
    }

    fun loadBookInfo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = bookRepository.getStateBookInformation(id, viewModelScope)
            val userData = bookRepository.getStateUserReadingData(id, viewModelScope)
            viewModelScope.launch(Dispatchers.Main) {
                _recentReadingBookInformationMap[id] = info
                _recentReadingUserReadingDataMap[id] = userData
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
