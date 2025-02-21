package org.aquamarine5.brainspark.chronoanalyser.data

import android.content.SharedPreferences

class ChronoConfigProxy<T>(
    private val sharedPreferences: SharedPreferences,
    private val key: String,
    private val defaultValue: T
) {
    @Suppress("UNCHECKED_CAST")
    fun isDefaultValue(): Boolean {
        return when (defaultValue) {
            is Long -> sharedPreferences.getLong(key, defaultValue) as T == defaultValue
            is String -> sharedPreferences.getString(key, defaultValue) as T == defaultValue
            is Int -> sharedPreferences.getInt(key, defaultValue) as T == defaultValue
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T == defaultValue
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T == defaultValue
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    fun clear() {
        with(sharedPreferences.edit()) {
            remove(key)
            apply()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getValue(): T {
        return when (defaultValue) {
            is Long -> sharedPreferences.getLong(key, defaultValue) as T
            is String -> sharedPreferences.getString(key, defaultValue) as T
            is Int -> sharedPreferences.getInt(key, defaultValue) as T
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    fun setValue(value: T) {
        with(sharedPreferences.edit()) {
            when (value) {
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }
}