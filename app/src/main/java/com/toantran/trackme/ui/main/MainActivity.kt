package com.toantran.trackme.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.toantran.trackme.R
import com.toantran.trackme.extension.checkGpsStatus
import com.toantran.trackme.service.TrackingLocationService


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mapManager: MapManager

    private lateinit var mainActivityViewModel: MainActivityViewModel

    private val ACCESS_FINE_LOCATION_PERMISSION_CODE = 102
    private val FOREGROUND_SERVICE_PERMISSION_CODE = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermission()

        mapManager = MapManager()
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupAction()
        observeViewModel()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapManager.setMap(mMap)
        requestPermissionLocation();
    }

    override fun onDestroy() {
        stopTrackingLocationService()
        super.onDestroy()
    }

    private fun setupAction() {
        findViewById<Button>(R.id.btnTakeSnapShot).setOnClickListener {
            if (mainActivityViewModel.allTrackedLocation.value!!.isNotEmpty())
                mapManager.takeSnapshot(mainActivityViewModel.allTrackedLocation.value!!) {
                    findViewById<ImageView>(R.id.imageView).apply {
                        setImageBitmap(it)
                        visibility = View.VISIBLE
                    }
                }
        }
    }

    private fun observeViewModel() {
        mainActivityViewModel.allTrackedLocation.observe(this, Observer { listTrackedLocation ->
            findViewById<TextView>(R.id.locationTxt).text = "${listTrackedLocation.size} items"
            mapManager.drawPolyline(listTrackedLocation)
        })

        mainActivityViewModel.distance.observe(this, Observer { distance ->
            findViewById<TextView>(R.id.txtDistance).text = "${distance} km"
        })

        mainActivityViewModel.velocity.observe(this, Observer { velocity ->
            findViewById<TextView>(R.id.txtSpeed).text = "${velocity} km/h"
        })
    }

    private fun setupPermission() {
        (this as Context).checkGpsStatus()
        requestPermissionForegroundService()
    }

    private fun requestPermissionForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                    FOREGROUND_SERVICE_PERMISSION_CODE
                )
            }
        }
    }

    private fun requestPermissionLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION_PERMISSION_CODE
                )
                return
            }
        }
        mapManager.requestPermissionLocationSuccess()
        startTrackingLocationService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ACCESS_FINE_LOCATION_PERMISSION_CODE -> {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED)
                        requestPermissionLocation()
                }
            }
            FOREGROUND_SERVICE_PERMISSION_CODE -> {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED)
                        requestPermissionForegroundService()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    private var trackingLocationService: Intent? = null

    private fun startTrackingLocationService() {
        trackingLocationService = Intent(this, TrackingLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(trackingLocationService)
        } else {
            startService(trackingLocationService)
        }
    }

    private fun stopTrackingLocationService() {
        if (trackingLocationService != null) {
            stopService(trackingLocationService)
            trackingLocationService = null
        }
    }

}
