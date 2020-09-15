package com.toantran.trackme.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.toantran.trackme.MyApplication
import com.toantran.trackme.R
import com.toantran.trackme.db.entity.TrackedLocationEntity

class MapManager {

    private var mMap: GoogleMap? = null
    private var polyline : Polyline? = null

    fun setMap(map: GoogleMap) {
        mMap = map
    }

    fun drawPolyline(listTrackedLocation: List<TrackedLocationEntity>) {
        if (polyline != null)
            polyline?.remove()

        polyline = mMap?.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(listTrackedLocation.map {
                    return@map LatLng(it.latitude, it.longitude)
                }.asIterable())
        )
        polyline?.startCap = CustomCap(
            BitmapDescriptorFactory.fromResource(R.drawable.pin_location)
        )
//        polyline?.endCap = CustomCap(
//            BitmapDescriptorFactory.fromResource(R.drawable.location)
//        )
    }

    companion object {
        fun computePolylineLength(listTrackedLocation: List<TrackedLocationEntity>) : Double {
            return SphericalUtil.computeLength(listTrackedLocation.map {
                return@map LatLng(it.latitude, it.longitude)
            })
        }
    }



    fun takeSnapshot(listTrackedLocation: List<TrackedLocationEntity>, onFinish: ((Bitmap)-> Unit)) {
        val builder: LatLngBounds.Builder = LatLngBounds.Builder()
        listTrackedLocation.forEach {
            builder.include(LatLng(it.latitude, it.longitude))
        }
        val padding = 50
        val bounds = builder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap?.animateCamera(cameraUpdate)
        mMap?.animateCamera(cameraUpdate, object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                mMap?.snapshot {
                    onFinish(it)
                }
            }
            override fun onCancel() {
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun requestPermissionLocationSuccess() {
        mMap?.isMyLocationEnabled = true

        val locationResult = LocationServices.getFusedLocationProviderClient(MyApplication.getInstance()).lastLocation
        locationResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val lastKnownLocation = task.result
                lastKnownLocation?.let {
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            it.latitude,
                            it.longitude), 12f))
                }
            }
        }
    }




}