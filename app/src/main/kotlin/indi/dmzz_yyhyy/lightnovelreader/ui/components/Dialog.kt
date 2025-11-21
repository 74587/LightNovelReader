package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem
import kotlin.math.round

@Composable
fun BaseDialog(
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dismissText: String,
    confirmationText: String,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseDialog(
        icon = icon,
        title = title,
        description = description,
        onDismissRequest = onDismissRequest,
    ) {
        content.invoke(this)
        Row(
            modifier = Modifier
                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = dismissText,
                    style = AppTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = confirmationText,
                    style = AppTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun BaseDialog(
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier
                .sizeIn(minWidth = 280.dp, maxWidth = 560.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Box(Modifier.height(24.dp))
            Icon(
                modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                painter = icon,
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = null
            )
            Box(Modifier.height(16.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = title,
                style = AppTypography.titleLarge,
                fontWeight = FontWeight.W500,
            )
            Box(Modifier.height(16.dp))
            Text(
                modifier = Modifier
                    .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Start,
                text = description,
                style = AppTypography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(Modifier.height(16.dp))
            content.invoke(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onSlideChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    title: String,
    description: String
) {
    BaseDialog(
        icon = painterResource(R.drawable.filled_settings_24px),
        title = title,
        description = description,
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.apply),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "字数限制：",
                    style = AppTypography.labelMedium
                )
                Spacer(Modifier.weight(1f))
                RollingNumber(
                    number = value.toInt(),
                    style = AppTypography.labelMedium,
                    separator = true
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "以上",
                    style = AppTypography.labelMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                valueRange = valueRange,
                steps = steps,
                onValueChange = { newValue ->
                    val adjustedValue = if (steps > 0) {
                        val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                        valueRange.start + round((newValue - valueRange.start) / stepSize) * stepSize
                    } else {
                        newValue
                    }
                    onSlideChange(adjustedValue)
                },
                onValueChangeFinished = onSliderChangeFinished,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        }
    }
}


interface ExportContext {
    val bookshelf: Boolean
    val readingData: Boolean
    val settings: Boolean
    val bookmark: Boolean
}

class MutableExportContext: ExportContext {
    override var bookshelf by mutableStateOf(true)
    override var readingData by mutableStateOf(true)
    override var settings by mutableStateOf(true)
    override var bookmark by mutableStateOf(true)
}

@Composable
fun ExportUserDataDialog(
    onDismissRequest: () -> Unit,
    onClickSaveAndSend: (ExportContext) -> Unit,
    onClickSaveToFile: (ExportContext) -> Unit
) {
    val mutableExportContext = remember { MutableExportContext() }
    val listItemModifier = Modifier
        .sizeIn(minWidth = 280.dp, maxWidth = 500.dp)
        .fillMaxWidth()
        .padding(horizontal = 14.dp)
    BaseDialog(
        icon = painterResource(R.drawable.output_24px),
        title = stringResource(R.string.settings_snap_data),
        description = stringResource(R.string.dialog_snap_user_data_text),
        onDismissRequest = onDismissRequest,
    ) {
        Column(Modifier.width(IntrinsicSize.Max).sizeIn(maxHeight = 350.dp)) {
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_bookshelf),
                supportingText = stringResource(R.string.dialog_snap_bookshelf_text),
                checked = mutableExportContext.bookshelf,
                onCheckedChange = { mutableExportContext.bookshelf = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_reading_data),
                supportingText = stringResource(R.string.dialog_snap_reading_data_text),
                checked = mutableExportContext.readingData,
                onCheckedChange = { mutableExportContext.readingData = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_settings),
                supportingText = stringResource(R.string.dialog_snap_settings_text),
                checked = mutableExportContext.settings,
                onCheckedChange = { mutableExportContext.settings = it }
            )
            /*HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_bookmarks),
                supportingText = stringResource(R.string.dialog_snap_bookmarks_text),
                checked = mutableExportContext.bookmark,
                onCheckedChange = { mutableExportContext.bookmark = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))*/
        }
        Row(
            modifier = Modifier
                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = AppTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(
                onClick = { onClickSaveAndSend(mutableExportContext) }
            ) {
                Text(
                    text = stringResource(R.string.export_and_share),
                    style = AppTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(
                onClick = { onClickSaveToFile(mutableExportContext) }
            ) {
                Text(
                    text = stringResource(R.string.export_to_file),
                    style = AppTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun SourceChangeDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    webDataSourceItems: List<WebDataSourceItem>,
    selectedWebDataSourceId: Int,
    onClickItem: (Int) -> Unit
) {
    BaseDialog(
        icon = painterResource(R.drawable.public_24px),
        title = stringResource(R.string.settings_select_data_source),
        description = stringResource(R.string.dialog_select_data_source_text),
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.switch_and_restart)
    ) {
        webDataSourceItems.forEachIndexed { index, webDataSourceItem ->
            RadioButtonListItem(
                modifier = Modifier
                    .sizeIn(minWidth = 280.dp, maxWidth = 500.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                title = webDataSourceItem.name,
                supportingText = stringResource(R.string.data_source_provider, webDataSourceItem.provider),
                selected = selectedWebDataSourceId == webDataSourceItem.id,
                onClick = { onClickItem(webDataSourceItem.id) }
            )
            if (index != webDataSourceItems.size - 1) {
                HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            }
        }
    }
}

@Composable
fun SettingsAboutInfoDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog (
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                Row {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = colorResource(id = R.color.ic_launcher_background),
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_foreground),
                            contentDescription = "appIcon",
                            modifier = Modifier.scale(1.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {

                        Text(
                            stringResource(id = R.string.app_name),
                            style = AppTypography.titleLarge
                        )
                        Text(
                            BuildConfig.APPLICATION_ID,
                            style = AppTypography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.settings_about_oss),
                    style = AppTypography.labelMedium
                )
                Spacer(Modifier.height(10.dp))
                val annotatedString = AnnotatedString.Companion.fromHtml(
                    htmlString = stringResource(
                        id = R.string.settings_about_source_code,
                        "<b><a href=\"https://github.com/dmzz-yyhyy/LightNovelReader\">GitHub</a></b>",
                        "<b><a href=\"https://github.com/dmzz-yyhyy/LightNovelReader/issues\">GitHub Issues</a></b>"
                    ),
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        pressedStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            background = MaterialTheme.colorScheme.secondaryContainer,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
                Text(
                    text = annotatedString,
                    style = AppTypography.bodyMedium
                )

                Spacer(modifier = Modifier.height(18.dp))

                val titleColor = MaterialTheme.colorScheme.onSurface
                val contentColor = MaterialTheme.colorScheme.secondary
                Column {
                    Text(
                        stringResource(R.string.dialog_about_version), color = titleColor
                    )
                    Text(
                        "${BuildConfig.VERSION_NAME} [${BuildConfig.VERSION_CODE}]", color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.translators), color = titleColor
                    )
                    Text(
                        stringResource(R.string.language_translators), color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {},
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Color) -> Unit,
    selectedColor: Color,
    colors: List<Color>,
) {
    var currentColor by remember {
        mutableStateOf(selectedColor)
    }

    BaseDialog (
        icon = painterResource(R.drawable.palette_24px),
        title = stringResource(R.string.dialog_color_picker),
        description = stringResource(R.string.dialog_color_picker_desc),
        onDismissRequest = onDismissRequest,
        onConfirmation = { onConfirmation(currentColor) },
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.apply),
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            currentColor = color
                        }
                ) {
                    val secondary = MaterialTheme.colorScheme.secondary
                    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
                    val blockIconId = painterResource(R.drawable.block_24px)
                    Canvas(
                        modifier = Modifier.size(44.dp)
                    ) {
                        if (color == currentColor)
                            drawCircle(
                                color = secondary,
                                radius = 22.dp.toPx(),
                            )
                        drawCircle(
                            color = surfaceContainer,
                            radius = 20.dp.toPx(),
                        )
                        drawCircle(
                            color = if (color.isUnspecified) surfaceContainer else color,
                            radius = 20.dp.toPx(),
                        )
                    }
                    if (index == 0)
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            painter = blockIconId,
                            contentDescription = null
                        )
                }
            }
        }
    }
}

@Composable
fun SliderValueDialog(
    value: Float,
    onValueChange: (Float) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: (Float) -> Unit
) {
    var text by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        text = value.toString()
    }

    val parsed = text.toFloatOrNull()
    val error = text.isNotBlank() && parsed == null

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.dialog_slider_custom)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Value") },
                    isError = error,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                if (error) {
                    Text(
                        text = stringResource(R.string.dialog_slider_custom_illegal_value),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    parsed?.let {
                        onValueChange(it)
                        onConfirmation(it)
                    }
                },
                enabled = parsed != null
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun DeleteBookshelfDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf),
                style = AppTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf_text),
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = stringResource(android.R.string.ok)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        }
    )
}
