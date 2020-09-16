package com.toantran.trackme.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.toantran.trackme.MyApplication

class TrackingLocationManager(
    private val mContext: Context,
    private val onLocationUpdate: (LocationResult?) -> Unit,
    private val onTrackingLocationStart: (Location) -> Unit) {

    private val TAG = TrackingLocationManager::class.java.name

    private val LOCATION_UPDATED_FREQUENCY_MIN = 10 * 1000
    private val LOCATION_UPDATED_FREQUENCY_MAX = 15 * 1000

    private val mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var requestingLocationUpdates = false

    init {
        // Get fusedLocationClient before doing anything related
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        // Init location request setting
        createLocationRequest()
        // init callback when new location updated.
        initLocationUpdatedCallback()
        resumeLocationUpdates()
    }

    fun destroy() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create().apply {
            // Todo: set smallest distance to notify location change
//        mLocationRequest.setSmallestDisplacement(30f);
            smallestDisplacement = 10f
            interval = 0L
            fastestInterval = 0L
//            interval = LOCATION_UPDATED_FREQUENCY_MAX.toLong()
//            fastestInterval = LOCATION_UPDATED_FREQUENCY_MIN.toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun initLocationUpdatedCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationUpdate(locationResult)
            }
        }
    }

    fun resumeLocationUpdates() {
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    locationCallback,
                    null
//                    Looper.getMainLooper()
                )
                val locationResult = mFusedLocationClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val lastKnownLocation = task.result
                        lastKnownLocation?.let {
                            onTrackingLocationStart(it)
                        }
                    }
                }
            } else {
                Log.d(TAG, "startLocationUpdates(): some permission not granted.")
            }
        }
    }

    fun stopLocationUpdates() {
        if (requestingLocationUpdates) {
            mFusedLocationClient.removeLocationUpdates(locationCallback)
            requestingLocationUpdates = false
        }
    }

}