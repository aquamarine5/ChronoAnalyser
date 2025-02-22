package org.aquamarine5.brainspark.chronoanalyser

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object PermissionController {
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
        } else {
            (mode == AppOpsManager.MODE_ALLOWED)
        }
    }

    fun requestUsageStatsPermission(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

@Composable
fun rememberPermissionState(): PermissionState {
    val context = LocalContext.current
    return remember {
        PermissionState(
            permission = android.Manifest.permission.PACKAGE_USAGE_STATS,
            state = derivedStateOf {
                PermissionController.hasUsageStatsPermission(context)
            },
            requestPermission = {
                PermissionController.requestUsageStatsPermission(context)
            }
        )
    }
}

class PermissionState(
    val permission: String,
    val state: State<Boolean>,
    val requestPermission: () -> Unit
)