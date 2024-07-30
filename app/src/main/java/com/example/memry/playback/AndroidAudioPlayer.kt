package com.example.memry.playback

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import java.io.File
import java.io.IOException

class AndroidAudioPlayer(
    private val context: Context
) : AudioPlayer {

    private var player: MediaPlayer? = null

    override fun playFile(file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "El archivo no existe", Toast.LENGTH_SHORT).show()
            return
        }

        // Liberar el MediaPlayer actual si existe
        player?.release()

        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error al reproducir el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun stop() {
        player?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        player = null
    }
}
