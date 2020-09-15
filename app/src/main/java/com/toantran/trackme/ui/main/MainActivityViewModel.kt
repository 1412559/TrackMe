package com.toantran.trackme.ui.main

import android.app.Application
import androidx.lifecycle.*
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.db.entity.TrackedLocationEntity
import com.toantran.trackme.extension.toFixed
import com.toantran.trackme.repository.TrackedLocationRepository

class MainActivityViewModel : ViewModel() {

    private val repository: TrackedLocationRepository

    val allTrackedLocation: LiveData<List<TrackedLocationEntity>>
    val distance: LiveData<Double>
    val velocity: LiveData<Float>

    init {
        val trackedLocationDao = MyDatabase.getDatabase().trackedLocationDao()
        repository = TrackedLocationRepository(trackedLocationDao)
        allTrackedLocation = repository.allTrackedLocationDao

        distance = allTrackedLocation.map {
            return@map (MapManager.computePolylineLength(it) / 1000).toFixed(2)
        }

        velocity = allTrackedLocation.map {
            if(it.isEmpty()) 0f else it.last().speed
        }
    }

}