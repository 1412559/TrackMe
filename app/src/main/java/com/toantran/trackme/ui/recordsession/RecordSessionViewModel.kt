package com.toantran.trackme.ui.recordsession

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.db.entity.RecordedSessionEntity
import com.toantran.trackme.db.entity.TrackedLocationEntity
import com.toantran.trackme.extension.diffTimeInHours
import com.toantran.trackme.extension.toFixed
import com.toantran.trackme.repository.RecordedSessionRepository
import com.toantran.trackme.repository.TrackedLocationRepository
import kotlinx.coroutines.launch
import java.util.*

class RecordSessionViewModel : ViewModel() {

    private val repository: TrackedLocationRepository
    private val recordedSessionRepository: RecordedSessionRepository

    val allTrackedLocation: LiveData<List<TrackedLocationEntity>>

    private val mDistance : LiveData<Double>
    private val mVelocity = MutableLiveData<Float>()
    private val mDuration = MutableLiveData<Int>()

    private val isRecording = MutableLiveData<Boolean>()

    private val eventBackToHome = MutableLiveData<Boolean>()

    init {
        val trackedLocationDao = MyDatabase.getDatabase().trackedLocationDao()
        repository = TrackedLocationRepository(trackedLocationDao)

        allTrackedLocation = repository.allTrackedLocationDao

        val recordedSessionDao = MyDatabase.getDatabase().recordedSessionDao()
        recordedSessionRepository = RecordedSessionRepository(recordedSessionDao)


        mDistance = allTrackedLocation.map {
            // Todo: move to converter
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
            // Todo: define threshold time: 5000s
            else if (Date().time - it.last().currentTime.time > 5000)
                0f
            else {
                val endLocation = it.last()
                val startLocation = it[it.size - 2]
                val distance = MapManager.computeDistanceBetween(
                    LatLng(startLocation.latitude, startLocation.longitude),
                    LatLng(endLocation.latitude, endLocation.longitude)
                )
                // Todo: convert from m to km
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

    fun setDuration(duration: Int) {
        mDuration.value = duration
    }

    fun getCurrentDuration() : LiveData<Int> {
        return mDuration
    }

    fun saveData(mapImagePath: String) {
        viewModelScope.launch {
            val totalDistance = mDistance.value
            val totalDuration = mDuration.value
            // Todo: convert duration from second to hour
            val averageSpeed: Float =  if (totalDistance == null || totalDuration == null) 0f else (totalDistance * 3600 / totalDuration).toFloat()
            recordedSessionRepository.insert(
                RecordedSessionEntity(
                    averageSpeed = averageSpeed,
                    totalDistance = totalDistance ?: 0.0,
                    totalDuration = totalDuration ?: 0,
                    mapImagePath = mapImagePath
                )
            )
            repository.deleteAll()
            eventBackToHome.postValue(true)
        }
    }

    fun getEventBackToHome() : LiveData<Boolean> {
        return eventBackToHome
    }

}