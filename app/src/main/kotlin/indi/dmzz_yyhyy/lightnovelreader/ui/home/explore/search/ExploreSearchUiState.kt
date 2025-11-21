package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.book.BookInformation

@State
interface ExploreSearchUiState {
    val isFocused: Boolean
    val isLoading: Boolean
    val isLoadingComplete: Boolean
    val historyList: List<String>
    val searchTypeIdList: List<String>
    val searchTypeNameMap: Map<String, String>
    val searchType: String
    val searchTip: String
    val searchResult: List<BookInformation>
    val allBookshelfBookIds: List<String>
}

class MutableExploreSearchUiState : ExploreSearchUiState {
    override var isFocused: Boolean by mutableStateOf(true)
    override var isLoading: Boolean by mutableStateOf(true)
    override var isLoadingComplete: Boolean by mutableStateOf(false)
    override var historyList: List<String> by mutableStateOf(mutableListOf())
    override var searchTypeIdList: List<String> by mutableStateOf(mutableListOf())
    override var searchTypeNameMap: Map<String, String> by mutableStateOf(mutableMapOf())
    override var searchType: String by mutableStateOf("")
    override var searchTip: String by mutableStateOf("")
    override var searchResult: List<BookInformation> by mutableStateOf(mutableListOf())
    override var allBookshelfBookIds: List<String> by mutableStateOf(mutableListOf())
}