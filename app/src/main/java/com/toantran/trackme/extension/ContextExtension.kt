package com.toantran.trackme.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

fun Context.checkGpsStatus() {
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