package com.dilipsuthar.wallbox.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.TypedValue
import android.view.Menu
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dilipsuthar.wallbox.R
import java.nio.file.Files.size
import java.text.NumberFormat
import java.util.*
import java.util.Map
import kotlin.math.abs


object Tools {

    const val GONE = 0
    const val INVISIBLE = 1

    fun setSystemBarColor(act: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = color
        }
    }

    fun setNavigationBarColor(act: Activity, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.navigationBarColor = color
        }
    }

    fun changeNavigationIconColor(toolbar: Toolbar, @ColorInt color: Int) {
        val drawable = toolbar.navigationIcon
        drawable?.mutate()
        drawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun changeMenuIconColor(menu: Menu, @ColorInt color: Int) {
        for (i in 0 until menu.size()) {
            val drawable = menu.getItem(i).icon ?: continue
            drawable.mutate()
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    fun setSystemBarLight(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            act.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun clearSystemBarLight(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            act.window.decorView.systemUiVisibility = 0
        }
    }

    fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    fun visibleViews(vararg views: View) {
        for (v in views) {
            v.visibility = View.VISIBLE
        }
    }

    fun inVisibleViews(vararg views: View, type: Int) {

        if (type == INVISIBLE) {
            for (v in views) {
                v.visibility = View.INVISIBLE
            }
        } else {
            for (v in views) {
                v.visibility = View.GONE
            }
        }
    }

    fun setSnackBarBgColor(ctx: Context, snackBar: Snackbar, color: Int) {
        val sbView = snackBar.view
        sbView.setBackgroundColor(ctx.resources.getColor(color))
    }

    fun setSnackBarDrawable(snackBar: Snackbar, drawable: Drawable?) {
        val view = snackBar.view
        view.background = drawable
    }

    fun hasNetwork(ctx: Context?): Boolean? {
        var isConnected: Boolean? = false
        val connectivityManager = ctx?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = true
        return isConnected
    }

    // Lambda function
    /*val hasActiveNetwork: (Context) -> Boolean? = { ctx ->
        var isConnected: Boolean? = false
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected)
            isConnected = true
        isConnected
    }*/

    fun copyToClipboard(context: Context, data: String, msg: String) {
        val clip = "clipboard"
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText(clip, data))
        PopupUtils.showToast(context, "$msg copied to clipboard", Toast.LENGTH_SHORT)
    }

    fun getAppVersion(context: Context): String? {
        val packageManager = context.packageManager
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return packageInfo?.versionName ?: "1.0.0"
    }

    fun formatLongNumbers(number: Long): String? {
        return when {
            abs(number / 1000000) > 1 -> (number / 1000000).toString() + "M"
            abs(number / 1000) > 1 -> (number / 1000).toString() + "K"
            else -> number.toString()   // deal with easy case
        }
    }

    fun toggleArrow(view: View): Boolean {
        return if (view.rotation.equals(0F)) {
            view.animate().setDuration(200).rotation(180F)
            true
        } else {
            view.animate().setDuration(200).rotation(0F)
            false
        }
    }
}