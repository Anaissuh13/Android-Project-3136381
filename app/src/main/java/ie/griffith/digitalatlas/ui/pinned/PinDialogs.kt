package ie.griffith.digitalatlas.ui.pinned

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.data.local.PinStatus
import ie.griffith.digitalatlas.data.local.PinnedPlace

//long enough for a reminder, short enough to fit on the list card
private const val MAX_NOTE_LENGTH = 200

//dialog for changing a pin's status and note
@Composable
fun EditPinDialog(
    pin: PinnedPlace,
    onDismiss: () -> Unit,
    onSave: (PinStatus, String) -> Unit
) {
    //starting from the pin's current values, keyed by id so opening another pin starts fresh.
    //rememberSaveable keeps what the user typed if the phone is rotated
    var status by rememberSaveable(pin.id) { mutableStateOf(pin.status) }
    var note by rememberSaveable(pin.id) { mutableStateOf(pin.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.edit_pin_title, pin.name)) },
        text = {
            Column {
                Text(text = stringResource(R.string.status_label), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                //filter chips so only one status can be picked, like the theme selector in settings
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PinStatus.entries.forEach { option ->
                        FilterChip(
                            selected = status == option,
                            onClick = { status = option },
                            label = { Text(text = stringResource(option.labelRes())) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = note,
                    //validating the input: stopping at the limit instead of letting the user type an essay
                    onValueChange = { if (it.length <= MAX_NOTE_LENGTH) note = it },
                    label = { Text(text = stringResource(R.string.note_label)) },
                    placeholder = { Text(text = stringResource(R.string.note_hint)) },
                    supportingText = {
                        Text(text = stringResource(R.string.note_counter, note.length, MAX_NOTE_LENGTH))
                    },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(status, note) }) {
                Text(text = stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

//asking before deleting, since removing a pin also throws away its note
@Composable
fun RemovePinDialog(
    pin: PinnedPlace,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) },
        title = { Text(text = stringResource(R.string.remove_pin_title, pin.name)) },
        text = { Text(text = stringResource(R.string.remove_pin_message)) },
        confirmButton = {
            //red text so it's clear this button deletes something
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.remove))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}