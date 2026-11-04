package ie.griffith.digitalatlas.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.TilesOverlay

//starting position, centred on ireland (need to change this to home country, maybe in milestone 2)
private const val START_LAT = 53.4
private const val START_LON = -7.9
private const val START_ZOOM = 6.5

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    //rememberSaveable survives switching tabs, so the map reopens where the user left it
    var savedLat by rememberSaveable { mutableDoubleStateOf(START_LAT) }
    var savedLon by rememberSaveable { mutableDoubleStateOf(START_LON) }
    var savedZoom by rememberSaveable { mutableDoubleStateOf(START_ZOOM) }

    //checking the theme's background instead of the phone setting,
    //so this still works once the user can pick light/dark in settings
    val darkMap = MaterialTheme.colorScheme.background.luminance() < 0.5f

    //creating the mapview once and remembering it, otherwise every recomposition would build a new map
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            //scaling tiles to the screen density, otherwise on high dpi phones the map text is tiny
            isTilesScaledToDpi = true
            //showing +/- buttons only when the map is touched, then fading them out,
            //so users who can't pinch easily can still zoom without the buttons cluttering the map
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT)
            //stopping the world from repeating sideways and scrolling into empty grey space
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            setScrollableAreaLimitLatitude(
                MapView.getTileSystem().maxLatitude,
                MapView.getTileSystem().minLatitude,
                0
            )
            minZoomLevel = 3.0
            maxZoomLevel = 19.0
            controller.setZoom(savedZoom)
            controller.setCenter(GeoPoint(savedLat, savedLon))
            //openstreetmap's tile policy requires the attribution to be visible on the map
            overlays.add(CopyrightOverlay(context))
            //saving the position every time the user drags or zooms
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    savedLat = mapCenter.latitude
                    savedLon = mapCenter.longitude
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    savedZoom = zoomLevelDouble
                    return false
                }
            })
        }
    }

    //pausing and resuming the map with the activity so it stops loading tiles in the background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            //freeing the map's tile threads and memory when leaving the map tab
            mapView.onDetach()
        }
    }

    //wrapping osmdroid in AndroidView as it is a classic Android View, therefore sitting it inside Compose
    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        //update runs again when the theme changes, inverting the tiles so the map isn't bright white in dark mode
        update = { map ->
            map.overlayManager.tilesOverlay.setColorFilter(
                if (darkMap) TilesOverlay.INVERT_COLORS else null
            )
            map.invalidate()
        }
    )
}