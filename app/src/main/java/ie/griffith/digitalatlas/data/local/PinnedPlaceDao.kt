package ie.griffith.digitalatlas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//all the database operations for pinned places, room writes the actual code for these at build time
@Dao
interface PinnedPlaceDao {

    //newest pins first, flow means the list updates by itself whenever a pin is added, edited or removed
    @Query("SELECT * FROM pinned_places ORDER BY pinnedAt DESC")
    fun getAll(): Flow<List<PinnedPlace>>

    //used to check if the place on the card is already pinned
    @Query("SELECT * FROM pinned_places WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    fun findByLocation(lat: Double, lon: Double): Flow<PinnedPlace?>

    //ignoring the insert if this place is already pinned (the unique index would reject it)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(place: PinnedPlace): Long

    @Update
    suspend fun update(place: PinnedPlace)

    @Delete
    suspend fun delete(place: PinnedPlace)
}