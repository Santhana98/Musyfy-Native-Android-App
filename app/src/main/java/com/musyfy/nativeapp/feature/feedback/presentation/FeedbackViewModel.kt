package com.musyfy.nativeapp.feature.feedback.presentation

import androidx.lifecycle.ViewModel
import com.musyfy.nativeapp.core.analytics.AnalyticsEvent
import com.musyfy.nativeapp.core.analytics.AnalyticsManager
import com.musyfy.nativeapp.data.local.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    suspend fun canInitiateFeedback(): Boolean {
        return preferencesManager.canInitiateFeedback(maxAllowed = 3)
    }

    suspend fun recordFeedbackInitiated() {
        preferencesManager.recordFeedbackInitiated()
    }

    fun logFeedbackSendInitiated() {
        analyticsManager.logEvent(AnalyticsEvent.feedbackSendInitiated())
    }
}
