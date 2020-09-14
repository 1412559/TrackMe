package com.toantran.trackme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.db.entity.TrackedLocationEntity
import com.toantran.trackme.repository.TrackedLocationRepository

class MainActivityViewModel : ViewModel() {

    private val repository: TrackedLocationRepository

    val allTrackedLocation: LiveData<List<TrackedLocationEntity>>

    init {
        val trackedLocationDao = MyDatabase.getDatabase().trackedLocationDao()
        repository = TrackedLocationRepository(trackedLocationDao)
        allTrackedLocation = repository.allTrackedLocationDao
    }

}