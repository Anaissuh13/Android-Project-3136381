package ie.griffith.digitalatlas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ie.griffith.digitalatlas.data.model.Place

//one row in the pinned_places table.
//the unique index on latitude + longitude stops the same place being pinned twice
@Entity(
    tableName = "pinned_places",
    indices = [Index(value = ["latitude", "longitude"], unique = true)]
)
data class PinnedPlace(
    //0 tells room to generate the id itself when the row is inserted
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subtitle: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    //kept so the details page can still load facts when opened from the pinned list
    val wikipediaTitle: String?,
    val status: PinStatus = PinStatus.WANT_TO_VISIT,
    val note: String = "",
    //saved as milliseconds since 1970, a plain number works on every android version
    val pinnedAt: Long = System.currentTimeMillis()
)


//turning a saved pin back into a Place, so the preview card and details page can show it
//without looking it up online again
fun PinnedPlace.toPlace() = Place(
    name = name,
    subtitle = subtitle,
    country = country,
    latitude = latitude,
    longitude = longitude,
    population = null,
    wikipediaTitle = wikipediaTitle
)