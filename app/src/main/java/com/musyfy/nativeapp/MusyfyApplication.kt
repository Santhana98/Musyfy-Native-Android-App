package com.musyfy.nativeapp

import android.app.Application
import com.musyfy.nativeapp.core.analytics.AnalyticsEvent
import com.musyfy.nativeapp.core.analytics.AnalyticsManager
import com.musyfy.nativeapp.core.analytics.AnalyticsConstants
import com.yausername.youtubedl_android.YoutubeDL
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MusyfyApplication : Application() {

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate() {
        super.onCreate()
        
        // Log the custom app_open event once on launch
        analyticsManager.logEvent(
            AnalyticsEvent(name = AnalyticsConstants.Events.APP_OPEN)
        )
        
        // Asynchronously initialize and update the yt-dlp binary rules
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(this@MusyfyApplication)
                YoutubeDL.getInstance().updateYoutubeDL(this@MusyfyApplication, YoutubeDL.UpdateChannel._STABLE)
            } catch (e: Exception) {
                // Ignore initialization failures in offline/no-network states
            }
        }
    }
}
