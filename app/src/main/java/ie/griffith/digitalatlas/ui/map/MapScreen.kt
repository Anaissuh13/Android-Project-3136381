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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.data.model.Place
import android.graphics.ColorMatrixColorFilter
import org.osmdroid.views.CustomZoomButtonsDisplay
import org.osmdroid.views.overlay.TilesOverlay

//starting position, centred on ireland
private const val START_LAT = 53.4
private const val START_LON = -7.9
private const val START_ZOOM = 6.5

//Milestone 1 feedback correction
//dark mode filter for the map tiles. inverting on its own turned the sea brown and the land purple,
//so the colours are inverted and then rotated 180 degrees round the colour wheel, which brings
//the real colours back (blue sea, green parks) but dark. everything is then dimmed to 80%
//and a dark slate base is added so it matches the app's dark theme.
//each row works out one colour channel: red, green, blue, alpha (last number = amount added)
private val DarkMapFilter = ColorMatrixColorFilter(
    floatArrayOf(
        0.4592f, -1.1440f, -0.1152f, 0f, 226f,
        -0.3408f, -0.3440f, -0.1152f, 0f, 234f,
        -0.3408f, -1.1440f, 0.6848f, 0f, 240f,
        0f, 0f, 0f, 1f, 0f
    )
)

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    //"see details" on the card, MainActivity will open PlaceDetailActivity from this in the next step
    onSeeDetails: (Place) -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    //rememberSaveable survives switching tabs, so the map reopens where the user left it
    var savedLat by rememberSaveable { mutableDoubleStateOf(START_LAT) }
    var savedLon by rememberSaveable { mutableDoubleStateOf(START_LON) }
    var savedZoom by rememberSaveable { mutableDoubleStateOf(START_ZOOM) }

    //checking the theme's background instead of the phone setting,
    //so this still works once the user can pick light/dark in settings
    val darkMap = MaterialTheme.colorScheme.background.luminance() < 0.5f
    //coral from the theme for the pin so it stands out against the map
    val pinColor = MaterialTheme.colorScheme.tertiary.toArgb()
    //reading this here so the map gets updated whenever the selected point changes
    val selectedPoint = viewModel.selectedPoint

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

            //stacking the +/- buttons on the left edge in the middle of the screen,
            //so the preview card at the bottom never covers them and the user can zoom while it's open
            zoomController.display.setPositions(
                false,
                CustomZoomButtonsDisplay.HorizontalPosition.LEFT,
                CustomZoomButtonsDisplay.VerticalPosition.CENTER
            )

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
            //listening for taps, added at index 0 so it sits underneath everything else
            //and the pin on top still gets its own taps first
            overlays.add(0, MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    if (p == null) return false
                    viewModel.onMapTapped(p.latitude, p.longitude)
                    return true
                }

                //long press isn't used yet
                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }))
        }
    }

    //one marker for the selected place, moved around instead of making a new one on every tap
    val selectionMarker = remember(mapView) {
        Marker(mapView).apply {
            //anchoring at the bottom centre so the tip of the pin points at the exact spot
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            //the preview card shows the place info, so turning off osmdroid's default popup bubble
            infoWindow = null
            setOnMarkerClickListener { _, _ -> true }
        }
    }
    //only rebuilding the pin when the theme colour changes
    val pinIcon = remember(pinColor) {
        AtlasPinDrawable(
            fillColor = pinColor,
            dotColor = android.graphics.Color.WHITE,
            density = context.resources.displayMetrics.density
        )
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

    //box so the preview card can sit on top of the map
    Box(modifier = modifier.fillMaxSize()) {
        //wrapping osmdroid in AndroidView as it is a classic Android View, therefore sitting it inside Compose
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            //update runs again whenever the theme or the selected point changes
            update = { map ->
                //darkening the tiles in dark mode so the map isn't bright white
                map.overlayManager.tilesOverlay.setColorFilter(
                    if (darkMap) DarkMapFilter else null
                )
                //showing, moving or hiding the pin depending on what's selected
                if (selectedPoint == null) {
                    map.overlays.remove(selectionMarker)
                } else {
                    selectionMarker.position = selectedPoint
                    selectionMarker.icon = pinIcon
                    if (!map.overlays.contains(selectionMarker)) map.overlays.add(selectionMarker)
                }
                map.invalidate()
            }
        )

        //card slides up from the bottom when a place is selected and back down when it's closed
        AnimatedVisibility(
            visible = selectedPoint != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
            exit = slideOutVertically { fullHeight -> fullHeight } + fadeOut()
        ) {
            PlacePreviewCard(
                state = viewModel.lookupState,
                onSeeDetails = onSeeDetails,
                //pinning needs the database (milestone 2), so just letting the user know for now
                onPinClick = {
                    Toast.makeText(context, R.string.pin_coming_soon, Toast.LENGTH_SHORT).show()
                },
                onRetry = viewModel::retry,
                onClose = viewModel::clearSelection
            )
        }
    }

    //back button closes the card first instead of leaving the app
    BackHandler(enabled = selectedPoint != null) {
        viewModel.clearSelection()
    }
}