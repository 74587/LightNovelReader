package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.text.MLTranslatorRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    private val translatorRepository: MLTranslatorRepository
) : ViewModel() {
    val settingState = SettingState(userDataRepository, viewModelScope)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _uiState = MutableTranslateUiState()
    val uiState: TranslateUiState = _uiState

    init {
        coroutineScope.launch {
            refreshAvailableModels()
        }

    }

    private suspend fun refreshAvailableModels() {
        _uiState.availableModelList = translatorRepository.getAvailableLanguages()
    }

    fun downloadModel(language: String) {
        if (_uiState.isDownloadingOrDeleting) return
        _uiState.isDownloadingOrDeleting = true
        viewModelScope.launch {
            translatorRepository.downloadModel(language)
            refreshAvailableModels()
            _uiState.isDownloadingOrDeleting = false
        }
    }

    fun deleteModel(language: String) {
        if (_uiState.isDownloadingOrDeleting) return
        _uiState.isDownloadingOrDeleting = true

        coroutineScope.launch {
            translatorRepository.deleteModel(language)
            refreshAvailableModels()
            _uiState.isDownloadingOrDeleting = false
        }
    }
}
