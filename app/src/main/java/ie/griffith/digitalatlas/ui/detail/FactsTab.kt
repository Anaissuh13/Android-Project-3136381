package ie.griffith.digitalatlas.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.data.model.WikiSummary
import ie.griffith.digitalatlas.network.AtlasImageLoader
import ie.griffith.digitalatlas.ui.components.EmptyState

@Composable
fun FactsTab(state: FactsState, onRetry: () -> Unit) {
    when (state) {
        FactsState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is FactsState.Loaded -> FactsContent(state.summary)
        FactsState.NotFound -> EmptyState(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            title = stringResource(R.string.facts_not_found_title),
            message = stringResource(R.string.facts_not_found_message)
        )
        FactsState.Error -> EmptyState(
            icon = Icons.Outlined.CloudOff,
            title = stringResource(R.string.facts_error_title),
            message = stringResource(R.string.facts_error_message),
            actionLabel = stringResource(R.string.retry),
            onAction = onRetry
        )
    }
}

@Composable
private fun FactsContent(summary: WikiSummary) {
    val context = LocalContext.current
    //scrollable since some summaries are a few paragraphs long
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        //the only photo in the app, loaded by coil off the main thread and cached
        summary.thumbnailUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = stringResource(R.string.place_photo, summary.title),
                imageLoader = AtlasImageLoader.get(context),
                //crop fills the box without stretching the photo
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    //grey box shows while the photo is still loading
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        summary.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(text = summary.extract, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        //wikipedia's licence asks for attribution
        Text(
            text = stringResource(R.string.from_wikipedia),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}