package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentUiState
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.UserReadingData

@Stable
interface ReaderScreenUiState {
    val bookId: String
    val userReadingData: UserReadingData
    val bookVolumes: BookVolumes
    val contentUiState: ContentUiState
}

class MutableReaderScreenUiState(
    contentUiState: ContentUiState
): ReaderScreenUiState {
    override var bookId by mutableStateOf("")
    override var userReadingData by mutableStateOf(UserReadingData.empty())
    override var bookVolumes by mutableStateOf(BookVolumes.empty(""))
    override var contentUiState by mutableStateOf(contentUiState)
}