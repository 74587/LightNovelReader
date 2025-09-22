package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.markAllChaptersAsReadDialog() {
    dialog<Route.MarkAllChaptersAsReadDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<MarkAllChaptersAsReadDialogViewModel>()
        val route = it.toRoute<Route.MarkAllChaptersAsReadDialog>()
        val list = viewModel.bookVolumes.volumes.flatMap { it.chapters }
            .map { chapterInformation -> chapterInformation.id }
        viewModel.bookId = route.bookId
        MarkAllChaptersAsReadDialog(
            onDismissRequest = navController::popBackStack,
            onConfirmation = {
                viewModel.markAllChaptersAsRead()
                navController.popBackStack()
            },
            volumesList = list
        )
    }
}

fun NavController.navigateToMarkAllChaptersAsReadDialog(bookId: Int) {
    navigate(Route.MarkAllChaptersAsReadDialog(bookId))
}

@Composable
fun MarkAllChaptersAsReadDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    volumesList: List<Int>
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.mark_all_read)) },
        text = {
            Text(stringResource(R.string.mark_all_read_desc))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmation,
                enabled = volumesList.isNotEmpty()
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