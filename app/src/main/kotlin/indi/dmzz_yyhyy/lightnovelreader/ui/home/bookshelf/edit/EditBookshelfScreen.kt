package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookshelfScreen(
    title: String,
    bookshelfId: Int,
    bookshelf: Bookshelf,
    init: (Int) -> Unit,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit,
    onClickDelete: (Int) -> Unit,
    onNameChange: (String) -> Unit,
    onAutoCacheChange: (Boolean) -> Unit,
    onSystemUpdateReminderChange: (Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isNameEmpty = bookshelf.name.isBlank()
    val bookshelfNamePlaceholder = stringResource(R.string.bookshelf_name_placeholder)
    val showErrorSnackbar = {
        showSnackbar(
            coroutineScope = coroutineScope,
            hostState = snackbarHostState,
            message = bookshelfNamePlaceholder,
        )
    }
    LaunchedEffect(bookshelfId) {
        init(bookshelfId)
    }
    val claim = LocalClaimSnackbarHost.current

    DisposableEffect(Unit) {
        claim(true)
        onDispose { claim(false) }
    }
    Scaffold(
        topBar = {
            TopBar(
                title = title,
                scrollBehavior = pinnedScrollBehavior,
                onClickBack = onClickBack,
                onClickSave = if (isNameEmpty) showErrorSnackbar else onClickSave
            )
        },
        snackbarHost = {
            SnackbarHost(LocalSnackbarHost.current)
        }
    ) {
        Column(Modifier.padding(it)) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                value = bookshelf.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name)) },
                supportingText = {
                    if (isNameEmpty) {
                        Text(
                            text = bookshelfNamePlaceholder,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.bodyMedium
                        )
                    }
                },
                maxLines = 1,
                interactionSource = interactionSource,
                isError = isNameEmpty,
                trailingIcon = {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(
                            painter = painterResource(R.drawable.cancel_24px),
                            contentDescription = "cancel",
                            tint =
                            if (isFocused) OutlinedTextFieldDefaults.colors().focusedTrailingIconColor
                            else OutlinedTextFieldDefaults.colors().unfocusedTrailingIconColor
                        )
                    }
                }
            )
            SectionHeader(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                text = stringResource(R.string.bookshelf_settings)
            )
            SwitchSettingItem(
                iconRes = R.drawable.cloud_download_24px,
                title = stringResource(R.string.settings_auto_cache),
                description = stringResource(R.string.settings_auto_cache_desc),
                value = bookshelf.autoCache,
                onValueChange = onAutoCacheChange
            )
            SwitchSettingItem(
                iconRes = R.drawable.outline_schedule_24px,
                title = stringResource(R.string.settings_book_update_reminder),
                description = stringResource(R.string.settings_book_update_reminder_desc),
                value = bookshelf.systemUpdateReminder,
                onValueChange = onSystemUpdateReminderChange
            )
            if (bookshelfId >= 0)
                ListItem(
                    modifier = Modifier.clickable {
                        onClickDelete(bookshelfId)
                    },
                    leadingContent = {
                        Icon(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            painter = painterResource(R.drawable.delete_forever_24px),
                            contentDescription = "Localized description",
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.settings_delete_bookshelf),
                            style = AppTypography.titleMedium,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(R.string.settings_delete_bookshelf_desc),
                            style = AppTypography.bodyMedium
                        )
                    },
                )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
    onClickSave: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = AppTypography.titleTopBar,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(painterResource(
                    id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            IconButton(onClickSave) {
                Icon(
                    painter = painterResource(R.drawable.save_24px),
                    contentDescription = "save"
                )
            }
        },
        windowInsets =
        WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SwitchSettingItem(
    iconRes: Int,
    title: String,
    description: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {onValueChange(!value)},
        headlineContent = { Text(text = title, style = AppTypography.titleMedium, modifier = Modifier.padding(bottom = 2.dp)) },
        supportingContent = { Text(text = description, style = AppTypography.bodyMedium) },
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = onValueChange
            )
        },
        leadingContent = {
            Icon(
                modifier = Modifier.padding(horizontal = 10.dp),
                painter = painterResource(iconRes),
                contentDescription = "Localized description",
            )
        },
    )
}