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

        // Set environment User Properties once on launch
        val deviceModel = android.os.Build.MODEL
        val androidVersion = android.os.Build.VERSION.RELEASE
        val appVersion = try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
                packageManager.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

        analyticsManager.setUserProperty(AnalyticsConstants.UserProperties.DEVICE_MODEL, deviceModel)
        analyticsManager.setUserProperty(AnalyticsConstants.UserProperties.ANDROID_VERSION, androidVersion)
        analyticsManager.setUserProperty(AnalyticsConstants.UserProperties.APP_VERSION, appVersion)
        
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
