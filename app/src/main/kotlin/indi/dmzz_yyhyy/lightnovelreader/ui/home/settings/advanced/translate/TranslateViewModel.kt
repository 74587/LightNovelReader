package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.AdvancedSettingState
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor (
    userDataRepository: UserDataRepository
) : ViewModel() {
    val settingState = AdvancedSettingState(userDataRepository, viewModelScope)

}