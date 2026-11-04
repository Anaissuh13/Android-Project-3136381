package ie.griffith.digitalatlas.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        //scrollable so nothing gets cut off on small phones or in landscape
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(text = stringResource(R.string.appearance))
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            ThemeSelector(
                selected = themeMode,
                onSelect = onThemeModeChange,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionHeader(text = stringResource(R.string.sensors_section))
            ComingSoonSwitchRow(
                title = stringResource(R.string.shake_to_discover),
                description = stringResource(R.string.shake_to_discover_desc)
            )
            ComingSoonSwitchRow(
                title = stringResource(R.string.use_location),
                description = stringResource(R.string.use_location_desc)
            )

            SectionHeader(text = stringResource(R.string.about_section))
            AboutRow(
                title = stringResource(R.string.source_osm_title),
                description = stringResource(R.string.source_osm_desc)
            )
            AboutRow(
                title = stringResource(R.string.source_nominatim_title),
                description = stringResource(R.string.source_nominatim_desc)
            )
            AboutRow(
                title = stringResource(R.string.source_wikipedia_title),
                description = stringResource(R.string.source_wikipedia_desc)
            )
        }
    }
}

//light/dark/system as one segmented row, so only one can ever be picked
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        ThemeMode.LIGHT to R.string.theme_light,
        ThemeMode.DARK to R.string.theme_dark,
        ThemeMode.SYSTEM to R.string.theme_system
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, labelRes) ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                //itemShape rounds the outer corners of the first and last buttons only
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(text = stringResource(labelRes))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
    )
}

//switches for sensor features that aren't built yet, greyed out so it's clear they don't work yet
@Composable
private fun ComingSoonSwitchRow(title: String, description: String) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = {
            Column {
                Text(text = description)
                Text(
                    text = stringResource(R.string.coming_next_milestone),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        trailingContent = {
            Switch(checked = false, onCheckedChange = null, enabled = false)
        }
    )
}

@Composable
private fun AboutRow(title: String, description: String) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = description) }
    )
}