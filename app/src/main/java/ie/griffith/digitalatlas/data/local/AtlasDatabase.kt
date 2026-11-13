package ie.griffith.digitalatlas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//the app's sqlite database, room creates the tables from the two entity classes.
//version goes up whenever a table changes, so existing users' data can be migrated
@Database(
    entities = [PinnedPlace::class, UserAddition::class],
    version = 1,
    exportSchema = false
)
abstract class AtlasDatabase : RoomDatabase() {

    abstract fun pinnedPlaceDao(): PinnedPlaceDao
    abstract fun userAdditionDao(): UserAdditionDao

    companion object {
        //volatile so every thread sees the instance as soon as it's created
        @Volatile
        private var instance: AtlasDatabase? = null

        //one database for the whole app, opening several copies wastes memory and can clash.
        //synchronized stops two threads creating it at the same time
        fun getInstance(context: Context): AtlasDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AtlasDatabase::class.java,
                    "digital_atlas.db"
                ).build().also { instance = it }
            }
    }
}