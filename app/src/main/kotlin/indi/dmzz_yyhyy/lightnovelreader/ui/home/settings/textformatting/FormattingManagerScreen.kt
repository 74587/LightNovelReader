package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormattingGroup
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFormattingScreen(
    onClickGroup: (String) -> Unit,
    onClickBack: () -> Unit,
    groups: List<FormattingGroup>,
    bookInformationMap: Map<String, BookInformation>
) {
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = enterAlwaysScrollBehavior,
                onClickBack = onClickBack
            )
        }
    ) { paddingValues ->
        val rules = groups.filter { it.id.isNotBlank() }
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { onClickGroup("") })
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.language_24px),
                            tint = colorScheme.secondary,
                            contentDescription = ""
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(
                        modifier = Modifier.weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.global_formatting_rules),
                            style = AppTypography.labelLarge,
                            maxLines = 1
                        )
                        Text(
                            text = stringResource(R.string.n_rules, groups.firstOrNull { it.id.isEmpty() }?.size ?: 0),
                            style = AppTypography.labelMedium,
                            color = colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = { onClickGroup("") }
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.arrow_forward_ios_24px),
                            tint = colorScheme.secondary,
                            contentDescription = ""
                        )
                    }
                }
            }
            if (rules.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        text = stringResource(R.string.book_rules),
                        style = AppTypography.titleSmall,
                        letterSpacing = 0.5.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            items(rules) { group ->
                bookInformationMap[group.id]?.let {
                    Group(
                        onClickGroup = { onClickGroup(group.id) },
                        formattingGroup = group,
                        bookInformation = it
                    )
                }
            }
            navigationBarSpacer()
        }
    }
}

@Composable
private fun Group(
    onClickGroup: (String) -> Unit,
    formattingGroup: FormattingGroup,
    bookInformation: BookInformation
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClickGroup(formattingGroup.id) })
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(
            width = 60.dp,
            height = 87.dp,
            uri = bookInformation.coverUri,
            rounded = 8.dp
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = bookInformation.title,
                style = AppTypography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = bookInformation.author,
                style = AppTypography.labelMedium,
                color = colorScheme.primary
            )
            Text(
                text = "${formattingGroup.size} 个规则",
                style = AppTypography.labelMedium,
                color = colorScheme.secondary
            )
        }
        IconButton(
            onClick = { onClickGroup(formattingGroup.id) }
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.arrow_forward_ios_24px),
                tint = colorScheme.secondary,
                contentDescription = "enter"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.settings_text_formatting),
                style = AppTypography.titleTopBar,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}