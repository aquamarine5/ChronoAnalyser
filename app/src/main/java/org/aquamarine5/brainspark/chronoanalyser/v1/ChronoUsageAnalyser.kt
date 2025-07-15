package org.aquamarine5.brainspark.chronoanalyser.v1

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.aquamarine5.brainspark.chronoanalyser.DateConverter
import org.aquamarine5.brainspark.chronoanalyser.toDateNumber
import org.aquamarine5.brainspark.chronoanalyser.v1.DataStoreSerializer.datastore
import org.aquamarine5.brainspark.chronoanalyser.v1.ProgressedFlowUtil.whenProgress
import org.aquamarine5.brainspark.chronoanalyser.v1.ProgressedFlowUtil.whenResolve
import org.aquamarine5.brainspark.chronoanalyser.v1.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.v1.data.entity.ChronoDailyReportEntity
import org.aquamarine5.brainspark.chronoanalyser.v1.data.entity.ChronoDailyUsageEntity
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

object ChronoUsageAnalyser {

    fun loadDailyUsageData(
        context: Context,
        date: LocalDate
    ): ProgressedFlowResult<Pair<Float, List<ChronoDailyUsageEntity>>> = flow {
        val startRealTimestamp = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val startTimestamp =
            startRealTimestamp - TimeUnit.HOURS.toMillis(1)
        val endTimestamp = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val timeDelta = endTimestamp - startRealTimestamp.toFloat()
        val usageManager = context.getSystemService(UsageStatsManager::class.java)
        val usageData = usageManager.queryEvents(startTimestamp, endTimestamp)
        val usageEvent = UsageEvents.Event()
        val dailyUsageData = mutableMapOf<String, ChronoDailyUsageEntity>()
        val eventUsage: MutableMap<String, Long> = HashMap()
        val allowedEventTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.ACTIVITY_PAUSED,
                10
            )
        } else {
            listOf(
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.MOVE_TO_FOREGROUND,
                10
            )
        }
        while (usageData.getNextEvent(usageEvent)) {
            emit(
                ProgressedFlowUtil.progressResult(
                    (usageEvent.timeStamp - startRealTimestamp) / (dailyUsageData.size + timeDelta)
                )
            )
            if (allowedEventTypes.contains(usageEvent.eventType).not())
                continue
            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (usageEvent.timeStamp < startRealTimestamp) {
                        eventUsage.remove(usageEvent.packageName)
                        continue
                    }
                    eventUsage[usageEvent.packageName]?.let { time ->
                        val timeDiff = if (time < startRealTimestamp) {
                            usageEvent.timeStamp - startRealTimestamp
                        } else {
                            usageEvent.timeStamp - time
                        }
                        dailyUsageData.compute(usageEvent.packageName) { packageName, data ->
                            data?.apply {
                                usageTime += timeDiff
                            } ?: ChronoDailyUsageEntity(
                                packageName = packageName,
                                dateNumber = date,
                                usageTime = timeDiff,
                                notificationCount = 0,
                                launchCount = 0
                            )
                        }
                    }
                    eventUsage.remove(usageEvent.packageName)
                }

                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    eventUsage[usageEvent.packageName] = usageEvent.timeStamp
                    dailyUsageData.compute(usageEvent.packageName) { packageName, data ->
                        data?.apply {
                            launchCount++
                        } ?: ChronoDailyUsageEntity(
                            packageName = packageName,
                            dateNumber = date,
                            usageTime = 0L,
                            notificationCount = 0,
                            launchCount = 1
                        )
                    }
                }

                10 -> {
                    dailyUsageData.compute(usageEvent.packageName) { packageName, data ->
                        data?.apply {
                            notificationCount++
                        } ?: ChronoDailyUsageEntity(
                            packageName = packageName,
                            dateNumber = date,
                            usageTime = 0L,
                            notificationCount = 1,
                            launchCount = 0
                        )
                    }
                }
            }
        }
        emit(ProgressedFlowUtil.resolveResult(timeDelta to dailyUsageData.values.toList()))
    }

    fun saveDailyUsageToReport(
        context: Context,
        data: List<ChronoDailyUsageEntity>
    ): ProgressedFlow = flow {
        val db = context.datastore
        val room = ChronoDatabase.getInstance(context)
        val dailyUsageDao = room.chronoDailyUsageDAO()
        val dailyReportDAO = room.chronoDailyReportDAO()
        var allUsageTime = 0L
        var allNotificationCount = 0
        var allLaunchCount = 0
        withContext(Dispatchers.IO) {
            data.forEachIndexed { index, it ->
                dailyUsageDao.upsertDailyData(it)
                allUsageTime += it.usageTime
                allLaunchCount += it.launchCount
                allNotificationCount += it.notificationCount
                emit(
                    ProgressedFlowUtil.progress(
                        index.toFloat() / data.size
                    )
                )
            }
            dailyReportDAO.upsertDailyReport(
                ChronoDailyReportEntity(
                    dateNumber = data[0].dateNumber,
                    allUsageTime = allUsageTime,
                    allLaunchCount = allLaunchCount,
                    allNotificationCount = allNotificationCount
                )
            )
        }
        db.updateData { datastore ->
            datastore.toBuilder().also {
                it.allUsageTime += allUsageTime
            }.build()
        }
        emit(ProgressedFlowUtil.resolve())
    }

    fun updateUsageData(context: Context): ProgressedFlow = flow {
        val datastore = context.datastore
        val datastoreResult = datastore.data.first()
        var startTime =
            if (datastoreResult.lastUpdateDate == 0) {
                LocalDate.now().minusDays(7)
            } else {
                DateConverter.toLocalDate(datastoreResult.lastUpdateDate)
            }
        val endTime = LocalDate.now().minusDays(1)
        while (startTime <= endTime) {
            loadDailyUsageData(context, startTime).collect { progress ->
                progress.whenProgress {
                    emit(ProgressedFlowUtil.progress(it))
                }.whenResolve { result->
                    saveDailyUsageToReport(context, result.second).collect { dailyProgress ->
                        dailyProgress.whenProgress {
                            emit(ProgressedFlowUtil.progress((result.first+it*result.second.size)/(result.first+result.second.size)))
                        }.whenResolve {
                            emit(ProgressedFlowUtil.resolve())
                        }
                    }
                }
            }
            startTime = startTime.plusDays(1)
        }
    }
}