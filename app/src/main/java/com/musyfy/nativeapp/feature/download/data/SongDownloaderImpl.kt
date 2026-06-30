package com.musyfy.nativeapp.feature.download.data

import android.content.Context
import com.musyfy.nativeapp.domain.model.Song
import com.musyfy.nativeapp.domain.repository.SongRepository
import com.musyfy.nativeapp.feature.download.domain.SongDownloader
import com.musyfy.nativeapp.feature.download.domain.model.DownloadStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongDownloaderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository
) : SongDownloader {

    private val okHttpClient = OkHttpClient()
    
    // Concurrent map to keep track of active download jobs
    private val activeJobs = ConcurrentHashMap<String, Job>()
    
    // In-memory cache of download statuses to expose state flows easily
    private val _downloadStatuses = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    override val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = _downloadStatuses.asStateFlow()

    init {
        // Initialize statuses from existing files in filesDir
        val files = context.filesDir.listFiles()
        val initialMap = mutableMapOf<String, DownloadStatus>()
        files?.forEach { file ->
            if ((file.name.endsWith(".mp3") || file.name.endsWith(".m4a")) && file.length() > 0) {
                val songId = file.name.substringBeforeLast(".")
                initialMap[songId] = DownloadStatus.Downloaded(file.absolutePath)
            }
        }
        _downloadStatuses.value = initialMap
    }

    override fun getDownloadStatus(songId: String): Flow<DownloadStatus> = flow {
        _downloadStatuses.collect { statuses ->
            val status = statuses[songId] ?: DownloadStatus.NotDownloaded
            emit(status)
        }
    }

    override fun downloadSong(song: Song): Flow<DownloadStatus> = flow {
        val songId = song.id
        val isYoutube = song.url.contains("youtube.com") || song.url.contains("youtu.be") || song.url.contains("youtube")
        
        val tempFile = File(context.filesDir, if (isYoutube) "$songId.m4a.tmp" else "$songId.mp3.tmp")
        val destFile = File(context.filesDir, if (isYoutube) "$songId.m4a" else "$songId.mp3")

        val repositorySong = songRepository.getSongs().first().find { it.id == songId }
        val finalAudioPath = repositorySong?.audioPath ?: destFile.absolutePath
        val audioFile = File(finalAudioPath)
        if (audioFile.exists() && audioFile.length() > 0) {
            emit(DownloadStatus.Downloaded(audioFile.absolutePath))
            return@flow
        }

        emit(DownloadStatus.Downloading(0f))
        _downloadStatuses.update { it + (songId to DownloadStatus.Downloading(0f)) }

        try {
            if (isYoutube) {
                // Initialize YoutubeDL
                YoutubeDL.getInstance().init(context)
                
                val request = YoutubeDLRequest(song.url).apply {
                    addOption("-f", "ba[ext=m4a]/bestaudio")
                    addOption("-o", tempFile.absolutePath)
                }
                
                val response = YoutubeDL.getInstance().execute(request) { progress, _, _ ->
                    val fraction = progress / 100f
                    _downloadStatuses.update { it + (songId to DownloadStatus.Downloading(fraction)) }
                }
                
                if (response.exitCode == 0 && tempFile.exists() && tempFile.length() > 0) {
                    // Download artwork locally too
                    downloadArtworkLocally(song.imageUrl, songId)
                    
                    if (tempFile.renameTo(destFile)) {
                        val currentSong = songRepository.getSongs().first().find { it.id == songId } ?: song
                        val artworkFile = File(context.filesDir, "$songId.jpg")
                        val updatedSong = currentSong.copy(
                            audioPath = destFile.absolutePath,
                            artworkPath = if (artworkFile.exists() && artworkFile.length() > 0) artworkFile.absolutePath else null
                        )
                        songRepository.addSong(updatedSong)

                        emit(DownloadStatus.Downloaded(destFile.absolutePath))
                        _downloadStatuses.update { it + (songId to DownloadStatus.Downloaded(destFile.absolutePath)) }
                    } else {
                        throw Exception("Failed to rename temp file to dest file")
                    }
                } else {
                    throw Exception("Download failed with exit code ${response.exitCode}")
                }
            } else {
                // Fallback to OkHttp direct download
                var existingLength = 0L
                if (tempFile.exists()) {
                    existingLength = tempFile.length()
                }

                val requestBuilder = Request.Builder().url(song.url)
                if (existingLength > 0) {
                    requestBuilder.header("Range", "bytes=$existingLength-")
                }

                val request = requestBuilder.build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    if (response.code == 416) {
                        tempFile.delete()
                        existingLength = 0L
                        downloadFromScratch(song, tempFile, destFile)
                        
                        downloadArtworkLocally(song.imageUrl, songId)
                        val currentSong = songRepository.getSongs().first().find { it.id == songId } ?: song
                        val artworkFile = File(context.filesDir, "$songId.jpg")
                        val updatedSong = currentSong.copy(
                            audioPath = destFile.absolutePath,
                            artworkPath = if (artworkFile.exists() && artworkFile.length() > 0) artworkFile.absolutePath else null
                        )
                        songRepository.addSong(updatedSong)

                        emit(DownloadStatus.Downloaded(destFile.absolutePath))
                        _downloadStatuses.update { it + (songId to DownloadStatus.Downloaded(destFile.absolutePath)) }
                    } else {
                        throw Exception("Server returned code ${response.code}")
                    }
                } else {
                    val body = response.body ?: throw Exception("Response body is null")
                    val responseLength = body.contentLength()
                    val totalLength = if (response.code == 206) {
                        responseLength + existingLength
                    } else {
                        tempFile.delete()
                        existingLength = 0
                        responseLength
                    }

                    val inputStream = body.byteStream()
                    val randomAccessFile = RandomAccessFile(tempFile, "rw")
                    randomAccessFile.seek(existingLength)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesDownloaded = existingLength

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        randomAccessFile.write(buffer, 0, bytesRead)
                        totalBytesDownloaded += bytesRead
                        val progress = if (totalLength > 0) totalBytesDownloaded.toFloat() / totalLength else 0f
                        
                        emit(DownloadStatus.Downloading(progress))
                        _downloadStatuses.update { it + (songId to DownloadStatus.Downloading(progress)) }
                    }

                    randomAccessFile.close()
                    inputStream.close()
                    body.close()

                    if (tempFile.renameTo(destFile)) {
                        downloadArtworkLocally(song.imageUrl, songId)
                        val currentSong = songRepository.getSongs().first().find { it.id == songId } ?: song
                        val artworkFile = File(context.filesDir, "$songId.jpg")
                        val updatedSong = currentSong.copy(
                            audioPath = destFile.absolutePath,
                            artworkPath = if (artworkFile.exists() && artworkFile.length() > 0) artworkFile.absolutePath else null
                        )
                        songRepository.addSong(updatedSong)

                        emit(DownloadStatus.Downloaded(destFile.absolutePath))
                        _downloadStatuses.update { it + (songId to DownloadStatus.Downloaded(destFile.absolutePath)) }
                    } else {
                        throw Exception("Failed to save downloaded file")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SongDownloaderImpl", "downloadSong: failed for song $songId", e)
            val currentStatus = _downloadStatuses.value[songId]
            if (currentStatus is DownloadStatus.Downloading) {
                emit(DownloadStatus.Error(e.message ?: "Unknown error"))
                _downloadStatuses.update { it + (songId to DownloadStatus.Error(e.message ?: "Unknown error")) }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun downloadFromScratch(song: Song, tempFile: File, destFile: File) {
        val request = Request.Builder().url(song.url).build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("Retry failed code ${response.code}")
        val body = response.body ?: throw Exception("Response body is null")
        val inputStream = body.byteStream()
        val outputStream = tempFile.outputStream()
        
        val buffer = ByteArray(8192)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.close()
        inputStream.close()
        body.close()
        tempFile.renameTo(destFile)
    }

    private fun downloadArtworkLocally(imageUrl: String?, songId: String) {
        if (imageUrl.isNullOrEmpty()) return
        try {
            val file = File(context.filesDir, "$songId.jpg")
            val request = Request.Builder().url(imageUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    file.outputStream().use { out ->
                        body.byteStream().use { inp ->
                            inp.copyTo(out)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore artwork download failure, not fatal
        }
    }

    override fun pauseDownload(songId: String) {
        activeJobs[songId]?.cancel()
        activeJobs.remove(songId)
        _downloadStatuses.update { it + (songId to DownloadStatus.NotDownloaded) }
    }

    override fun cancelDownload(songId: String) {
        activeJobs[songId]?.cancel()
        activeJobs.remove(songId)
        val tempFileMp3 = File(context.filesDir, "$songId.mp3.tmp")
        if (tempFileMp3.exists()) tempFileMp3.delete()
        val tempFileM4a = File(context.filesDir, "$songId.m4a.tmp")
        if (tempFileM4a.exists()) tempFileM4a.delete()
        _downloadStatuses.update { it - songId }
    }

    override fun deleteDownloadedSong(songId: String): Boolean {
        cancelDownload(songId)
        val destFileMp3 = File(context.filesDir, "$songId.mp3")
        val deletedMp3 = if (destFileMp3.exists()) destFileMp3.delete() else false
        val destFileM4a = File(context.filesDir, "$songId.m4a")
        val deletedM4a = if (destFileM4a.exists()) destFileM4a.delete() else false
        val artworkFile = File(context.filesDir, "$songId.jpg")
        if (artworkFile.exists()) artworkFile.delete()
        _downloadStatuses.update { it - songId }
        return deletedMp3 || deletedM4a
    }

    fun registerJob(songId: String, job: Job) {
        activeJobs[songId] = job
    }

    fun clearJob(songId: String) {
        activeJobs.remove(songId)
    }
}
