package ie.griffith.digitalatlas.ui.pinned

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.ui.components.EmptyState

@Composable
fun PinnedScreen() {
    //showing the empty state for now, the pinned list needs the database which comes in milestone 2
    EmptyState(
        icon = Icons.Outlined.PushPin,
        title = stringResource(R.string.pinned_empty_title),
        message = stringResource(R.string.pinned_empty_message)
    )
}