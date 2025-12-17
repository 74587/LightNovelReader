package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.licenses

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.util.author
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer

@Composable
fun LicensesScreen(
    onClickBack: () -> Unit
) {
    val libraries by produceLibraries(R.raw.aboutlibraries)
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopBar(onClickBack)
        LicenseList(libraries?.libraries ?: emptyList())
    }
}

@Composable
fun LicenseList(items: List<Library>) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item(key = "app_license_card") {
            AppLicenseCard(
                onClick = {
                    uriHandler.openUri("https://github.com/dmzz-yyhyy/LightNovelReader?tab=readme-ov-file#license")
                }
            )
        }

        items(
            items = items,
            key = { it.artifactId }
        ) { lib ->
            LicenseItem(lib)
        }

        navigationBarSpacer()
    }
}

@Composable
private fun LicenseCard(
    title: String,
    subtitle: String? = null,
    licenseType: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        shape = shape,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (!subtitle.isNullOrBlank() || !licenseType.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    if (!licenseType.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        LicenseTypeLabel(licenseType)
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseTypeLabel(text: String) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = colorScheme.outlineVariant,
                shape = shape
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppLicenseCard(onClick: (() -> Unit)? = null) {
    LicenseCard(
        title = stringResource(R.string.app_name),
        subtitle = "GNU General Public License",
        licenseType = "GPL",
        onClick = onClick
    )
}

@Composable
fun LicenseItem(lib: Library) {
    var showDialog by remember { mutableStateOf(false) }

    LicenseCard(
        title = lib.name,
        subtitle = lib.author,
        licenseType = lib.licenses.firstOrNull()?.name,
        onClick = { showDialog = true }
    )

    if (showDialog) LicenseDialog(lib = lib, onDismiss = { showDialog = false })
}


@Composable
fun LicenseDialog(
    lib: Library,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Column {
                Text(
                    text = lib.name,
                    style = typography.titleLarge,
                )
                if (lib.author.isNotBlank()) {
                    Text(
                        text = lib.author,
                        style = typography.labelLarge,
                        color = colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        text = {
            val licenseText = remember(lib) {
                buildString {
                    lib.licenses.forEach { lic ->
                        lic.licenseContent?.let { append(it + "\n\n") }
                    }
                }.trim()
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = licenseText.ifBlank { "No license text provided." },
                    style = typography.bodyMedium,
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_open_source_licenses),
                    style = typography.displayLarge,
                    fontWeight = FontWeight.W600,
                    color = colorScheme.onSurface)
            }
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
    )
}