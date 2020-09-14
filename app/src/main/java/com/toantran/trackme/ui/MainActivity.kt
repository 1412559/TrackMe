package com.toantran.trackme.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.toantran.trackme.R
import com.toantran.trackme.service.TrackingLocationService

class MainActivity : AppCompatActivity() {

    private val ACCESS_FINE_LOCATION_PERMISSION_CODE = 102
    private val FOREGROUND_SERVICE_PERMISSION_CODE = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkGpsStatus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setupPermissionForegroundService()
        }

        // TODO: use when mapReady
        enableSetMyLocation();

        val mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        mainActivityViewModel.allTrackedLocation.observe(this, Observer { listTrackedLocation ->
            findViewById<TextView>(R.id.locationTxt).text = "${listTrackedLocation.size} items"
        })

    }

    private fun checkGpsStatus() {
        val manager =  getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val builder =
                AlertDialog.Builder(this)
            builder.setMessage("Please turn on GPS to continue!")
                .setCancelable(false)
                .setPositiveButton(
                    "Go to setting"
                ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupPermissionForegroundService() {
        if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.FOREGROUND_SERVICE),
                FOREGROUND_SERVICE_PERMISSION_CODE
            )
        }
    }
    
    override fun onDestroy() {
        stopTrackingLocationService()
        super.onDestroy()
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


    // TODO: use when mapReady
    private fun enableSetMyLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION_PERMISSION_CODE
                )
                return
            }
        }
//        mMap.setMyLocationEnabled(true)
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
                        enableSetMyLocation()
                }
            }
            FOREGROUND_SERVICE_PERMISSION_CODE -> {
                for (grantResult in grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            setupPermissionForegroundService()
                        }
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

}
