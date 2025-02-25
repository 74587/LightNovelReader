package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.advanced.debug

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
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onClickBack: () -> Unit,
    onClickQuery: (String) -> Unit,
    result: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Debug",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSurface,
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
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            var sqlCommand by remember { mutableStateOf("") }
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
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                text = result
            )
        }
    }
}