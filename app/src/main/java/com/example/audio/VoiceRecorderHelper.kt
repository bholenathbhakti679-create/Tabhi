package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class VoiceRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null

    var isRecording = false
        private set

    var isPlaying = false
        private set

    fun startRecording(targetFile: File): Boolean {
        return try {
            stopPlayback()
            currentRecordingFile = targetFile
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(targetFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            true
        } catch (e: Exception) {
            Log.e("VoiceRecorderHelper", "Failed to start recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            false
        }
    }

    fun stopRecording(): String? {
        if (!isRecording) return null
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            currentRecordingFile?.absolutePath
        } catch (e: Exception) {
            Log.e("VoiceRecorderHelper", "Failed to stop recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            null
        }
    }

    fun playAudio(filePath: String, onCompletion: () -> Unit) {
        try {
            stopPlayback()
            val file = File(filePath)
            if (!file.exists()) return

            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    isPlaying = false
                    onCompletion()
                }
                start()
            }
            isPlaying = true
        } catch (e: Exception) {
            Log.e("VoiceRecorderHelper", "Failed to play audio", e)
            isPlaying = false
            onCompletion()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            isPlaying = false
        } catch (e: Exception) {
            Log.e("VoiceRecorderHelper", "Error stopping playback", e)
        }
    }

    fun release() {
        stopPlayback()
        if (isRecording) {
            try {
                mediaRecorder?.stop()
            } catch (_: Exception) {}
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }
    }
}
