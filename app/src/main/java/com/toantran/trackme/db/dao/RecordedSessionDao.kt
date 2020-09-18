package com.toantran.trackme.db.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.toantran.trackme.db.entity.RecordedSessionEntity

@Dao
interface RecordedSessionDao {

    @Query("SELECT * from recorded_session_table ORDER BY id DESC")
    fun getAll(): LiveData<List<RecordedSessionEntity>>

    @Query("SELECT * from recorded_session_table ORDER BY id COLLATE NOCASE DESC")
    fun getAllPaging(): PagingSource<Int, RecordedSessionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recordedSessionEntity: RecordedSessionEntity)

    @Query("DELETE FROM recorded_session_table")
    suspend fun deleteAll()
}