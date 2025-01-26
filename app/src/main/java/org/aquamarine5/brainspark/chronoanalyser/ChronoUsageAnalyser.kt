package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoConfigController
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.DateSQLConverter
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


object ChronoUsageAnalyser {
    private val classTag = this::class.simpleName

    fun updateUsageByStatsFlow(context: Context): FlowResult<Map<String, Long>> =
        flow {
            val outputData = mutableMapOf<String, Long>()
            val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - TimeUnit.DAYS.toMillis(365 * 2)
            val usageStatsList: List<UsageStats> =
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_YEARLY,
                    beginTime,
                    endTime
                )
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

    fun updateRecordFlow(context: Context): FlowResult<Boolean> =
        flow {
            val db = ChronoDatabase.getInstance(context)
            val lastUpdateDateProxy = ChronoConfigController.lastUpdateDailyRecordDate(context)
            val lastUpdateDateValue = lastUpdateDateProxy.getValue()
            val zoneId = ZoneId.systemDefault()
            val mUsageStatsManager =
                context.getSystemService(UsageStatsManager::class.java)
            val lastUpdateDate =
                if (lastUpdateDateValue == ChronoConfigController.DEFAULT_LAST_UPDATE_RECORD_DATE) {
                    val predictedUsageData =
                        mUsageStatsManager.queryEvents(0, System.currentTimeMillis())
                    val predictedCurrentEvent = UsageEvents.Event()
                    if (predictedUsageData.getNextEvent(predictedCurrentEvent)) {
                        DateSQLConverter.toDateNumber(predictedCurrentEvent.timeStamp) + 1
                    } else {
                        ChronoConfigController.DEFAULT_LAST_UPDATE_RECORD_DATE
                    }
                } else {
                    lastUpdateDateValue
                }
            val startTime =
                LocalDate.parse(lastUpdateDate.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))
                    .atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endTime = LocalDate.now().atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endDateNumber = DateSQLConverter.toDateNumber(endTime)
            val usageData = mUsageStatsManager.queryEvents(startTime, endTime)
            val usageEvent = UsageEvents.Event()
            var lastPackageName = ""
            val recordData = mutableMapOf<String, ChronoDailyRecordEntity>()
            val eventUsage: MutableMap<String, Long> = HashMap()
            var lastRecordDateNumber = 0
            while (usageData.hasNextEvent()) {
                usageData.getNextEvent(usageEvent)
                val app = usageEvent.packageName
                val eventDate = DateSQLConverter.toDateNumber(usageEvent.timeStamp)
                if (lastRecordDateNumber == 0) {
                    lastRecordDateNumber = eventDate
                } else if (lastRecordDateNumber != eventDate) {
                    lastRecordDateNumber = eventDate
                    eventUsage[lastPackageName]?.let { pt ->
                        recordData.getOrPut(lastPackageName) {
                            ChronoDailyRecordEntity(
                                lastPackageName,
                                eventDate
                            )
                        }.usageTime += usageEvent.timeStamp - pt


                        if (usageEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                            eventUsage[usageEvent.packageName]?.let {
                                recordData.getOrPut(usageEvent.packageName) {
                                    ChronoDailyRecordEntity(
                                        usageEvent.packageName,
                                        eventDate - 1
                                    )
                                }.usageTime += DateSQLConverter.toTimestamp(eventDate) - it
                            }
                        }

                        val recordDAO = db.chronoDailyDataDAO()
                        val recordCount = recordData.size.toFloat()
                        recordData.values.forEachIndexed { index, recordValue ->
                            withContext(Dispatchers.IO) {
                                Log.d(classTag,"${recordValue.packageName} ${recordValue.dateNumber} ${recordValue.usageTime}")
                                recordDAO.insertDailyData(recordValue)
                            }
                            emit(FlowResultUtil.progress((index / recordCount) * (eventDate / endDateNumber.toFloat())))
                            yield()

                        }
                        eventUsage.clear()
                        lastPackageName = ""
                        recordData.clear()

                        if (usageEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                            recordData.getOrPut(
                                usageEvent.packageName
                            ) {
                                ChronoDailyRecordEntity(
                                    usageEvent.packageName, lastRecordDateNumber
                                )
                            }.usageTime =
                                usageEvent.timeStamp - DateSQLConverter.toTimestamp(eventDate)
                        }
                    }
                } else {
                    when (usageEvent.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> {
                            eventUsage[app] = usageEvent.timeStamp
                            recordData.getOrPut(app) {
                                ChronoDailyRecordEntity(
                                    app,
                                    eventDate
                                )
                            }.startupCount++
                            lastPackageName = app
                        }

                        UsageEvents.Event.ACTIVITY_PAUSED -> {
                            eventUsage[app]?.let {
                                recordData.getOrPut(app) {
                                    ChronoDailyRecordEntity(app, eventDate)
                                }.usageTime += usageEvent.timeStamp - it
                            }
                            eventUsage.remove(app)
                        }
                    }
                }
                yield()
            }

        }

    fun updateUsageByEventFlow(context: Context): FlowResult<Map<String, Long>> =
        flow {
            val db = ChronoDatabase.getInstance(context)
            val lastUpdateTimeProxy = ChronoConfigController.lastUpdateTime(context)
            val appDAO = db.chronoAppDAO()
            val currTime = System.currentTimeMillis()
            val mUsageStatsManager =
                context.getSystemService(UsageStatsManager::class.java)
            val usageEvents =
                mUsageStatsManager!!.queryEvents(lastUpdateTimeProxy.getValue(), currTime)
            val eventUsage: MutableMap<String, Long> = HashMap()
            val totalUsage: MutableMap<String, Long> = HashMap()
            var lastApp = ""
            val allCounts = 700f
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

                    else -> {
                        //Log.w(classTag, "Unknown event type: ${currentEvent.eventType}")
                    }
                }
                emit(FlowResultUtil.progress((if (currentProgress <= 1f) currentProgress else 1f) / 2))
                //Log.d(classTag, "progress: $currentProgress")
                currentCount++
                yield()
            }

            if (eventUsage.containsKey(lastApp)) if (totalUsage.containsKey(lastApp)) totalUsage[lastApp] =
                totalUsage[lastApp]!! + (System.currentTimeMillis() - eventUsage[lastApp]!!)
            else totalUsage[lastApp] = (System.currentTimeMillis() - eventUsage[lastApp]!!)

            val usageCount = totalUsage.size.toFloat() + 1

            for ((index, usage) in totalUsage.entries.withIndex()) {
                withContext(Dispatchers.IO) {
                    val rawApp = appDAO.getAppByPackageName(usage.key)
                    if (rawApp == null)
                        appDAO.upsertApp(
                            ChronoAppEntity(
                                usage.key, getAppName(context, usage.key), usage.value
                            )
                        )
                    else {
                        appDAO.addAppCounts(
                            usage.key,
                            usage.value,
                            addedNotificationCount,
                            addedStartupCount
                        )
                    }
                }
                emit(FlowResultUtil.progress(0.5f + index / usageCount))
                yield()
            }
            latestTime?.let {
                lastUpdateTimeProxy.setValue(latestTime)
            }
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