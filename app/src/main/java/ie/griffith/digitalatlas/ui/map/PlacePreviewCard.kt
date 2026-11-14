package ie.griffith.digitalatlas.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.data.model.Place
import java.text.NumberFormat
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.FilledTonalButton

//the card that slides up from the bottom of the map after tapping somewhere,
//one composable handles every lookup state so the card stays in the same spot
@Composable
fun PlacePreviewCard(
    state: PlaceLookupState,
    //whether the place on the card is already saved, changes the pin button
    isPinned: Boolean,
    onSeeDetails: (Place) -> Unit,
    onPinClick: (Place) -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                when (state) {
                    PlaceLookupState.Loading -> LoadingContent()
                    is PlaceLookupState.Found -> FoundContent(state.place, isPinned, onSeeDetails, onPinClick)
                    PlaceLookupState.NothingHere -> MessageContent(
                        icon = Icons.Outlined.Waves,
                        title = stringResource(R.string.nothing_here_title),
                        message = stringResource(R.string.nothing_here_message)
                    )
                    PlaceLookupState.Error -> MessageContent(
                        icon = Icons.Outlined.CloudOff,
                        title = stringResource(R.string.lookup_error_title),
                        message = stringResource(R.string.lookup_error_message),
                        actionLabel = stringResource(R.string.retry),
                        onAction = onRetry
                    )
                    //never actually shown, the card is hidden when nothing is selected
                    PlaceLookupState.Idle -> Unit
                }
            }
            //close button in the top corner for every state
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.close))
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = stringResource(R.string.finding_place), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun FoundContent(
    place: Place,
    isPinned: Boolean,
    onSeeDetails: (Place) -> Unit,
    onPinClick: (Place) -> Unit
) {
    //end padding leaves room for the close button so long names don't run under it
    Text(
        text = place.name,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(end = 32.dp)
    )
    if (place.subtitle.isNotBlank()) {
        Text(
            text = place.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    place.population?.let { population ->
        //NumberFormat adds the right thousands separator for the user's region, e.g. 75,529
        Text(
            text = stringResource(R.string.population, NumberFormat.getIntegerInstance().format(population)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        //see details is the main action so it gets the filled button and the extra width
        Button(onClick = { onSeeDetails(place) }, modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.see_details))
        }
        //filled tonal "pinned" when it's saved, outlined "pin" when it isn't, tapping either toggles it
        if (isPinned) {
            FilledTonalButton(onClick = { onPinClick(place) }) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.pinned))
            }
        } else {
            OutlinedButton(onClick = { onPinClick(place) }) {
                Icon(
                    imageVector = Icons.Outlined.PushPin,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.pin))
            }
        }
    }
}

//shared layout for the "nothing here" and "error" states, the button only shows if there's an action
@Composable
private fun MessageContent(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(modifier = Modifier.padding(end = 32.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (actionLabel != null) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onAction) { Text(text = actionLabel) }
    }
}