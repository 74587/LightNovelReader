package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val textProcessingRepository: TextProcessingRepository,
    private val exploreRepository: ExploreRepository
) : ViewModel() {
    private val _uiState = MutableExploreHomeUiState()
    private var workingExplorePageJob: Job? = null
    private var workingExploreBooksRowsJob: Job? = null
    val uiState: ExploreHomeUiState = _uiState

    fun init() {
        changePage(_uiState.selectedPage)
    }

    fun changePage(page: Int) {
        workingExplorePageJob?.cancel()
        workingExploreBooksRowsJob?.cancel()
        _uiState.selectedPage = page
        workingExplorePageJob = viewModelScope.launch {
            val selectedId = exploreRepository.explorePageIdList[page]
            val explorePageMap = exploreRepository.explorePageDataSourceMap
            _uiState.pageTitles = explorePageMap.map { it.value.title }
            workingExploreBooksRowsJob = viewModelScope.launch {
                explorePageMap[selectedId]?.getExplorePage()?.let { explorePage ->
                    _uiState.explorePageTitle = textProcessingRepository.processText { explorePage.title }
                    explorePage.rows.collect { exploreBooksRows ->
                        _uiState.explorePageBooksRawList =
                            exploreBooksRows.map { exploreBooksRow ->
                                exploreBooksRow.copy(
                                    bookList = exploreBooksRow.bookList.map {
                                        textProcessingRepository.processExploreBooksRow(it)
                                    }
                                )
                            }
                        }
                }
            }
        }
    }

    fun refresh() {
        _uiState.explorePageBooksRawList = emptyList()
        changePage(_uiState.selectedPage)
    }
}
