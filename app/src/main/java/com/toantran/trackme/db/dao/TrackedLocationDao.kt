package com.toantran.trackme.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.toantran.trackme.db.entity.TrackedLocationEntity

@Dao
interface TrackedLocationDao {

    @Query("SELECT * from tracked_location_info_table ORDER BY id ASC")
    fun getAll(): LiveData<List<TrackedLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trackedLocationEntity: TrackedLocationEntity)

    @Query("DELETE FROM tracked_location_info_table")
    suspend fun deleteAll()
}