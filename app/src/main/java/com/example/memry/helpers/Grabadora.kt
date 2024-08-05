package com.example.memry.helpers

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.widget.Toast
import java.io.IOException
import java.util.Calendar

class Grabadora(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private var output: String? = null

    fun test() {
        Toast.makeText(context, "Prueba Grabadora", Toast.LENGTH_SHORT).show()
    }

    fun get_output(): String? {
        return output
    }

    fun get_state(): Boolean {
        return state
    }

    fun grabar_audio() {
        setupMediaRecorder()
        startRecording()
    }

    fun play_audio(archivo: String?) {
        var player: MediaPlayer? = MediaPlayer().apply {
            try {
                setDataSource(archivo)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupMediaRecorder() {
        output = get_audio_string()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output)
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(context, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            Toast.makeText(context, "IllegalStateException: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(context, "IOException: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun parar_grabacion() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            state = false
            Toast.makeText(context, "Grabación Finalizada", Toast.LENGTH_SHORT).show()

        } catch (e: RuntimeException) {
            Toast.makeText(context, "RuntimeException: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // Función que devuelve un String con nombre de la fecha y hora actual
    private fun get_audio_string(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR).toString().padStart(4, '0')
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val h = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val s = cal.get(Calendar.SECOND).toString().padStart(2, '0')

        return "${context.externalCacheDir?.absolutePath}/D${y}_${m}_${d}_H${h}_${min}_${s}_audio.mp3"
    }

}