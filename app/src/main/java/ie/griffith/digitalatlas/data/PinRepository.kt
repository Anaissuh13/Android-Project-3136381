package ie.griffith.digitalatlas.data

import android.content.Context
import ie.griffith.digitalatlas.data.local.AtlasDatabase
import ie.griffith.digitalatlas.data.local.PinStatus
import ie.griffith.digitalatlas.data.local.PinnedPlace
import ie.griffith.digitalatlas.data.model.Place
import kotlinx.coroutines.flow.Flow

//the screens go through this instead of using the dao directly, same idea as SettingsRepository
class PinRepository(context: Context) {

    private val dao = AtlasDatabase.getInstance(context).pinnedPlaceDao()

    val allPins: Flow<List<PinnedPlace>> = dao.getAll()

    //null while the place isn't pinned
    fun pinFor(latitude: Double, longitude: Double): Flow<PinnedPlace?> =
        dao.findByLocation(latitude, longitude)

    //turning the place from the map into a database row, new pins start as "want to visit"
    suspend fun pin(place: Place) {
        dao.insert(
            PinnedPlace(
                name = place.name,
                subtitle = place.subtitle,
                country = place.country,
                latitude = place.latitude,
                longitude = place.longitude,
                wikipediaTitle = place.wikipediaTitle
            )
        )
    }

    suspend fun update(pin: PinnedPlace, status: PinStatus, note: String) {
        //copy() keeps every other column the same and only changes these two
        dao.update(pin.copy(status = status, note = note.trim()))
    }

    suspend fun unpin(pin: PinnedPlace) {
        dao.delete(pin)
    }
}