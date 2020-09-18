package com.toantran.trackme.ui.homeactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.repository.TrackedLocationRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    val recordedSessionDao = MyDatabase.getDatabase().recordedSessionDao()

    private val repository: TrackedLocationRepository

    val allRecordedSession = Pager(
        PagingConfig(
            pageSize = 10,
            enablePlaceholders = true,
            maxSize = 30
        )
    ) {
        recordedSessionDao.getAllPaging()
    }.flow

    init {
        val trackedLocationDao = MyDatabase.getDatabase().trackedLocationDao()
        repository = TrackedLocationRepository(trackedLocationDao)
    }

    fun deleteAllTrackedLocation() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

}