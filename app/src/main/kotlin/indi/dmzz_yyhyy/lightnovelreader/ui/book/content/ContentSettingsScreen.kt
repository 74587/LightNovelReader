package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSliderEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream

@Suppress("AnimateAsStateLabel")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    uiState: ContentScreenUiState,
    settingState: SettingState,
    onClickChangeBackgroundColor: () -> Unit,
    onClickChangeTextColor: () -> Unit
) {
    val isEnableIndicator = (settingState.enableBatteryIndicator
            || settingState.enableTimeIndicator
            || settingState.enableReadingChapterProgressIndicator)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 16.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .width(50.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) {
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        val animatedProgress by rememberInfiniteTransition(label = "animatedProgress").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ), label = "animatedProgressFloat"
        )

        val bgFlashColor by animateColorAsState(
            targetValue = if (animatedProgress > 0.5f) MaterialTheme.colorScheme.primary else Color.Transparent,
            animationSpec = tween(2000), label = "bgFlashColor"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 16.dp)
        ) {
            AnimatedVisibility(
                visible = sheetState.currentValue == Expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_settings_24px),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.settings_preview),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.W600
                        )
                    }
                    Box(
                        Modifier
                            .background(
                                if (selectedTabIndex == 2) bgFlashColor
                                else if (settingState.backgroundColor.isUnspecified) MaterialTheme.colorScheme.background
                                else settingState.backgroundColor
                            )
                    ) {
                        if (settingState.enableBackgroundImage)
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                painter =
                                    if (settingState.backgroundImageUri.toString().isEmpty()) painterResource(id = R.drawable.paper)
                                    else rememberAsyncImagePainter(settingState.backgroundImageUri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(
                                    top = animateDpAsState(
                                        targetValue = if (settingState.autoPadding) 12.dp else settingState.topPadding.dp,
                                        animationSpec = tween(300)
                                    ).value,
                                    start = animateDpAsState(
                                        targetValue = if (settingState.autoPadding) 16.dp else settingState.leftPadding.dp,
                                        animationSpec = tween(300)
                                    ).value,
                                    end = animateDpAsState(
                                        targetValue = if (settingState.autoPadding) 16.dp else settingState.rightPadding.dp,
                                        animationSpec = tween(300)
                                    ).value,
                                    bottom = animateDpAsState(
                                        targetValue = if (settingState.autoPadding) 16.dp else settingState.bottomPadding.dp,
                                        animationSpec = tween(300)
                                    ).value
                                )
                        ) {
                            Box {
                                ContentText(
                                    content = uiState.chapterContent.content,
                                    onClickLastChapter = { },
                                    onClickNextChapter = { },
                                    fontSize = settingState.fontSize.sp,
                                    fontLineHeight = settingState.fontLineHeight.sp,
                                    readingProgress = 0.2f,
                                    isUsingFlipPage = settingState.isUsingFlipPage,
                                    isUsingClickFlip = settingState.isUsingClickFlipPage,
                                    isUsingVolumeKeyFlip = settingState.isUsingVolumeKeyFlip,
                                    flipAnime = settingState.flipAnime,
                                    onChapterReadingProgressChange = { },
                                    paddingValues = PaddingValues(bottom = if (isEnableIndicator) 46.dp else 12.dp),
                                    autoPadding = settingState.autoPadding,
                                    fastChapterChange = settingState.fastChapterChange,
                                    changeIsImmersive = {},
                                    settingState = settingState
                                )
                            }
                            Indicator(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        if (settingState.autoPadding)
                                            PaddingValues(
                                                bottom = 8.dp,
                                                start = 16.dp,
                                                end = 16.dp
                                            )
                                        else PaddingValues(
                                            start = settingState.leftPadding.dp,
                                            end = settingState.rightPadding.dp
                                        )
                                    ),
                                enableBatteryIndicator = settingState.enableBatteryIndicator,
                                enableTimeIndicator = settingState.enableTimeIndicator,
                                enableChapterTitle = settingState.enableChapterTitleIndicator,
                                chapterTitle = uiState.chapterContent.title,
                                enableReadingChapterProgressIndicator = settingState.enableReadingChapterProgressIndicator,
                                readingChapterProgress = 0.33f
                            )

                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = sheetState.currentValue == PartiallyExpanded || sheetState.currentValue == Hidden,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(R.string.reader_settings),
                    fontWeight = FontWeight.W600
                )
            }
            ContentSettings(
                settingState = settingState,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { index -> selectedTabIndex = index },
                onClickChangeBackgroundColor = onClickChangeBackgroundColor,
                onClickChangeTextColor = onClickChangeTextColor
            )
        }
    }
}

data class TabItem(val title:String, val iconRes: Int)

@Composable
fun ContentSettings(
    settingState: SettingState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onClickChangeBackgroundColor: () -> Unit,
    onClickChangeTextColor: () -> Unit
) {
    val tabs = listOf(
        TabItem("外观", R.drawable.filled_menu_book_24px),
        TabItem("操作", R.drawable.settings_applications_24px),
        TabItem("边距", R.drawable.aspect_ratio_24px),
    )

    val pagerState = rememberPagerState(initialPage = selectedTabIndex, pageCount = { tabs.size })

    LaunchedEffect(selectedTabIndex) {
        pagerState.scrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    Column {
        TabsRow(
            tabs = tabs,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            userScrollEnabled = false
        ) { pageIndex ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                when (pageIndex) {
                    0 -> AppearancePage(
                        settingState,
                        onClickChangeBackgroundColor,
                        onClickChangeTextColor
                    )
                    1 -> ActionPage(settingState)
                    2 -> PaddingPage(settingState)
                }
            }
        }
    }
}

@Composable
fun TabsRow(
    tabs: List<TabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        selectedTabIndex = selectedTabIndex,
        indicator = { tabPositions ->
            SecondaryIndicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(6.dp)),
                content = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(tab.iconRes),
                            contentDescription = tab.title,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = tab.title,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            )
        }
    }
}


fun LazyListScope.AppearancePage(
    settingState: SettingState,
    onClickChangeBackgroundColor: () -> Unit,
    onClickChangeTextColor: () -> Unit
) {
    item {
        SettingsSliderEntry(
            iconRes = R.drawable.format_size_24px,
            title = stringResource(R.string.settings_reader_font_size),
            unit = "sp",
            valueRange = 8f..64f,
            value = settingState.fontSize,
            floatUserData = settingState.fontSizeUserData
        )
    }
    item {
        SettingsSliderEntry(
            iconRes = R.drawable.format_line_spacing_24px,
            title = stringResource(R.string.settings_reader_line_spacing),
            unit = "sp",
            valueRange = 0f..32f,
            value = settingState.fontLineHeight,
            floatUserData = settingState.fontLineHeightUserData
        )
    }
    item {
        SettingsSliderEntry(
            iconRes = R.drawable.format_bold_24px,
            title = "字重",
            unit = "",
            valueRange = 100f..900f,
            value = settingState.fontWeigh,
            valueFormat = { (it / 100).toInt() * 100f },
            floatUserData = settingState.fontWeighUserData
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.translate_24px,
            title = "简繁转换",
            description = "将内容从简体转换为繁体",
            checked = settingState.enableSimplifiedTraditionalTransform,
            booleanUserData = settingState.enableSimplifiedTraditionalTransformUserData,
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.translate_24px,
            title = "启用翻译",
            description = "将内容翻译为指定语言，可从设置调整相关选项",
            checked = settingState.enableMLTranslateKey,
            booleanUserData = settingState.enableMLTranslateUserData
        )
    }
    item {
        val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
        val background = MaterialTheme.colorScheme.background
        SettingsClickableEntry (
            modifier = Modifier.animateItem(),
            iconRes = R.drawable.palette_24px,
            title = "文字颜色",
            description = "自定义阅读器文字颜色",
            onClick = onClickChangeTextColor,
            trailingContent = {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(44.dp)
                ) {
                    drawCircle(
                        color = onSecondaryContainer,
                        radius = 20.dp.toPx(),
                    )
                    drawCircle(
                        color = background,
                        radius = 17.5.dp.toPx(),
                    )
                    drawCircle(
                        color = if (settingState.textColor.isUnspecified) background else settingState.textColor,
                        radius = 17.5.dp.toPx(),
                    )
                }
            }
        )
    }
    item {
        val textMeasurer = rememberTextMeasurer()
        val coroutineScope = rememberCoroutineScope()
        val content = LocalContext.current
        val launcher = uriLauncher {
            CoroutineScope(Dispatchers.IO).launch {
                val font = content.filesDir.resolve("readerTextFont")
                    .also {
                        if (it.exists()) {
                            it.delete()
                            it.createNewFile()
                        } else it.createNewFile()
                    }
                try {
                    content.contentResolver.openFileDescriptor(it, "r")
                        ?.use { parcelFileDescriptor ->
                            FileInputStream(parcelFileDescriptor.fileDescriptor).use { fileInputStream ->
                                fileInputStream.readBytes()
                            }.let(font::writeBytes)
                        }
                } catch (e: Exception) {
                    Log.e("ReaderTextFont", "failed to load chosen file")
                    e.printStackTrace()
                }
                try {
                    textMeasurer
                        .measure(
                            text = "",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(font))
                            )
                        )
                } catch (exception: Exception) {
                    coroutineScope.launch {
                        Toast.makeText(content, "字体文件错误或已损坏, 请您检查后导入", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                settingState.fontFamilyUriUserData.set(font.toUri())
            }
        }
        SettingsMenuEntry(
            modifier = Modifier.animateItem(),
            iconRes = R.drawable.text_fields_24px,
            title = "文本字体",
            description = "使用应用内置的字体或自定义字体文件",
            options = MenuOptions.SelectText,
            selectedOptionKey = if (settingState.fontFamilyUri.toString()
                    .isEmpty()
            ) MenuOptions.SelectText.Default else MenuOptions.SelectText.Customize,
            onOptionChange = {
                when (it) {
                    MenuOptions.SelectText.Default -> settingState.fontFamilyUriUserData.asynchronousSet(Uri.EMPTY)
                    MenuOptions.SelectText.Customize -> selectDataFile(launcher, "*/*")
                }
            }
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.lightbulb_24px,
            title = stringResource(R.string.settings_reader_keep_screen_on),
            description = stringResource(R.string.settings_reader_keep_screen_on_desc),
            checked = settingState.keepScreenOn,
            booleanUserData = settingState.keepScreenOnUserData,
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.imagesearch_roller_24px,
            title = "背景图片",
            description = "自定义阅读器背景图片",
            checked = settingState.enableBackgroundImage,
            booleanUserData = settingState.enableBackgroundImageUserData
        )
    }
    if (settingState.enableBackgroundImage) {
        item {
            val content = LocalContext.current
            val launcher = uriLauncher {
                CoroutineScope(Dispatchers.IO).launch {
                    val image = content.filesDir.resolve("readerBackgroundImage")
                        .also {
                            if (it.exists()) {
                                it.delete()
                                it.createNewFile()
                            }
                            else it.createNewFile()
                        }
                    try {
                        content.contentResolver.openFileDescriptor(it, "r")?.use { parcelFileDescriptor ->
                            FileInputStream(parcelFileDescriptor.fileDescriptor).use { fileInputStream ->
                                fileInputStream.readBytes()
                            }.let(image::writeBytes)
                        }
                    } catch (e: Exception) {
                        Log.e("ReaderBackground", "failed to load chosen file")
                        e.printStackTrace()
                    }
                    settingState.backgroundImageUriUserData.set(image.toUri())
                }
            }
            SettingsMenuEntry(
                modifier = Modifier.animateItem(),
                iconRes = R.drawable.drive_file_move_24px,
                title = "选择图片",
                description = "使用应用内置的图片背景或自定义图片文件",
                options = MenuOptions.SelectImage,
                selectedOptionKey = if (settingState.backgroundImageUri.toString().isEmpty()) MenuOptions.SelectImage.Default else MenuOptions.SelectImage.Customize,
                onOptionChange = {
                    when (it) {
                        MenuOptions.SelectImage.Default -> settingState.backgroundImageUriUserData.asynchronousSet(Uri.EMPTY)
                        MenuOptions.SelectImage.Customize -> selectDataFile(launcher, "image/*")
                    }
                }
            )
        }
        item {
            SettingsMenuEntry(
                modifier = Modifier.animateItem(),
                title = "背景显示模式",
                iconRes = R.drawable.insert_page_break_24px,
                description = "指定自定义背景图片的显示模式",
                options = MenuOptions.ReaderBgImageDisplayModeOptions,
                selectedOptionKey = settingState.backgroundImageDisplayMode,
                stringUserData = settingState.backgroundImageDisplayModeUserData
            )
        }
    }
    if (!settingState.enableBackgroundImage)
        item {
            val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
            val background = MaterialTheme.colorScheme.background
            SettingsClickableEntry (
                modifier = Modifier.animateItem(),
                iconRes = R.drawable.colorize_24px,
                title = "背景颜色",
                description = "自定义阅读器背景色",
                onClick = onClickChangeBackgroundColor,
                trailingContent = {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(44.dp)
                    ) {
                        drawCircle(
                            color = onSecondaryContainer,
                            radius = 20.dp.toPx(),
                        )
                        drawCircle(
                            color = background,
                            radius = 17.5.dp.toPx(),
                        )
                        drawCircle(
                            color = if (settingState.backgroundColor.isUnspecified) background else settingState.backgroundColor,
                            radius = 17.5.dp.toPx(),
                        )
                    }
                }
            )
        }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.battery_horiz_050_24px,
            title = stringResource(R.string.settings_reader_battery_indicator),
            description = stringResource(R.string.settings_reader_battery_indicator_desc),
            checked = settingState.enableBatteryIndicator,
            booleanUserData = settingState.enableBatteryIndicatorUserData,
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.outline_schedule_24px,
            title = stringResource(R.string.settings_reader_time_indicator),
            description = stringResource(R.string.settings_reader_time_indicator_desc),
            checked = settingState.enableTimeIndicator,
            booleanUserData = settingState.enableTimeIndicatorUserData,
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.contract_24px,
            title = stringResource(R.string.settings_reader_chapter_indicator),
            description = stringResource(R.string.settings_reader_chapter_indicator_desc),
            checked = settingState.enableChapterTitleIndicator,
            booleanUserData = settingState.enableChapterTitleIndicatorUserData,
        )
    }
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.clock_loader_40_24px,
            title = stringResource(R.string.settings_reader_progress_indicator),
            description = stringResource(R.string.settings_reader_progress_indicator_desc),
            checked = settingState.enableReadingChapterProgressIndicator,
            booleanUserData = settingState.enableReadingChapterProgressIndicatorUserData,
        )
    }
}

fun LazyListScope.ActionPage(settingState: SettingState) {
    item {
        SettingsSwitchEntry(
            iconRes = R.drawable.menu_book_24px,
            title = stringResource(R.string.settings_reader_page_mode),
            description = stringResource(R.string.settings_reader_page_mode_desc),
            checked = settingState.isUsingFlipPage,
            booleanUserData = settingState.isUsingFlipPageUserData,
        )
    }
    if (settingState.isUsingFlipPage) {
        item {
            SettingsSwitchEntry(
                modifier = Modifier.animateItem(),
                iconRes = R.drawable.auto_stories_24px,
                title = stringResource(R.string.settings_reader_volume_key_control),
                description = stringResource(R.string.settings_reader_volume_key_control_desc),
                checked = settingState.isUsingVolumeKeyFlip,
                booleanUserData = settingState.isUsingVolumeKeyFlipUserData,
            )
        }
    }
    if (settingState.isUsingFlipPage) {
        item {
            SettingsSwitchEntry(
                modifier = Modifier.animateItem(),
                iconRes = R.drawable.touch_app_24px,
                title = stringResource(R.string.settings_reader_t2tp),
                description = stringResource(R.string.settings_reader_t2tp_desc),
                checked = settingState.isUsingClickFlipPage,
                booleanUserData = settingState.isUsingClickFlipPageUserData,
            )
        }
    }
    if (settingState.isUsingFlipPage) {
        item {
            SettingsMenuEntry(
                modifier = Modifier.animateItem(),
                iconRes = R.drawable.transition_chop_24px,
                title = stringResource(R.string.settings_reader_page_turn_anim),
                description = stringResource(R.string.settings_reader_page_turn_anim_desc),
                options = MenuOptions.FlipAnimationOptions,
                selectedOptionKey = settingState.flipAnime,
                stringUserData = settingState.flipAnimeUserData
            )
        }
    }
    if (settingState.isUsingFlipPage) {
        item {
            SettingsSwitchEntry(
                modifier = Modifier.animateItem(),
                iconRes = R.drawable.quick_reorder_24px,
                title = stringResource(R.string.settings_reader_quick_chapter_switch),
                description = stringResource(R.string.settings_reader_quick_chapter_switch_desc),
                checked = settingState.fastChapterChange,
                booleanUserData = settingState.fastChapterChangeUserData,
            )
        }
    }
}

fun LazyListScope.PaddingPage(settingState: SettingState) {
    item {
        SettingsSwitchEntry(
            title = stringResource(R.string.settings_reader_auto_margin),
            description = stringResource(R.string.settings_reader_auto_margin_desc),
            checked = settingState.autoPadding,
            booleanUserData = settingState.autoPaddingUserData,
        )
    }
    if (!settingState.autoPadding) {
        item {
            SettingsSliderEntry(
                title = stringResource(R.string.settings_reader_top_margin),
                unit = "dp",
                valueRange = 0f..128f,
                value = settingState.topPadding,
                floatUserData = settingState.topPaddingUserData
            )
        }
    }
    if (!settingState.autoPadding) {
        item {
            SettingsSliderEntry(
                title = stringResource(R.string.settings_reader_bottom_margin),
                unit = "dp",
                valueRange = 0f..128f,
                value = settingState.bottomPadding,
                floatUserData = settingState.bottomPaddingUserData
            )
        }
    }
    if (!settingState.autoPadding) {
        item {
            SettingsSliderEntry(
                title = stringResource(R.string.settings_reader_left_margin),
                unit = "dp",
                valueRange = 0f..128f,
                value = settingState.leftPadding,
                floatUserData = settingState.leftPaddingUserData
            )
        }
    }
    if (!settingState.autoPadding) {
        item {
            SettingsSliderEntry(
                title = stringResource(R.string.settings_reader_right_margin),
                unit = "dp",
                valueRange = 0f..128f,
                value = settingState.rightPadding,
                floatUserData = settingState.rightPaddingUserData
            )
        }
    }
}

@Suppress("DuplicatedCode")
fun selectDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>, mime: String) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.pictures", "primary:Pictures")
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = mime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
    }
    launcher.launch(Intent.createChooser(intent, "选择背景图片"))
}