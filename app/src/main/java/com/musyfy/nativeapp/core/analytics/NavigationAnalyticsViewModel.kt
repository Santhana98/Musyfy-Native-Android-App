package com.musyfy.nativeapp.core.analytics

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationAnalyticsViewModel @Inject constructor(
    val tracker: NavigationAnalyticsTracker
) : ViewModel()
