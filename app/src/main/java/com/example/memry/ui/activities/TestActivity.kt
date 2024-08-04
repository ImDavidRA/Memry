package com.example.memry.ui.activities

import AdapterTest
import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memry.dataClasses.Audios
import com.example.memry.databinding.ActivityTestBinding
import java.io.IOException
import java.util.Calendar

class TestActivity : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false
    private lateinit var binding: ActivityTestBinding
    private val audioList = mutableListOf<Audios>()

    private lateinit var adapter: AdapterTest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView: RecyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterTest(audioList)
        recyclerView.adapter = adapter

        binding.buttonStartRecording.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, permissions, 0)
            } else {
                setupMediaRecorder()
                startRecording()
            }
        }

        binding.playButton.setOnClickListener {
            var player: MediaPlayer? = MediaPlayer().apply {
                try {
                    setDataSource(output)
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        binding.buttonStopRecording.setOnClickListener {
            stopRecording()
        }

        binding.buttonPauseRecording.setOnClickListener {
            pauseRecording()
        }
    }

    private fun get_audio_string(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR).toString().padStart(4, '0')
        val m = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val d = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val h = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val min = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        val s = cal.get(Calendar.SECOND).toString().padStart(2, '0')

        return "${externalCacheDir?.absolutePath}/D${y}_${m}_${d}_H${h}_${min}_${s}_audio.mp3"
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
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            Toast.makeText(this, "IllegalStateException: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } catch (e: IOException) {
            Toast.makeText(this, "IOException: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        if (state) {
            if (!recordingStopped) {
                Toast.makeText(this, "Paused!", Toast.LENGTH_SHORT).show()
                mediaRecorder?.pause()
                recordingStopped = true
                binding.buttonPauseRecording.text = "Resume"
            } else {
                resumeRecording()
            }
        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        Toast.makeText(this, "Resumed!", Toast.LENGTH_SHORT).show()
        mediaRecorder?.resume()
        binding.buttonPauseRecording.text = "Pause"
        recordingStopped = false
    }

    private fun stopRecording() {
        if (state) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                state = false
                Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT).show()
                val i = audioList.size + 1

                //TODO: Borrar luego
                val testTitulo = output.toString()
                audioList.add(Audios(testTitulo, "1:50", output))

                adapter.notifyDataSetChanged()
            } catch (e: RuntimeException) {
                Toast.makeText(this, "RuntimeException: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMediaRecorder()
                startRecording()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
