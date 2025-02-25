package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoConfigController
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.dao.ChronoDailyRecordDAO
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyRecordEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit


object ChronoUsageAnalyser {
    private const val LOGTAG = "ChronoUsageAnalyser"

    private const val SKIP_UPDATE_MIN_INTERVAL = 60 * 1000

    @Deprecated("Use updateDailyUsageByStatsFlow instead")
    fun updateDailyUsageByStatsFlow(context: Context): FlowResult<Map<String, Long>> =
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

    fun updateDailyRecordFlowV2(context: Context): FlowResult<Boolean> =
        flow {
            val recordDAO = ChronoDatabase.getInstance(context).chronoDailyDataDAO()
            val lastUpdateDateProxy = ChronoConfigController.lastUpdateDailyRecordDate(context)
            val lastUpdateDate = if (lastUpdateDateProxy.isDefaultValue()) {
                LocalDate.now().minusDays(7)
            } else {
                DateConverter.toLocalDate(lastUpdateDateProxy.getValue())
            }
            val lastUpdateDateTimestamp = lastUpdateDate.toTimestampUTC()
            val timeDelta = System.currentTimeMillis() - lastUpdateDateTimestamp
            if (timeDelta < SKIP_UPDATE_MIN_INTERVAL) {
                Log.w(
                    LOGTAG,
                    "Skipping update, last update was less than $timeDelta millis ago."
                )
                emit(FlowResultUtil.resolve(false))
                return@flow
            }
            val usageManager = context.getSystemService(UsageStatsManager::class.java)

            val startTimestamp = lastUpdateDate.toTimestampUTC()
            val endTimestamp = LocalDate.now().toTimestampUTC()
            val dayCount = ChronoUnit.DAYS.between(lastUpdateDate, LocalDate.now()).toFloat()
            var dayIndex = 0
            if (startTimestamp == endTimestamp) {
                Log.w(LOGTAG, "No new data to update")
                emit(FlowResultUtil.resolve(false))
                return@flow
            }
            val usageData = usageManager.queryEvents(startTimestamp, endTimestamp)
            val usageEvent = UsageEvents.Event()
            val recordData = mutableMapOf<String, ChronoDailyRecordEntity>()
            val eventUsage: MutableMap<String, Long> = HashMap()
            var lastRecordDate: LocalDate? = null
            val allowedEventTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.ACTIVITY_PAUSED
                )
            } else {
                listOf(
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    UsageEvents.Event.MOVE_TO_FOREGROUND
                )
            }
            while (usageData.getNextEvent(usageEvent)) {
                if (allowedEventTypes.contains(usageEvent.eventType).not())
                    continue
                with(usageEvent) {
                    val usageDate = DateConverter.toLocalDateUTC(timeStamp)
                    if (lastRecordDate == null) {
                        lastRecordDate = usageDate
                    } else if (lastRecordDate != usageDate) {
                        val splitDateTimestamp =
                            DateConverter.toTimestampUTC(DateConverter.toLocalDateUTC(timeStamp))
                        var nextDayUsageTime: Long? = null
                        if (eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                            eventUsage[packageName]?.let {
                                recordData.getOrPut(packageName) {
                                    ChronoDailyRecordEntity(packageName, usageDate.minusDays(1))
                                }.usageTime += timeStamp - splitDateTimestamp
                                nextDayUsageTime = it - splitDateTimestamp
                            }
                        }
                        writeAllRecordData(recordDAO, recordData) {
                            emit(FlowResultUtil.progress((it / dayCount) + dayIndex / dayCount))
                            yield()
                        }
                        dayIndex++
                        recordData.clear()
                        eventUsage.clear()
                        nextDayUsageTime?.let {
                            recordData.getOrPut(packageName) {
                                ChronoDailyRecordEntity(packageName, usageDate).apply {
                                    usageTime = it
                                }
                            }
                        }
                    } else {
                        when (usageEvent.eventType) {
                            UsageEvents.Event.ACTIVITY_RESUMED -> {
                                eventUsage[packageName] = usageEvent.timeStamp
                                recordData.getOrPut(packageName) {
                                    ChronoDailyRecordEntity(
                                        packageName,
                                        usageDate
                                    )
                                }.startupCount++
                            }

                            UsageEvents.Event.ACTIVITY_PAUSED -> {
                                eventUsage[packageName]?.let {
                                    recordData.getOrPut(packageName) {
                                        ChronoDailyRecordEntity(packageName, usageDate)
                                    }.usageTime += usageEvent.timeStamp - it
                                }
                                eventUsage.remove(packageName)
                            }

                            else -> {}
                        }
                    }
                }
            }
            eventUsage.forEach{ (packageName, startTimestamp) ->
                recordData.getOrPut(packageName) {
                    ChronoDailyRecordEntity(packageName, lastRecordDate!!)
                }.usageTime += DateConverter.toTimestampUTC(lastRecordDate!!) - startTimestamp
            }
            writeAllRecordData(recordDAO, recordData) {
                emit(FlowResultUtil.progress(it / dayCount + dayIndex / dayCount))
                yield()
            }
            lastUpdateDateProxy.setValue(LocalDate.now().toDateNumber())
            emit(FlowResultUtil.resolve(true))
        }

    private suspend fun writeAllRecordData(
        recordDAO: ChronoDailyRecordDAO,
        recordData: Map<String, ChronoDailyRecordEntity>,
        withProgress: suspend (Float) -> Unit
    ) {
        val recordCount = recordData.size.toFloat()
        recordData.values.forEachIndexed { index, recordValue ->
            withContext(Dispatchers.IO) {
                if (recordDAO.getDailyData(recordValue.packageName, recordValue.dateNumber) != null)
                    Log.w(
                        LOGTAG,
                        "Duplicate record found, ${recordValue.packageName} ${recordValue.dateNumber}"
                    )
                else
                    recordDAO.insertDailyData(recordValue)
            }
            withProgress(index / recordCount)
            yield()
        }
    }

    @Deprecated("Use updateDailyRecordFlow instead")
    fun updateDailyRecordFlow(context: Context): FlowResult<Boolean> =
        flow {
            val db = ChronoDatabase.getInstance(context)
            val recordDAO = db.chronoDailyDataDAO()
            val lastUpdateDateProxy = ChronoConfigController.lastUpdateDailyRecordDate(context)
            val lastUpdateDateData = DateConverter.toLocalDate(lastUpdateDateProxy.getValue())
            val lastUpdateDateValue = DateConverter.toTimestamp(lastUpdateDateData)
            if (System.currentTimeMillis() - lastUpdateDateValue < SKIP_UPDATE_MIN_INTERVAL) {
                Log.w(
                    LOGTAG,
                    "Skipping update, last update was less than ${System.currentTimeMillis() - lastUpdateDateValue} millis ago"
                )
                emit(FlowResultUtil.resolve(false))
                return@flow
            }

            val mUsageStatsManager =
                context.getSystemService(UsageStatsManager::class.java)
            val lastUpdateDate =
                if (lastUpdateDateData == ChronoConfigController.DEFAULT_LAST_UPDATE_RECORD_DATE) {
                    val predictedUsageData =
                        mUsageStatsManager.queryEvents(0, System.currentTimeMillis())
                    val predictedCurrentEvent = UsageEvents.Event()
                    if (predictedUsageData.getNextEvent(predictedCurrentEvent)) {
                        DateConverter.toLocalDateUTC(predictedCurrentEvent.timeStamp).plusDays(1)
                    } else {
                        throw IllegalStateException("No usage data found")
                    }
                } else {
                    lastUpdateDateData
                }
            val startTime = lastUpdateDate.toTimestampUTC()
            val endTime = LocalDate.now().toTimestampUTC()
            if (startTime == endTime) {
                //emit(FlowResultUtil.resolve(false))
                Log.w(LOGTAG, "No new data to update")
                //return@flow
            }
            val endDateNumber = DateConverter.toDateNumberUTC(endTime)
            val usageData = mUsageStatsManager.queryEvents(startTime, endTime)
            val usageEvent = UsageEvents.Event()
            var lastPackageName = ""
            val recordData = mutableMapOf<String, ChronoDailyRecordEntity>()
            val eventUsage: MutableMap<String, Long> = HashMap()
            var lastRecordDateNumber: LocalDate? = null
            val allowedEventTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.ACTIVITY_PAUSED
                )
            } else {
                listOf(
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    UsageEvents.Event.MOVE_TO_FOREGROUND
                )
            }
            while (usageData.getNextEvent(usageEvent)) {
                if (allowedEventTypes.contains(usageEvent.eventType).not())
                    continue
                val app = usageEvent.packageName
                val eventDate =
                    DateConverter.toLocalDateUTC(usageEvent.timeStamp)
                if (lastRecordDateNumber == null) {
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
                                        eventDate.minusDays(1)
                                    )
                                }.usageTime += eventDate.toTimestampUTC() - it
                            }
                        }

                        val recordCount = recordData.size.toFloat()
                        recordData.values.forEachIndexed { index, recordValue ->
                            withContext(Dispatchers.IO) {
                                if (recordDAO.getDailyData(
                                        recordValue.packageName,
                                        recordValue.dateNumber
                                    ) != null
                                )
                                    Log.w(
                                        LOGTAG,
                                        "Duplicate record found, ${recordValue.packageName} ${recordValue.dateNumber}"
                                    )
                                else
                                    recordDAO.insertDailyData(recordValue)
                            }
                            emit(
                                FlowResultUtil.progress(
                                    (index / recordCount) * (DateConverter.toDateNumber(
                                        eventDate
                                    ) / endDateNumber.toFloat())
                                )
                            )
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
                                usageEvent.timeStamp - eventDate.toTimestampUTC()
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
            val recordCount = recordData.size.toFloat()
            recordData.values.forEachIndexed { index, recordValue ->
                withContext(Dispatchers.IO) {
                    if (recordDAO.getDailyData(
                            recordValue.packageName,
                            recordValue.dateNumber
                        ) != null
                    )
                        Log.w(
                            LOGTAG,
                            "Duplicate record found, ${recordValue.packageName} ${recordValue.dateNumber}"
                        )
                    else
                        recordDAO.insertDailyData(recordValue)
                }
                emit(
                    FlowResultUtil.progress(
                        index / recordCount
                    )
                )
                yield()
            }
            lastUpdateDateProxy.setValue(LocalDate.now().toDateNumber())
            emit(FlowResultUtil.resolve(true))
        }

    fun updateUsageByEventFlow(context: Context): FlowResult<Boolean> =
        flow {
            val db = ChronoDatabase.getInstance(context)
            val lastUpdateTimeProxy = ChronoConfigController.lastUpdateTime(context)
            val lastUpdateTimeValue = lastUpdateTimeProxy.getValue()
            if (System.currentTimeMillis() - lastUpdateTimeValue < SKIP_UPDATE_MIN_INTERVAL) {
                Log.w(
                    LOGTAG,
                    "Skipping update, last update was less than ${System.currentTimeMillis() - lastUpdateTimeValue} millis ago"
                )
                emit(FlowResultUtil.resolve(false))
                return@flow
            }
            val appDAO = db.chronoAppDAO()
            val currTime = System.currentTimeMillis()
            val mUsageStatsManager =
                context.getSystemService(UsageStatsManager::class.java)!!
            val usageEvents =
                mUsageStatsManager.queryEvents(lastUpdateTimeValue, currTime)
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
                        addedStartupCount++
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
                        //Log.w(classTag, "Notification received $app")
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
                lastUpdateTimeProxy.setValue(it)
            }
            emit(FlowResultUtil.resolve(true))
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