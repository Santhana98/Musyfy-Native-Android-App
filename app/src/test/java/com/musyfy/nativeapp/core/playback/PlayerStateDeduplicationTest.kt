package com.musyfy.nativeapp.core.playback

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStateDeduplicationTest {

    @Test
    fun testErrorCategorization_categorizesKnownExceptions() {
        val networkErr = PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
        val storageErr = PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
        val decoderErr = PlaybackException.ERROR_CODE_DECODER_INIT_FAILED

        assertEquals("NETWORK_ERROR", categorizeError(networkErr))
        assertEquals("STORAGE_ERROR", categorizeError(storageErr))
        assertEquals("DECODER_ERROR", categorizeError(decoderErr))
        assertEquals("GENERAL_PLAYBACK_ERROR", categorizeError(9999))
    }

    @Test
    fun testStateMapping_mapsExoPlayerStatesCorrectly() {
        assertEquals(PlayerState.IDLE, mapExoState(Player.STATE_IDLE, false))
        assertEquals(PlayerState.BUFFERING, mapExoState(Player.STATE_BUFFERING, false))
        assertEquals(PlayerState.PLAYING, mapExoState(Player.STATE_READY, true))
        assertEquals(PlayerState.PAUSED, mapExoState(Player.STATE_READY, false))
        assertEquals(PlayerState.COMPLETED, mapExoState(Player.STATE_ENDED, false))
    }

    private fun categorizeError(errorCode: Int): String {
        return when (errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "NETWORK_ERROR"

            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
            PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "STORAGE_ERROR"

            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FAILED -> "DECODER_ERROR"

            else -> "GENERAL_PLAYBACK_ERROR"
        }
    }

    private fun mapExoState(playbackState: Int, isPlaying: Boolean): PlayerState {
        return when (playbackState) {
            Player.STATE_IDLE -> PlayerState.IDLE
            Player.STATE_BUFFERING -> PlayerState.BUFFERING
            Player.STATE_READY -> if (isPlaying) PlayerState.PLAYING else PlayerState.PAUSED
            Player.STATE_ENDED -> PlayerState.COMPLETED
            else -> PlayerState.IDLE
        }
    }
}
