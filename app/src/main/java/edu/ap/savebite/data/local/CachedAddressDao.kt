package edu.ap.savebite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedAddressDao {
    @Query("SELECT * FROM CachedAddress WHERE `key` = :key")
    suspend fun get(key: String): CachedAddress?

    // REPLACE: a newer answer for the same place overwrites the old one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(address: CachedAddress)
}
