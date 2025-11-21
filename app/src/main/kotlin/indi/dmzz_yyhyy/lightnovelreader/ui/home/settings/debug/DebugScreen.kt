package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug

import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onClickBack: () -> Unit,
    onClickQuery: (String) -> Unit,
    onClickOpenBook: (String) -> Unit,
    result: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Debug",
                        style = AppTypography.titleTopBar,
                        fontWeight = FontWeight.W400,
                        color = colorScheme.onSurface,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClickBack) {
                        Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            var sqlCommand by remember { mutableStateOf("") }
            var bookId by remember { mutableStateOf("") }

            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = "打开书本",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                value = bookId,
                onValueChange = { bookId = it },
                label = { Text("书本ID") },
                placeholder = { Text("输入书本ID") },
                supportingText = { Text("输入书本ID") },
                maxLines = 1,
                interactionSource = interactionSource,
                trailingIcon = {
                    IconButton(onClick = { bookId = "" }) {
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
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        onClickOpenBook.invoke(bookId)
                    }
                ) {
                    Text(
                        text = "打开"
                    )
                }
            }

            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = "SQL调试",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                value = sqlCommand,
                onValueChange = { sqlCommand = it },
                label = { Text("SQL指令") },
                placeholder = { Text("输入SQL指令") },
                supportingText = { Text("输入SQL指令") },
                maxLines = 1,
                interactionSource = interactionSource,
                trailingIcon = {
                    IconButton(onClick = { sqlCommand = "" }) {
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
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        onClickQuery.invoke(sqlCommand)
                    }
                ) {
                    Text(
                        text = "执行"
                    )
                }
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = result
            )

            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = "崩溃测试",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            Column {
                SettingsClickableEntry(
                    modifier = Modifier.background(colorScheme.background),
                    title = "Crash by error()",
                    description = "error(\"Crashed\")",
                    onClick = {
                        error("Crashed")
                    }
                )
                SettingsClickableEntry(
                    modifier = Modifier.background(colorScheme.background),
                    title = "Crash by Lopper",
                    description = "Looper.getMainLooper().quit()",
                    onClick = {
                        Looper.getMainLooper().quit()
                    }
                )
                SettingsClickableEntry(
                    modifier = Modifier.background(colorScheme.background),
                    title = "Crash by NPE",
                    description = "NullPointerException",
                    onClick = {
                        throw NullPointerException()
                    }
                )
                SettingsClickableEntry(
                    modifier = Modifier.background(colorScheme.background),
                    title = "Crash by divide by zero",
                    description = "x = 1 / 0",
                    onClick = {
                        throw ArithmeticException(" / by zero")
                    }
                )
                SettingsClickableEntry(
                    modifier = Modifier.background(colorScheme.background),
                    title = "Crash by RuntimeException",
                    description = "throw RuntimeException(\"Crashed\")",
                    onClick = {
                        throw RuntimeException("Crashed")
                    }
                )
            }

        }
    }
}