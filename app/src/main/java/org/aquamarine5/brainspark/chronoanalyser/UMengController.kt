package org.aquamarine5.brainspark.chronoanalyser

import android.content.Context
import com.umeng.commonsdk.UMConfigure

object UMengController {
    private const val APP_KEY="67ac758b9a16fe6dcd4557a4"
    private const val APP_CHANNEL="android_chrono:alpha"
    fun init(context: Context){
        UMConfigure.setLogEnabled(true);
        UMConfigure.preInit(context, APP_KEY, APP_CHANNEL)
        UMConfigure.init(context, APP_KEY, APP_CHANNEL,UMConfigure.DEVICE_TYPE_PHONE,"")
    }
}