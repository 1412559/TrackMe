package com.toantran.trackme.ui.main

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.db.entity.TrackedLocationEntity
import com.toantran.trackme.extension.diffTimeInHours
import com.toantran.trackme.extension.toFixed
import com.toantran.trackme.repository.TrackedLocationRepository
import java.util.*

class MainActivityViewModel : ViewModel() {

    private val repository: TrackedLocationRepository

    val allTrackedLocation: LiveData<List<TrackedLocationEntity>>
    private val mDistance : LiveData<Double>
    private val mVelocity = MutableLiveData<Float>()

    private val isRecording = MutableLiveData<Boolean>()

    init {
        val trackedLocationDao = MyDatabase.getDatabase().trackedLocationDao()
        repository = TrackedLocationRepository(trackedLocationDao)
        allTrackedLocation = repository.allTrackedLocationDao

        mDistance = allTrackedLocation.map {
            return@map (MapManager.computePolylineLength(it) / 1000).toFixed(2)
        }

        isRecording.value = true
    }

    fun getRecordingStatus() : LiveData<Boolean> {
        return isRecording
    }

    fun setRecordingStatus(isRecording: Boolean) {
        this.isRecording.value = isRecording
    }

    fun calculateVelocity() {
        val velocity = allTrackedLocation.value?.let {
            if (it.isEmpty() || it.size < 2)
                0f
            else if (Date().time - it.last().currentTime.time > 5000)
                0f
            else {
                val endLocation = it.last()
                val startLocation = it[it.size - 2]
                val distance = MapManager.computeDistanceBetween(
                    LatLng(startLocation.latitude, startLocation.longitude),
                    LatLng(endLocation.latitude, endLocation.longitude)
                )
                val distanceInKm = distance / 1000
                val time = endLocation.currentTime.diffTimeInHours(startLocation.currentTime)
                val velocity = distanceInKm.toFloat() / time
                velocity
            }
        }
        mVelocity.value = velocity?.toFixed(2) ?: 0f
    }

    fun getCurrentVelocity() : LiveData<Float> {
        return mVelocity
    }

    fun getCurrentDistance() : LiveData<Double> {
        return mDistance
    }

}