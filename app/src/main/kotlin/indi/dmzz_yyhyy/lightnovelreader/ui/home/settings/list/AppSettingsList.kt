package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions

@Composable
fun AppSettingsList(
    settingState: SettingState,
    onClickTranslateSettings: () -> Unit
) {
    SettingsClickableEntry(
        iconRes = R.drawable.translate_24px,
        title = "内容翻译 [Beta]",
        description = "使用基于本地模型的内容翻译",
        onClick = onClickTranslateSettings,
        option = if (settingState.enableMLTranslateKey) "目标: " +
                stringResource(
                    MenuOptions.MLKitLangOptions
                        .get(settingState.translateTargetLanguageKey).nameId
                )
            else stringResource(R.string.disabled)
    )
}