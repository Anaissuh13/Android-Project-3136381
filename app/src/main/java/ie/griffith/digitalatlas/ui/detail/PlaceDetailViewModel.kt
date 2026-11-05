package ie.griffith.digitalatlas.ui.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.griffith.digitalatlas.data.model.WikiSummary
import ie.griffith.digitalatlas.network.WikipediaClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

//what the facts tab can be showing
sealed interface FactsState {
    data object Loading : FactsState
    data class Loaded(val summary: WikiSummary) : FactsState
    data object NotFound : FactsState
    data object Error : FactsState
}

class PlaceDetailViewModel : ViewModel() {

    var factsState by mutableStateOf<FactsState>(FactsState.Loading)
        private set

    private var loadedTitle: String? = null

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