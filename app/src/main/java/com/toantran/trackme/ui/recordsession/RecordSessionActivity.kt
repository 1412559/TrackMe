package com.toantran.trackme.ui.recordsession

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.toantran.trackme.R
import com.toantran.trackme.extension.checkGpsStatus
import com.toantran.trackme.extension.saveImageToInternalStorage
import com.toantran.trackme.service.TrackingLocationService
import kotlinx.android.synthetic.main.activity_record_session.*
import java.util.*


class RecordSessionActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = RecordSessionActivity::class.java.name

    private lateinit var mMap: GoogleMap
    private lateinit var mapManager: MapManager

    private lateinit var recordSessionViewModel: RecordSessionViewModel

    private val ACCESS_FINE_LOCATION_PERMISSION_CODE = 102
    private val FOREGROUND_SERVICE_PERMISSION_CODE = 103

    private var mService: TrackingLocationService? = null
    private var mIsBound: Boolean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_session)

        setupPermission()

        mapManager = MapManager()
        recordSessionViewModel = ViewModelProvider(this).get(RecordSessionViewModel::class.java)

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

        btnPause.setOnClickListener {
            recordSessionViewModel.setRecordingStatus(false)
        }

        btnResume.setOnClickListener {
            recordSessionViewModel.setRecordingStatus(true)
        }

        btnStop.setOnClickListener {
            if (recordSessionViewModel.allTrackedLocation.value!!.isNotEmpty()){
                mapManager.takeSnapshot(recordSessionViewModel.allTrackedLocation.value!!) {
                    val path = this.saveImageToInternalStorage(it, Date().time.toString())
                    recordSessionViewModel.saveData(path)
                }
            } else {
                Toast.makeText(this,"No data to record!!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun observeViewModel() {
        recordSessionViewModel.allTrackedLocation.observe(this, Observer { listTrackedLocation ->
            mapManager.drawPolyline(listTrackedLocation)
        })

        recordSessionViewModel.getCurrentDistance().observe(this, Observer { distance ->
            txtDistance.text = "${distance} km"
        })

        recordSessionViewModel.getCurrentVelocity().observe(this, Observer { velocity ->
            txtSpeed.text = "${velocity} km/h"
        })

        recordSessionViewModel.getRecordingStatus().observe(this, Observer { isRecording ->
            btnPause.visibility = if(isRecording) View.VISIBLE else View.GONE
            btnResume.visibility = if(!isRecording) View.VISIBLE else View.GONE
            btnStop.visibility = if(!isRecording) View.VISIBLE else View.GONE

            if (isRecording) {
                mService?.resumeTracking()
            } else {
                mService?.pauseTracking()
            }
        })

        recordSessionViewModel.getCurrentDuration().observe(this, Observer { timeInSec ->
            txtDuration.text = "${timeInSec}s"
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


    private fun startTrackingLocationService() {
        Intent(this, TrackingLocationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopTrackingLocationService() {
        Intent(this, TrackingLocationService::class.java).also { intent ->
            unbindService(serviceConnection)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, iBinder: IBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.")
            // We've bound to MyService, cast the IBinder and get MyBinder instance
            val binder = iBinder as TrackingLocationService.MyBinder
            mService = binder.service
            mIsBound = true
            observeTimerFromService() // return a random number from the service
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "ServiceConnection: disconnected from service.")
            mIsBound = false
        }
    }

    private fun observeTimerFromService() {
        mService?.getRecordTimeInSec()?.observe(this, Observer { timeInSec ->
            recordSessionViewModel.setDuration(timeInSec)
            recordSessionViewModel.calculateVelocity()
        })
    }

}
