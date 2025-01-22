package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import java.time.LocalDateTime
import java.time.ZoneId

class ChronoUsageAnalyser {
    private fun getLongAgoTimestamp():Long{
        val time = LocalDateTime.of(2000, 1, 1, 0, 0)
        val zoneId = ZoneId.systemDefault()
        return time.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun loadUsagePerApplicationFlow(context: Context): FlowResult<Map<String,Long>> =flow{
        val outputData= mutableMapOf<String,Long>()
        val usageStatsManager=context.getSystemService(UsageStatsManager::class.java)
        val usageData=usageStatsManager.queryAndAggregateUsageStats(getLongAgoTimestamp(),System.currentTimeMillis())
        val progressLength=usageData.size.toFloat()
        val db=ChronoDatabase.getInstance(context)
        val appDAO=db.chronoAppDAO()
        for ((index, usageApp) in usageData.entries.withIndex()) {
            val packageName=usageApp.key
            outputData[packageName] = usageApp.value.totalTimeInForeground
            emit(FlowResultUtil.progress(index/progressLength/2))
            yield()
            appDAO.insertApp(ChronoAppEntity(
                packageName,getAppName(context,packageName),usageApp.value.totalTimeInForeground
            ))
            emit(FlowResultUtil.progress(index/progressLength))
            yield()
        }
        emit(FlowResultUtil.resolve(outputData))
    }

    private fun getAppName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}