package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier,
        color = colorScheme.primary,
        style = AppTypography.titleSmall,
        fontWeight = FontWeight.W600
    )
}
