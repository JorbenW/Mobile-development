package edu.ap.savebite.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The only Room database of the app, as a singleton (lab Room).
 * FavoriteStore and CachedReservation are added in the coming weeks.
 */
@Database(entities = [CachedAddress::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedAddressDao(): CachedAddressDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savebite.db"
                )
                    // Room codelab: while developing, a new version simply starts with an empty database
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
