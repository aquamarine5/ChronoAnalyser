package org.aquamarine5.brainspark.chronoanalyser

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.Locale

fun formatTime(milliseconds: Long): String {
    val hours = milliseconds / 3600000
    val minutes = (milliseconds % 3600000) / 60000
    val seconds = (milliseconds % 60000) / 1000
    return String.format(Locale.SIMPLIFIED_CHINESE, "%02d:%02d:%02d", hours, minutes, seconds)
}

fun getAppName(context: Context, packageName: String): String {
    val packageManager = context.packageManager
    return try {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        packageName
    }
}

fun getAppIcon(context: Context, packageName: String): ImageBitmap {
    val packageManager = context.packageManager
    return try {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        when (val drawable = packageManager.getApplicationIcon(applicationInfo)) {
            is BitmapDrawable -> drawable.bitmap.asImageBitmap()
            is AdaptiveIconDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap.asImageBitmap()
            }

            else -> ImageBitmap(
                1,
                1
            ) // Return a default empty bitmap in case of an unsupported drawable type
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ImageBitmap(1, 1) // Return a default empty bitmap in case of an error
    }
}