package ie.griffith.digitalatlas.ui.detail

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ie.griffith.digitalatlas.data.PinRepository
import ie.griffith.digitalatlas.data.local.PinnedPlace
import ie.griffith.digitalatlas.data.model.Place
import ie.griffith.digitalatlas.data.model.WikiSummary
import ie.griffith.digitalatlas.network.WikipediaClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//what the facts tab can be showing
sealed interface FactsState {
    data object Loading : FactsState
    data class Loaded(val summary: WikiSummary) : FactsState
    data object NotFound : FactsState
    data object Error : FactsState
}

//loads the wikipedia facts and keeps track of whether this place is pinned, survives rotation.
//AndroidViewModel because the pin database needs the app's context
class PlaceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val pinRepository = PinRepository(application)

    var factsState by mutableStateOf<FactsState>(FactsState.Loading)
        private set

    //the saved pin for this place, null while it isn't pinned
    var pin by mutableStateOf<PinnedPlace?>(null)
        private set

    private var loadedTitle: String? = null
    private var pinWatchJob: Job? = null

    //called when the screen opens, skips it if this article is already loaded,
    //e.g. after rotating the phone, so it doesn't download it again
    fun loadFacts(title: String) {
        if (title == loadedTitle) return
        loadedTitle = title
        fetch(title)
    }

    fun retry() {
        loadedTitle?.let { fetch(it) }
    }

    //listening to the database for this place, so the pin button stays right even if
    //the place gets pinned or unpinned somewhere else. only started once
    fun watchPin(latitude: Double, longitude: Double) {
        if (pinWatchJob != null) return
        pinWatchJob = viewModelScope.launch {
            pinRepository.pinFor(latitude, longitude).collect { pin = it }
        }
    }

    //the pin button in the top bar, saves the place or removes it if it's already saved
    fun togglePin(place: Place) {
        viewModelScope.launch {
            val current = pin
            if (current == null) pinRepository.pin(place) else pinRepository.unpin(current)
        }
    }

    private fun fetch(title: String) {
        factsState = FactsState.Loading
        viewModelScope.launch {
            factsState = try {
                val summary = WikipediaClient.getSummary(title)
                if (summary == null) FactsState.NotFound else FactsState.Loaded(summary)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("DigitalAtlas", "Wikipedia lookup failed for $title", e)
                FactsState.Error
            }
        }
    }
}