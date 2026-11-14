package ie.griffith.digitalatlas.ui.map

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ie.griffith.digitalatlas.data.PinRepository
import ie.griffith.digitalatlas.data.local.PinnedPlace
import ie.griffith.digitalatlas.data.local.toPlace
import ie.griffith.digitalatlas.data.model.Place
import ie.griffith.digitalatlas.network.NominatimClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

//every state the preview card can be in, using a sealed interface so the
//card's when() has to handle all of them
sealed interface PlaceLookupState {
    data object Idle : PlaceLookupState
    data object Loading : PlaceLookupState
    data class Found(val place: Place) : PlaceLookupState
    data object NothingHere : PlaceLookupState
    data object Error : PlaceLookupState
}

//AndroidViewModel instead of ViewModel because the database needs the app's context
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val pinRepository = PinRepository(application)

    //every pinned place, so the map can draw a marker for each one.
    //stateIn turns room's flow into a value the screen can read straight away,
    //WhileSubscribed stops listening to the database 5 seconds after the map screen goes away
    val pins: StateFlow<List<PinnedPlace>> = pinRepository.allPins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //where the pin is, null when nothing is selected
    //private set so only the viewmodel can change it, the screen just reads it
    var selectedPoint by mutableStateOf<GeoPoint?>(null)
        private set

    var lookupState by mutableStateOf<PlaceLookupState>(PlaceLookupState.Idle)
        private set

    private var lookupJob: Job? = null
    private var lastRequestTime = 0L

    fun onMapTapped(lat: Double, lon: Double) {
        selectedPoint = GeoPoint(lat, lon)
        lookupState = PlaceLookupState.Loading

        //cancelling the previous lookup so a slow old answer can't overwrite a newer tap
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            //nominatim allows max 1 request per second, so waiting out the rest of the second if needed.
            //delay() gets cancelled by the next tap, so tapping quickly only sends the last one
            val wait = MIN_REQUEST_GAP_MS - (SystemClock.elapsedRealtime() - lastRequestTime)
            if (wait > 0) delay(wait)
            lastRequestTime = SystemClock.elapsedRealtime()

            lookupState = try {
                val place = NominatimClient.reverseGeocode(lat, lon)
                if (place == null) {
                    Log.d(TAG, "Nothing at $lat, $lon")
                    PlaceLookupState.NothingHere
                } else {
                    Log.d(TAG, "Found ${place.name}, ${place.subtitle}")
                    PlaceLookupState.Found(place)
                }
            } catch (e: CancellationException) {
                //rethrowing so cancelling a lookup doesn't get shown to the user as an error
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Lookup failed", e)
                PlaceLookupState.Error
            }
        }
    }

    //tapping a saved pin on the map shows its card straight away from the database,
    //no internet lookup needed
    fun onPinTapped(pinId: Long) {
        val pin = pins.value.find { it.id == pinId } ?: return
        lookupJob?.cancel()
        selectedPoint = GeoPoint(pin.latitude, pin.longitude)
        lookupState = PlaceLookupState.Found(pin.toPlace())
    }

    //the card's pin button, saves the place or removes it if it's already pinned
    fun togglePin(place: Place) {
        viewModelScope.launch {
            val existing = pins.value.find { it.latitude == place.latitude && it.longitude == place.longitude }
            if (existing == null) pinRepository.pin(place) else pinRepository.unpin(existing)
        }
    }

    //used by the retry button on the card when the lookup failed
    fun retry() {
        selectedPoint?.let { onMapTapped(it.latitude, it.longitude) }
    }

    //used by the close button on the card, removes the pin too.
    //leaving lookupState as it is so the card doesn't go blank while it slides away
    fun clearSelection() {
        lookupJob?.cancel()
        selectedPoint = null
    }

    companion object {
        private const val TAG = "DigitalAtlas"
        private const val MIN_REQUEST_GAP_MS = 1_000L
    }
}