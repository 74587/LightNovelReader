package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.translate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State

@State
interface TranslateUiState {
    val targetLanguage: String
    val isDownloadingOrDeleting: Boolean
    val availableModelList: List<String>
}

class MutableTranslateUiState: TranslateUiState {
    override val targetLanguage: String by mutableStateOf("")
    override var isDownloadingOrDeleting: Boolean by mutableStateOf(false)
    override var availableModelList: List<String> by mutableStateOf(emptyList())
}