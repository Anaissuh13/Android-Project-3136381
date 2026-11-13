package ie.griffith.digitalatlas.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

//one fact or tip the user wrote about a place, used by the "my additions" tab next week.
//linked to a place by placeKey instead of the pin's id, so the user can add notes
//to a place without having to pin it first
@Entity(
    tableName = "user_additions",
    //index because every query looks additions up by their place
    indices = [Index(value = ["placeKey"])]
)
data class UserAddition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val placeKey: String,
    val type: AdditionType,
    val title: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long = System.currentTimeMillis()
)