package com.toantran.trackme.extension

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

fun Context.saveImageToInternalStorage(bitmap: Bitmap, name: String): String {
    val contextWrapper = ContextWrapper(this)
    //Todo: change image dir name
    val directory: File = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)
    val filePath = File(directory, "${name}.jpg")
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(filePath)
        // Use the compress method on the BitMap object to write image to the OutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return filePath.absolutePath

}
