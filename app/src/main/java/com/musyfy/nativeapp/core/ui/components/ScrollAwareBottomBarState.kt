package com.musyfy.nativeapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * State and scroll connection to observe scroll direction globally
 * for Blinkit-style bottom navigation bar hide/show behavior.
 *
 * It ONLY observes scrolling deltas (returns Offset.Zero) so it never interferes
 * with child scrollable components (LazyColumn, verticalScroll, etc.).
 */
@Stable
class ScrollAwareBottomBarState {
    var isVisible by mutableStateOf(true)
        private set

    private var accumulatedDelta by mutableFloatStateOf(0f)
    private val scrollThreshold = 14f // Smooth scroll sensitivity threshold in pixels

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            // Only respond to vertical scrolling
            if (kotlin.math.abs(delta) > 0.5f) {
                accumulatedDelta += delta
                if (accumulatedDelta < -scrollThreshold) {
                    if (isVisible) {
                        isVisible = false
                    }
                    accumulatedDelta = 0f
                } else if (accumulatedDelta > scrollThreshold) {
                    if (!isVisible) {
                        isVisible = true
                    }
                    accumulatedDelta = 0f
                }
            }
            // Always return Offset.Zero so nested scrolling is never consumed or interfered with
            return Offset.Zero
        }
    }

    fun show() {
        isVisible = true
        accumulatedDelta = 0f
    }
}

@Composable
fun rememberScrollAwareBottomBarState(): ScrollAwareBottomBarState {
    return remember { ScrollAwareBottomBarState() }
}
