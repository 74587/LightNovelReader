package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.runtime.Composable
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState

@Composable
fun AppSettingsList(
    settingState: SettingState,
    onClickTranslateSettings: () -> Unit
) {
    SettingsClickableEntry(
        iconRes = R.drawable.translate_24px,
        title = "内容翻译",
        description = "使用基于 ML Kit 的章节内容翻译",
        onClick = onClickTranslateSettings,
        option = "不使用" /*"简体中文 → English"*/
    )
}