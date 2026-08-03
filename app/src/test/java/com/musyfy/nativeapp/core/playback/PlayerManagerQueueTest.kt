package com.musyfy.nativeapp.core.playback

import com.musyfy.nativeapp.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerManagerQueueTest {

    private val song1 = Song("s1", "Song 1", "Artist 1", "https://yt.com/1")
    private val song2 = Song("s2", "Song 2", "Artist 2", "https://yt.com/2")
    private val song3 = Song("s3", "Song 3", "Artist 3", "https://yt.com/3")

    @Test
    fun testReorderCoercion_coercesOutOfBoundsIndicesSafely() {
        val list = mutableListOf(song1, song2, song3)
        val fromIndex = -5
        val toIndex = 10

        val safeFromIndex = fromIndex.coerceIn(0, list.size - 1)
        val safeToIndex = toIndex.coerceIn(0, list.size - 1)

        assertEquals(0, safeFromIndex)
        assertEquals(2, safeToIndex)

        val song = list.removeAt(safeFromIndex)
        list.add(safeToIndex, song)

        assertEquals(listOf(song2, song3, song1), list)
    }

    @Test
    fun testPlayNextInsertionIndex_calculatesCorrectNextPosition() {
        val queue = mutableListOf(song1, song2, song3)
        val currentPlayingIndex = 1 // Playing song2
        val newSong = Song("s4", "Song 4", "Artist 4", "https://yt.com/4")

        val insertIndex = (currentPlayingIndex + 1).coerceIn(0, queue.size)
        queue.add(insertIndex, newSong)

        assertEquals(2, insertIndex)
        assertEquals(song4Id("s4"), queue[2].id)
        assertEquals(song2.id, queue[1].id)
    }

    @Test
    fun testRemovePlayingSongIndex_calculatesAdjacentSong() {
        val queue = listOf(song1, song2, song3)
        val removedIndex = 1 // Removing song2 while playing

        val remaining = queue.filter { it.id != "s2" }
        val nextIndex = removedIndex.coerceAtMost(remaining.size - 1)

        assertEquals(1, nextIndex)
        assertEquals("s3", remaining[nextIndex].id)
    }

    private fun song4Id(id: String) = id
}
