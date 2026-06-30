package com.musyfy.nativeapp

import android.app.Application
import com.yausername.youtubedl_android.YoutubeDL
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MusyfyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
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
