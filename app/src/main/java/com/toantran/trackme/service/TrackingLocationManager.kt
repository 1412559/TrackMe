package com.toantran.trackme.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.google.android.gms.location.*

class TrackingLocationManager(
    private val mContext: Context,
    private val onLocationUpdate: (LocationResult?) -> Unit) {

    private val TAG = TrackingLocationManager::class.java.name

    private val LOCATION_UPDATED_FREQUENCY_MIN = 10 * 1000
    private val LOCATION_UPDATED_FREQUENCY_MAX = 15 * 1000

    private val mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var requestingLocationUpdates = true

    init {
        // Get fusedLocationClient before doing anything related
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        // Init location request setting
        createLocationRequest()
        // init callback when new location updated.
        initLocationUpdatedCallback()
        resume()
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

    private fun resume() {
        if (requestingLocationUpdates) {
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
            } else {
                Log.d(TAG, "startLocationUpdates(): some permission not granted.")
            }
        }
    }

}