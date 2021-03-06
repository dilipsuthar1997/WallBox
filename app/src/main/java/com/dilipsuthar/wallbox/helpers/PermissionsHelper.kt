package com.dilipsuthar.wallbox.helpers

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.dilipsuthar.wallbox.WallBox

object PermissionsHelper {

    fun permissionGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, WallBox.REQUEST_CODE)
    }

}