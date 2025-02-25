package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope

@Stable
class AdvancedSettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val translateEnabledUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.TranslateEnabled.path)
    val translateTargetLanguageUserData = userDataRepository.stringUserData(UserDataPath.Settings.App.TranslateTargetLanguage.path)

    val translateEnabled by translateEnabledUserData.asState(false)
    val translateTargetLanguageKey by translateTargetLanguageUserData.asState("en")
}