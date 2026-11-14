package ie.griffith.digitalatlas.ui.pinned

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ie.griffith.digitalatlas.data.PinRepository
import ie.griffith.digitalatlas.data.local.PinStatus
import ie.griffith.digitalatlas.data.local.PinnedPlace
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//holds the pinned list and handles editing and removing pins
class PinnedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PinRepository(application)

    //null until the database has answered, so the screen shows a spinner instead of
    //flashing "no pinned places yet" for a split second
    val pins: StateFlow<List<PinnedPlace>?> = repository.allPins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    //the update part of CRUD
    fun updatePin(pin: PinnedPlace, status: PinStatus, note: String) {
        viewModelScope.launch { repository.update(pin, status, note) }
    }

    //the delete part of CRUD, only called after the user confirms in the dialog
    fun removePin(pin: PinnedPlace) {
        viewModelScope.launch { repository.unpin(pin) }
    }
}