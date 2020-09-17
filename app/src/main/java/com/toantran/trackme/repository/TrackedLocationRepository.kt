package com.toantran.trackme.repository

import androidx.lifecycle.LiveData
import com.toantran.trackme.db.dao.TrackedLocationDao
import com.toantran.trackme.db.entity.TrackedLocationEntity

class TrackedLocationRepository(
    private val trackedLocationDao: TrackedLocationDao
) {

    val allTrackedLocationDao: LiveData<List<TrackedLocationEntity>> = trackedLocationDao.getAll()

    suspend fun insert(trackedLocationEntity: TrackedLocationEntity)
            = trackedLocationDao.insert(trackedLocationEntity)

    suspend fun deleteAll() = trackedLocationDao.deleteAll()
}