package com.toantran.trackme.repository

import androidx.lifecycle.LiveData
import com.toantran.trackme.db.dao.RecordedSessionDao
import com.toantran.trackme.db.entity.RecordedSessionEntity

class RecordedSessionRepository(
    private val recordedSessionDao: RecordedSessionDao
) {

    val allRecordedSessionDao: LiveData<List<RecordedSessionEntity>> = recordedSessionDao.getAll()

    suspend fun insert(recordedSessionEntity: RecordedSessionEntity)
            = recordedSessionDao.insert(recordedSessionEntity)
}