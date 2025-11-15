package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        color = colorScheme.onSurfaceVariant,
        style = AppTypography.titleSmall,
        fontWeight = FontWeight.W600
    )
}
