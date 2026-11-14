package ie.griffith.digitalatlas.ui.pinned

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.data.local.PinnedPlace
import ie.griffith.digitalatlas.ui.components.EmptyState

//the pinned places tab: the read, update and delete parts of CRUD for the pinned_places table
@Composable
fun PinnedScreen(
    //MainScreen switches to the map tab and flies to the pin
    onPinClick: (PinnedPlace) -> Unit,
    viewModel: PinnedViewModel = viewModel()
) {
    val pins by viewModel.pins.collectAsStateWithLifecycle()
    //which pin's dialog is open, null means that dialog is closed
    var editingPin by remember { mutableStateOf<PinnedPlace?>(null) }
    var removingPin by remember { mutableStateOf<PinnedPlace?>(null) }

    //copying into a local val so kotlin can smart cast it after the null check
    val list = pins
    when {
        list == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        list.isEmpty() -> EmptyState(
            icon = Icons.Outlined.PushPin,
            title = stringResource(R.string.pinned_empty_title),
            message = stringResource(R.string.pinned_empty_message)
        )
        //LazyColumn only builds the rows that are on screen, so a long list stays smooth
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //key lets compose follow each row by its id, so removing one animates instead of redrawing them all
            items(list, key = { it.id }) { pin ->
                PinnedPlaceItem(
                    pin = pin,
                    onClick = { onPinClick(pin) },
                    onEdit = { editingPin = pin },
                    onRemove = { removingPin = pin },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    editingPin?.let { pin ->
        EditPinDialog(
            pin = pin,
            onDismiss = { editingPin = null },
            onSave = { status, note ->
                viewModel.updatePin(pin, status, note)
                editingPin = null
            }
        )
    }
    removingPin?.let { pin ->
        RemovePinDialog(
            pin = pin,
            onDismiss = { removingPin = null },
            onConfirm = {
                viewModel.removePin(pin)
                removingPin = null
            }
        )
    }
}