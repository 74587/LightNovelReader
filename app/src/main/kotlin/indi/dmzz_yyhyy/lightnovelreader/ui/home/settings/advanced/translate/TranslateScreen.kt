package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.AdvancedSettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    onClickBack: () -> Unit,
    settingState: AdvancedSettingState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "内容翻译",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClickBack) {
                        Icon(
                            painterResource(
                            id = R.drawable.arrow_back_24px),
                            contentDescription = "back"
                        )
                    }
                },
                windowInsets =
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                ),
            )
        }
    ) { paddingValue ->
        LazyColumn(
            modifier = Modifier.padding(paddingValue)
        ) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painter = painterResource(R.drawable.info_24px), "hint", Modifier.size(18.dp))
                            Text("原理", fontWeight = FontWeight.W600)
                        }
                        Text(
                            text = "借助 Google 的机器学习翻译模型，下载所需语言的模型后即可使用基于本地的离线翻译功能。",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            item {
                SettingsSwitchEntry(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    title = "启用翻译",
                    description = "使用基于 ML Kit 的章节内容翻译",
                    checked = settingState.translateEnabled,
                    booleanUserData = settingState.translateEnabledUserData
                )
            }
            if (!settingState.translateEnabled) return@LazyColumn
            item {
                SettingsClickableEntry(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    iconRes = R.drawable.call_received_24px,
                    title = "translate_source_lang",
                    description = "RT",
                    option = "中文",
                    onClick = {  }
                )
                SettingsMenuEntry(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    iconRes = R.drawable.call_made_24px,
                    title = "translate_target_lang",
                    description = "RT",
                    options = MenuOptions.MLKitLangOptions,
                    selectedOptionKey = settingState.translateTargetLanguageKey,
                    stringUserData = settingState.translateTargetLanguageUserData
                )
            }
        }
    }
}