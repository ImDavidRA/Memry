package com.example.memry.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.Toast
import com.example.memry.R
import com.example.memry.databinding.ActivityPrincipalBinding
import com.example.memry.playback.AndroidAudioPlayer
import com.example.memry.recorder.AndroidAudioRecorder
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityPrincipal : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var fAuth: FirebaseAuth

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private var audioFile: File? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            0
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fAuth = FirebaseAuth.getInstance()

        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTexts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnRecAudio.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start recording
                    File(cacheDir, fileName()).also {
                        it.parentFile?.mkdirs()
                        recorder.start(it)
                        audioFile = it
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Stop recording
                    recorder.stop()
                    true
                }
                else -> false
            }
        }

        binding.btnPlayAudio.setOnClickListener {
            player.playFile(audioFile ?: return@setOnClickListener)
        }

        binding.btnStopPlay.setOnClickListener {
            player.stop()
        }

    }

    fun fileName(): String {
        val formatter = SimpleDateFormat("dd_MM_yyyy_HH:mm", Locale.getDefault())
        val currentDateTime = Date()
        val formattedDateTime = formatter.format(currentDateTime)

        return "audio_$formattedDateTime.mp3"
    }

    fun updateTexts() {
        binding .txtEmail.text = fAuth.currentUser?.email.toString()
    }

    override fun onBackPressed() {
        fAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}