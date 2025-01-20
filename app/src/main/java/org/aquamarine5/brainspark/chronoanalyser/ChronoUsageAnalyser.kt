package org.aquamarine5.brainspark.chronoanalyser

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import org.aquamarine5.brainspark.chronoanalyser.data.ChronoDatabase
import org.aquamarine5.brainspark.chronoanalyser.data.entity.ChronoDayRecordEntity
import java.util.concurrent.TimeUnit

class ChronoUsageAnalyser {
    fun getRecordsHourlyIntervalByDays(context: Context, days: Long): Map<String, Map<Int, Map<Int, Map<Int, Map<Int, Long>>>>> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(days)
        val usageStatsMap: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

        val usageMap = mutableMapOf<String, MutableMap<Int, MutableMap<Int, MutableMap<Int, MutableMap<Int, Long>>>>>()
        for ((packageName, usageStats) in usageStatsMap) {
            val startHour = (usageStats.firstTimeStamp / TimeUnit.HOURS.toMillis(1)).toInt()
            val endHour = (usageStats.lastTimeStamp / TimeUnit.HOURS.toMillis(1)).toInt()
            val totalTime = usageStats.totalTimeInForeground

            if (totalTime > 0) {
                val year = (usageStats.firstTimeStamp / TimeUnit.DAYS.toMillis(365)).toInt()
                val month = ((usageStats.firstTimeStamp % TimeUnit.DAYS.toMillis(365)) / TimeUnit.DAYS.toMillis(30)).toInt()
                val day = ((usageStats.firstTimeStamp % TimeUnit.DAYS.toMillis(30)) / TimeUnit.DAYS.toMillis(1)).toInt()

                for (hour in startHour..endHour) {
                    val yearlyUsage = usageMap.getOrPut(packageName) { mutableMapOf() }
                    val monthlyUsage = yearlyUsage.getOrPut(year) { mutableMapOf() }
                    val dailyUsage = monthlyUsage.getOrPut(month) { mutableMapOf() }
                    val hourlyUsage = dailyUsage.getOrPut(day) { mutableMapOf() }
                    hourlyUsage[hour] = hourlyUsage.getOrDefault(hour, 0L) + totalTime / (endHour - startHour + 1)
                }
            }
        }
        return usageMap
    }

    fun getRecordsDailyIntervalByDays(context: Context, days: Long): Map<String, Map<Int, Map<Int, Map<Int, Long>>>> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(days)
        val usageStatsMap: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

        val usageMap = mutableMapOf<String, MutableMap<Int, MutableMap<Int, MutableMap<Int, Long>>>>()
        for ((packageName, usageStats) in usageStatsMap) {
            val totalTime = usageStats.totalTimeInForeground

            if (totalTime > 0) {
                val year = (usageStats.firstTimeStamp / TimeUnit.DAYS.toMillis(365)).toInt()
                val month = ((usageStats.firstTimeStamp % TimeUnit.DAYS.toMillis(365)) / TimeUnit.DAYS.toMillis(30)).toInt()
                val day = ((usageStats.firstTimeStamp % TimeUnit.DAYS.toMillis(30)) / TimeUnit.DAYS.toMillis(1)).toInt()

                val yearlyUsage = usageMap.getOrPut(packageName) { mutableMapOf() }
                val monthlyUsage = yearlyUsage.getOrPut(year) { mutableMapOf() }
                val dailyUsage = monthlyUsage.getOrPut(month) { mutableMapOf() }
                dailyUsage[day] = dailyUsage.getOrDefault(day, 0L) + totalTime
            }
        }
        return usageMap
    }

    fun installPreviousRecords(context: Context){
        val db = ChronoDatabase.getInstance(context)
        val dayRecordDao = db.chronoDayRecordDAO()

        val dailyRecords = getRecordsDailyIntervalByDays(context, 30)
        for ((packageName, yearlyUsage) in dailyRecords) {
            for ((year, monthlyUsage) in yearlyUsage) {
                for ((month, dailyUsage) in monthlyUsage) {
                    for ((day, totalTime) in dailyUsage) {
                        val record = ChronoDayRecordEntity(packageName, totalTime, year, month, day)
                        dayRecordDao.insertDayRecord(record)
                    }
                }
            }
        }
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