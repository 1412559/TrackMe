package com.toantran.trackme.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.db.entity.TrackedLocationEntity
import com.toantran.trackme.repository.TrackedLocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class TrackingLocationService : Service() {

    private val TAG = TrackingLocationService::class.java.name

    private val CHANNEL_ID = "ChannelTrackingLocation"
    private val SERVICE_ID = 1

    private lateinit var trackingLocationManager: TrackingLocationManager
    private lateinit var repository: TrackedLocationRepository

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    private val recordTimeInSec = MutableLiveData<Int>(0)
    private var timer : Timer? = null

    // Binder given to clients
    private val mBinder: IBinder = MyBinder()

    /**
     * Class used for the client Binder. The Binder object is responsible for returning an instance
     * of "TrackingLocationService" to the client.
     */
    inner class MyBinder : Binder() {
        // Return this instance of TrackingLocationService so clients can call public methods
        val service: TrackingLocationService
            get() =this@TrackingLocationService
    }


    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        setupNotification()
        setupTrackingLocationManager()

        repository = TrackedLocationRepository(MyDatabase.getDatabase().trackedLocationDao())

        startTimer()

    }

    private fun setupNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Channel tracking location",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            val notification =
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("TrackMe")
                    .setContentText("Tracking location service is running")
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build()
            startForeground(SERVICE_ID, notification)
        }
    }

    private fun setupTrackingLocationManager() {
        trackingLocationManager = TrackingLocationManager(
            this,
            { locationResult ->
                locationResult?.locations?.forEach { location ->
                    Log.d(TAG,"Location get by Fuse: $location")
                    ioScope.launch {
                        repository.insert(TrackedLocationEntity(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            speed = location.speed,
                            distance = 0f,
                            duration = 0f,
                            currentTime = Date()
                        ))
                    }
                }
            },
            { location ->
                ioScope.launch {
                    repository.insert(
                        TrackedLocationEntity(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            speed = location.speed,
                            distance = 0f,
                            duration = 0f,
                            currentTime = Date()
                        )
                    )
                }
            }
        )
    }

    private fun startTimer() {
        timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                recordTimeInSec.postValue(recordTimeInSec.value!! + 1)
            }
        }
        timer?.schedule(timerTask, 0, 1000)
    }

    fun getRecordTimeInSec() : LiveData<Int> {
        return recordTimeInSec
    }

    fun pauseTracking() {
        timer?.cancel()
        timer = null
        trackingLocationManager.stopLocationUpdates()
    }

    fun resumeTracking() {
        if (timer == null)
            startTimer()
        trackingLocationManager.resumeLocationUpdates()
    }

    override fun onDestroy() {
        trackingLocationManager.destroy()
        timer?.cancel()
        super.onDestroy()
    }


}
