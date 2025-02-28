package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.translate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    onClickBack: () -> Unit,
    settingState: SettingState,
    uiState: TranslateUiState,
    onClickDownloadModel: (String) -> Unit,
    onClickDeleteModel: (String) -> Unit
) {
    val availableLanguages = uiState.availableModelList
    val targetLanguage = settingState.translateTargetLanguageKey

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
                        .padding(horizontal = 14.dp, vertical = 12.dp)
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
                            text = "借助 Google 的机器学习翻译模型，下载所需语言的模型后即可使用基于本地的离线翻译功能。\n\n提示: Beta 版功能，可能影响部分内容显示。翻译质量取决于目标语言。",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

            }
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    text = "翻译设置",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.W600
                )
                SettingsSwitchEntry(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    title = "启用翻译",
                    description = "全局启用内容翻译",
                    checked = settingState.enableMLTranslateKey,
                    booleanUserData = settingState.enableMLTranslateUserData
                )
            }
            if (!settingState.enableMLTranslateKey || availableLanguages.isEmpty()) return@LazyColumn
            item {
                SettingsClickableEntry(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    iconRes = R.drawable.call_received_24px,
                    title = "源语言",
                    description = "*暂不支持更改该值",
                    option = "中文",
                    onClick = {  }
                )
                SettingsMenuEntry(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    iconRes = R.drawable.call_made_24px,
                    title = "目标语言",
                    options = MenuOptions.MLKitLangOptions,
                    selectedOptionKey = settingState.translateTargetLanguageKey,
                    stringUserData = settingState.translateTargetLanguageUserData
                )
            }
            item {
                AnimatedVisibility(
                    visible = !availableLanguages.contains(targetLanguage),
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        colors = CardDefaults.outlinedCardColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            containerColor = MaterialTheme.colorScheme.errorContainer,),) {
                        if (uiState.isDownloadingOrDeleting) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        MenuOptions.MLKitLangOptions
                                            .get(settingState.translateTargetLanguageKey).nameId
                                    ) + " 缺失模型",
                                    fontWeight = FontWeight.W600,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = stringResource(R.string.app_name) +" 将在下次使用翻译时为选定语言进行下载。你也可以手动下载。",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.W600,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Row {
                                Spacer(Modifier.weight(1f))
                                Button(
                                    onClick = {
                                        onClickDownloadModel(targetLanguage) },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    enabled = !uiState.isDownloadingOrDeleting
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.download_24px),
                                            "",
                                        )
                                        Text("下载")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    text = "已下载的模型",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.W600
                )
                Spacer(Modifier.height(8.dp))

            }
            items(uiState.availableModelList) { language->
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = stringResource(if (language == "zh") R.string.key_ml_kit_lang_zh
                            else MenuOptions.MLKitLangOptions.get(language).nameId)
                        )
                        Text(
                            text = language,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (language != "en" && language != "zh")
                        IconButton(
                            onClick = { onClickDeleteModel(language) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete_forever_24px),
                                tint = MaterialTheme.colorScheme.secondary,
                                contentDescription = null
                            )
                        }
                }
            }
        }
    }
}