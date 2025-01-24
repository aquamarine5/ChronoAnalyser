package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoConfigController
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import java.util.concurrent.TimeUnit


object ChronoUsageAnalyser {
    private val classTag = this::class.simpleName

    fun loadUsageByStatsFlow(context: Context): FlowResult<Map<String, Long>> = flow {
        val outputData = mutableMapOf<String, Long>()
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - TimeUnit.DAYS.toMillis(365 * 2)
        val usageStatsList: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY, beginTime, endTime)
        val listCount = usageStatsList.size.toFloat()
        for ((index, usageStats) in usageStatsList.withIndex()) {
            val packageName = usageStats.packageName
            val totalTimeInForeground = usageStats.totalTimeInForeground
            outputData[packageName] =
                outputData.getOrDefault(packageName, 0L) + totalTimeInForeground
            emit(FlowResultUtil.progress(index / listCount))
            yield()
        }
        emit(FlowResultUtil.resolve(outputData))
    }

    fun loadUsageByEventFlow(context: Context): FlowResult<Map<String, Long>> = flow {
        val currTime = System.currentTimeMillis()
        val mUsageStatsManager =
            context.getSystemService(UsageStatsManager::class.java)
        val usageEvents =
            mUsageStatsManager!!.queryEvents(currTime - TimeUnit.DAYS.toMillis(7), currTime)
        val eventUsage: MutableMap<String, Long> = HashMap()
        val totalUsage: MutableMap<String, Long> = HashMap()
        var lastApp = ""
        val allCounts = 70000f
        var currentCount = 0
        var time: Long
        var addedNotificationCount = 0
        var addedStartupCount = 0
        var latestTime: Long? = null
        while (usageEvents.hasNextEvent()) {
            val currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            val currentProgress = currentCount / allCounts
            val app = currentEvent.packageName
            time = currentEvent.timeStamp
            when (currentEvent.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    eventUsage[app] = time
                    lastApp = app
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (eventUsage.containsKey(app)) {
                        if (totalUsage.containsKey(app))
                            totalUsage[app] = totalUsage[app]!! + (time - eventUsage[app]!!)
                        else
                            totalUsage[app] = (time - eventUsage[app]!!)
                    }
                    eventUsage.remove(app)
                    latestTime = time
                }

                12 -> {
                    Log.w(classTag, "Notification received $app")
                    addedNotificationCount++
                }
            }
            emit(FlowResultUtil.progress((if (currentProgress <= 1f) currentProgress else 1f) / 2))
            delay(100)
            currentCount++
            yield()
        }

        if (eventUsage.containsKey(lastApp)) if (totalUsage.containsKey(lastApp)) totalUsage[lastApp] =
            totalUsage[lastApp]!! + (System.currentTimeMillis() - eventUsage[lastApp]!!)
        else totalUsage[lastApp] = (System.currentTimeMillis() - eventUsage[lastApp]!!)

        val usageCount = totalUsage.size.toFloat() + 1
        val db = ChronoDatabase.getInstance(context)
        val lastUpdateTimeProxy = ChronoConfigController.lastUpdateTime(context)
        val appDAO = db.chronoAppDAO()

        for ((index, usage) in totalUsage.entries.withIndex()) {
            val rawApp = appDAO.getAppByPackageName(usage.key)
            if (rawApp == null)
                appDAO.upsertApp(
                    ChronoAppEntity(
                        usage.key, getAppName(context, usage.key), usage.value
                    )
                )
            else {
                appDAO.addAppCounts(usage.key, usage.value,addedNotificationCount,addedStartupCount)
            }
            emit(FlowResultUtil.progress(0.5f + index / usageCount))
            yield()
        }
        if (latestTime != null)
            lastUpdateTimeProxy.setValue(latestTime)
        emit(FlowResultUtil.resolve(totalUsage))
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