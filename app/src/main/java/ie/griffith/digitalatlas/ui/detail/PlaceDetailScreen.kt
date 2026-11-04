package ie.griffith.digitalatlas.ui.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Attractions
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.ui.components.EmptyState
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

//the three tabs from the proposal
private enum class DetailTab(@StringRes val labelRes: Int) {
    FACTS(R.string.tab_facts),
    TO_VISIT(R.string.tab_to_visit),
    MY_ADDITIONS(R.string.tab_my_additions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    name: String,
    subtitle: String,
    latitude: Double,
    longitude: Double,
    onBack: () -> Unit
) {
    val tabs = DetailTab.entries
    //pager state is shared by the tab row and the pager so swiping and tapping stay in sync
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    //scope for animating the pager when a tab is tapped, animateScrollToPage is a suspend function
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        //automirrored so the arrow flips for right-to-left languages
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //header with the region and coordinates, the wikipedia photo goes above this tomorrow
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, style = MaterialTheme.typography.titleMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCoordinates(latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = stringResource(tab.labelRes)) }
                    )
                }
            }

            //pager only composes the page on screen (and its neighbours), so hidden tabs don't waste work
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (tabs[page]) {
                    DetailTab.FACTS -> EmptyState(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = stringResource(R.string.facts_placeholder_title),
                        message = stringResource(R.string.facts_placeholder_message)
                    )
                    DetailTab.TO_VISIT -> EmptyState(
                        icon = Icons.Outlined.Attractions,
                        title = stringResource(R.string.to_visit_placeholder_title),
                        message = stringResource(R.string.to_visit_placeholder_message)
                    )
                    DetailTab.MY_ADDITIONS -> EmptyState(
                        icon = Icons.Outlined.EditNote,
                        title = stringResource(R.string.additions_placeholder_title),
                        message = stringResource(R.string.additions_placeholder_message)
                    )
                }
            }
        }
    }
}

//turning 53.27, -9.05 into "53.2700° N, 9.0500° W", easier to read than negative numbers
private fun formatCoordinates(lat: Double, lon: Double): String {
    val latDirection = if (lat >= 0) "N" else "S"
    val lonDirection = if (lon >= 0) "E" else "W"
    return String.format(
        Locale.getDefault(),
        "%.4f° %s, %.4f° %s",
        abs(lat), latDirection, abs(lon), lonDirection
    )
}