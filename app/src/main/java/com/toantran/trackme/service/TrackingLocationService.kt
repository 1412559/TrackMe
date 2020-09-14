package com.toantran.trackme.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.toantran.trackme.db.MyDatabase
import com.toantran.trackme.db.dao.TrackedLocationDao
import com.toantran.trackme.db.entity.TrackedLocationEntity
import com.toantran.trackme.repository.TrackedLocationRepository
import kotlinx.coroutines.*
import java.util.*

class TrackingLocationService : Service() {

    private val TAG = TrackingLocationService::class.java.name

    private val CHANNEL_ID = "ChannelTrackingLocation"
    private val SERVICE_ID = 1

    private lateinit var trackingLocationManager: TrackingLocationManager
    private lateinit var repository: TrackedLocationRepository

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(intent: Intent): IBinder? {
        return null;
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
        // TODO: recheck
//        else {
//            startForeground(SERVICE_ID_1, Notification())
//        }
    }

    private fun setupTrackingLocationManager() {
        trackingLocationManager = TrackingLocationManager(
            this
        ) { locationResult ->
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
        }
    }

    override fun onDestroy() {
        trackingLocationManager.destroy()
        super.onDestroy()
    }


}
