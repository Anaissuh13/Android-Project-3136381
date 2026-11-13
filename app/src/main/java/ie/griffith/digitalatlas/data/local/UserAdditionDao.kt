package ie.griffith.digitalatlas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//database operations for the user's own facts and tips, the screen for these comes next week
@Dao
interface UserAdditionDao {

    @Query("SELECT * FROM user_additions WHERE placeKey = :placeKey ORDER BY createdAt DESC")
    fun getForPlace(placeKey: String): Flow<List<UserAddition>>

    @Insert
    suspend fun insert(addition: UserAddition): Long

    @Update
    suspend fun update(addition: UserAddition)

    @Delete
    suspend fun delete(addition: UserAddition)
}