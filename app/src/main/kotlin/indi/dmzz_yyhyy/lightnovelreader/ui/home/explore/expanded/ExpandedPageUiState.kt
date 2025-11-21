package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

@State
interface ExpandedPageUiState {
    val pageTitle: String
    val filters: List<Filter>
    val bookList: List<BookInformation>
    val allBookshelfBookIds: List<String>
}

class MutableExpandedPageUiState : ExpandedPageUiState {
    override var pageTitle: String by mutableStateOf("")
    override var filters: SnapshotStateList<Filter> = mutableStateListOf()
    override var bookList: List<BookInformation> by mutableStateOf(emptyList())
    override var allBookshelfBookIds: List<String> by mutableStateOf(emptyList())
}