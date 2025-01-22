package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ChronoUsageAnalyser {
    private fun getLongAgoTimestamp():Long{
        val time = LocalDateTime.of(2025, 1, 11, 0, 0)
        val zoneId = ZoneId.systemDefault()
        return time.atZone(zoneId).toInstant().toEpochMilli()
    }
    fun loadUsagePerApplicationFlow(context: Context): FlowResult<Map<String, Long>> = flow {
        val outputData = mutableMapOf<String, Long>()
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
        val beginTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val endTime = System.currentTimeMillis()
        Log.w("zxzx","zzzz")
        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, beginTime, endTime)
        val listCount=usageStatsList.size.toFloat()
        for ((index, usageStats) in usageStatsList.withIndex()) {
            val packageName = usageStats.packageName
            val totalTimeInForeground = usageStats.totalTimeInForeground

            Log.w("Chronoz","$packageName $totalTimeInForeground")


            outputData[packageName] = outputData.getOrDefault(packageName, 0L) + totalTimeInForeground
            emit(FlowResultUtil.progress(index/listCount))
            yield()
        }

        emit(FlowResultUtil.resolve(outputData))
    }
//    fun loadUsagePerApplicationFlow(context: Context): FlowResult<Map<String,Long>> =flow{
//        val outputData= mutableMapOf<String,Long>()
//        val usageStatsManager=context.getSystemService(UsageStatsManager::class.java)
//        Log.d("ChronoUsageAnalyser", "${System.currentTimeMillis()-TimeUnit.DAYS.toMillis(217)}: ${getLongAgoTimestamp()}")
//        val usageData=usageStatsManager.queryAndAggregateUsageStats(
//            System.currentTimeMillis()-TimeUnit.DAYS.toMillis(1)
//            ,System.currentTimeMillis())
//        val progressLength=usageData.size.toFloat()
//        val db=ChronoDatabase.getInstance(context)
//        val appDAO=db.chronoAppDAO()
//        for ((index, usageApp) in usageData.entries.withIndex()) {
//            val packageName=usageApp.key
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                outputData[packageName] = -usageApp.value.totalTimeInForeground+usageApp.value.totalTimeVisible
//            }
//            Log.d("ChronoUsageAnalyser", "totalTimeVisibleloadUsagePerApplicationFlow: $packageName ${getAppName(context,packageName)} ${usageApp.value.totalTimeInForeground}")
////            emit(FlowResultUtil.progress(index/progressLength/2))
////            yield()
////            withContext(Dispatchers.IO){
////                appDAO.insertApp(ChronoAppEntity(
////                    packageName,getAppName(context,packageName),usageApp.value.totalTimeInForeground
////                ))
////            }
//            emit(FlowResultUtil.progress(index/progressLength))
//            yield()
//        }
//        emit(FlowResultUtil.resolve(outputData))
//    }

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