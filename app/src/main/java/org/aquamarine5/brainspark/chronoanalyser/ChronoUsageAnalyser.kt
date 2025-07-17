package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.aquamarine5.brainspark.chronoanalyser.DataStoreSerializer.datastore
import org.aquamarine5.brainspark.chronoanalyser.ProgressedFlowUtil.whenProgress
import org.aquamarine5.brainspark.chronoanalyser.ProgressedFlowUtil.whenResolve
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoAppUsageEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyReportEntity
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDailyUsageEntity
import java.time.LocalDate
import java.time.ZoneOffset

object ChronoUsageAnalyser {

    fun loadDailyUsageData(
        context: Context,
        date: LocalDate
    ): ProgressedFlowResult<Pair<Float, List<ChronoDailyUsageEntity>>> = flow {
        val startTimestamp =
            date.atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
        val endTimestamp =
            date.plusDays(1).atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
        val timeDelta = endTimestamp - startTimestamp.toFloat()
        val usageManager = context.getSystemService(UsageStatsManager::class.java)
        val usageData = usageManager.queryEvents(startTimestamp, endTimestamp)
        val usageEvent = UsageEvents.Event()
        val dailyUsageData = mutableMapOf<String, ChronoDailyUsageEntity>()
        val eventUsage: MutableMap<Pair<String,String>, Long> = HashMap()
        while (usageData.getNextEvent(usageEvent)) {
            emit(
                ProgressedFlowUtil.progressResult(
                    (usageEvent.timeStamp - startTimestamp) / (dailyUsageData.size + timeDelta)
                )
            )
            when (usageEvent.eventType) {
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val timeDiff =
                        if (eventUsage.containsKey(usageEvent.packageName to usageEvent.className)) {
                            usageEvent.timeStamp - eventUsage[usageEvent.packageName to usageEvent.className]!!
                        } else {
                            continue
                        }
                    dailyUsageData.compute(usageEvent.packageName) { packageName, data ->
                        (data ?: ChronoDailyUsageEntity(
                            packageName = packageName,
                            dateNumber = date,
                            usageTime = 0L,
                            notificationCount = 0,
                            launchCount = 0
                        )).apply {
                            usageTime += timeDiff
                        }
                    }

                    eventUsage.remove(usageEvent.packageName to usageEvent.className)
                }

                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if(eventUsage.any { it.key.first==usageEvent.packageName }.not())
                        dailyUsageData.compute(usageEvent.packageName) { packageName, data ->
                            (data ?: ChronoDailyUsageEntity(
                                packageName = packageName,
                                dateNumber = date,
                                usageTime = 0L,
                                notificationCount = 0,
                                launchCount = 0
                            )).apply {
                                launchCount++
                            }
                        }
                    eventUsage[usageEvent.packageName to usageEvent.className] = usageEvent.timeStamp

                }

                12 -> {
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
        eventUsage.forEach { (name, time) ->
            dailyUsageData.compute(name.first) { packageName, data ->
                (data ?: ChronoDailyUsageEntity(
                    packageName = packageName,
                    dateNumber = date,
                    usageTime = 0L,
                    notificationCount = 0,
                    launchCount = 0
                )).apply {
                    //usageTime += (endTimestamp - time)
                }
            }
        }
        emit(ProgressedFlowUtil.resolveResult(timeDelta to dailyUsageData.values.toList()))
    }

    fun getApplicationLabel(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        return try {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun saveDailyUsageToReport(
        context: Context,
        data: List<ChronoDailyUsageEntity>
    ): ProgressedFlowResult<ChronoDailyReportEntity> = flow {
        val db = context.datastore
        val room = ChronoDatabase.getInstance(context)
        val dailyUsageDao = room.chronoDailyUsageDAO()
        val appUsageDao = room.chronoAppUsageDAO()
        val dailyReportDAO = room.chronoDailyReportDAO()
        var allUsageTime = 0L
        var allNotificationCount = 0
        var allLaunchCount = 0
        withContext(Dispatchers.IO) {
            val appUsageData = appUsageDao.getAllApps()
            data.forEachIndexed { index, it ->
                dailyUsageDao.upsertDailyData(it)
                allUsageTime += it.usageTime
                allLaunchCount += it.launchCount
                allNotificationCount += it.notificationCount
                withContext(Dispatchers.Main) {
                    emit(
                        ProgressedFlowUtil.progressResult(
                            index.toFloat() / data.size / 2
                        )
                    )
                }
            }
            data.forEachIndexed { index, usage ->
                appUsageDao.upsertApp(
                    (appUsageData.find { it.packageName == usage.packageName }
                        ?: ChronoAppUsageEntity(
                            usage.packageName,
                            getApplicationLabel(context, usage.packageName)
                        )).also {
                        it.usageTime += usage.usageTime
                        it.notificationCount += usage.notificationCount
                        it.launchCount += usage.launchCount
                    }
                )
                withContext(Dispatchers.Main) {
                    emit(
                        ProgressedFlowUtil.progressResult(
                            (index + data.size).toFloat() / data.size / 2
                        )
                    )
                }
            }
            val report = ChronoDailyReportEntity(
                dateNumber = data[0].dateNumber,
                allUsageTime = allUsageTime,
                allLaunchCount = allLaunchCount,
                allNotificationCount = allNotificationCount
            )
            dailyReportDAO.upsertDailyReport(
                report
            )
            db.updateData { datastore ->
                datastore.toBuilder().also {
                    it.allUsageTime += allUsageTime
                }.build()
            }
            withContext(Dispatchers.Main) {
                emit(ProgressedFlowUtil.resolveResult(report))
            }
        }

    }

    fun updateUsageData(context: Context): ProgressedFlow = flow {
        val datastore = context.datastore
        val datastoreResult = datastore.data.first()
        val startTime =
            if (datastoreResult.lastUpdateDate == 0) {
                LocalDate.now().minusDays(7)
            } else {
                DateConverter.toLocalDate(datastoreResult.lastUpdateDate)
            }
        val endTime = LocalDate.now().minusDays(1)
        var nowTime = startTime
        val timeDelta = endTime.toEpochDay() - startTime.toEpochDay()+1
        val dailyReports = mutableListOf<ChronoDailyReportEntity>()
        while (nowTime <= endTime) {
            val dayIndex = nowTime.toEpochDay() - startTime.toEpochDay()
            loadDailyUsageData(context, nowTime).collect { progress ->
                progress.whenProgress {
                    emit(ProgressedFlowUtil.progress(it * (dayIndex + 1) / (timeDelta)))
                }.whenResolve { result ->
                    saveDailyUsageToReport(context, result.second).collect { dailyProgress ->
                        dailyProgress.whenProgress {
                            emit(ProgressedFlowUtil.progress((result.first + it * result.second.size) / (result.first + result.second.size) * (dayIndex + 1) / (timeDelta)))
                        }.whenResolve {
                            dailyReports.add(it)
                        }
                    }
                }
            }
            nowTime = nowTime.plusDays(1)
        }
        withContext(Dispatchers.IO) {
            datastore.updateData {
                it.toBuilder()
                    .setLastUpdateDate(LocalDate.now().toDateNumber())
                    .setLastUpdateTime(System.currentTimeMillis())
                    .setAllUsageTime(datastoreResult.allUsageTime +
                            dailyReports.sumOf { report -> report.allUsageTime }
                    )
                    .build()
            }
        }
        emit(ProgressedFlowUtil.resolve())
    }
}