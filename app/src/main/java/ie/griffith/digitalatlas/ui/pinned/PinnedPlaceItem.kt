package ie.griffith.digitalatlas.ui.pinned

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.data.local.PinStatus
import ie.griffith.digitalatlas.data.local.PinnedPlace
import java.text.DateFormat
import java.util.Date

//the text shown for each status, used by the list and the edit dialog
@StringRes
fun PinStatus.labelRes(): Int = when (this) {
    PinStatus.WANT_TO_VISIT -> R.string.status_want_to_visit
    PinStatus.VISITING -> R.string.status_visiting
}

//one pinned place in the list. tapping the card flies to it on the map,
//the two icons on the right edit or remove it
@Composable
fun PinnedPlaceItem(
    pin: PinnedPlace,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            //weight(1f) lets the text take the space left over after the two buttons
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pin.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (pin.subtitle.isNotBlank()) {
                    Text(
                        text = pin.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                StatusLabel(status = pin.status)
                if (pin.note.isNotBlank()) {
                    Text(
                        text = pin.note,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                //DateFormat writes the date the way the user's region expects, e.g. 14 Nov 2026
                Text(
                    text = stringResource(
                        R.string.pinned_on,
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(pin.pinnedAt))
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            //descriptions include the place name so a screen reader says "edit Galway", not just "edit"
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.edit_pin_cd, pin.name)
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.remove_pin_cd, pin.name)
                )
            }
        }
    }
}

//small coloured label for the status, teal for visiting and sand for want to visit.
//it only shows the status, changing it happens in the edit dialog
@Composable
private fun StatusLabel(status: PinStatus) {
    val visiting = status == PinStatus.VISITING
    Surface(
        shape = RoundedCornerShape(50),
        color = if (visiting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (visiting) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = stringResource(status.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}