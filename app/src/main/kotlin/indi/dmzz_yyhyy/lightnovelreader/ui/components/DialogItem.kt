package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    colors: ListItemColors = ListItemDefaults.colors(),
    supportingText: String,
    trailingContent: @Composable () -> Unit,
) {
    ListItem(
        modifier = modifier,
        colors = colors,
        headlineContent = {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        trailingContent = trailingContent
    )
}

@Composable
fun CheckBoxListItem(
    modifier: Modifier = Modifier,
    title: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier,
        title = title,
        supportingText = supportingText,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun RadioButtonListItem(
    modifier: Modifier = Modifier,
    title: String,
    supportingText: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier,
        title = title,
        supportingText = supportingText,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}