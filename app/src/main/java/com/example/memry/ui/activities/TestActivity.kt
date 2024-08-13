package com.example.memry.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memry.adapters.AdapterTest
import com.example.memry.dataClasses.Audios
import com.example.memry.databinding.ActivityTestBinding
import com.example.memry.helpers.Grabadora
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TestActivity : AppCompatActivity() {


    // TODO: Quitar al acabar la clase "Grabadora"
    private var output: String? = null
    private lateinit var binding: ActivityTestBinding
    private val audioList = mutableListOf<Audios>()
    private lateinit var adapter: AdapterTest
    private var grabadora: Grabadora = Grabadora(this)

    private lateinit var alarmasRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmasRef = FirebaseDatabase.getInstance("https://memry--app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Alarmas")

        val recyclerView: RecyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterTest(audioList, this)
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
                grabadora.grabar_audio()
            }
        }

        binding.playButton.setOnClickListener {
            grabadora.playAudio(output)
        }

        binding.buttonStopRecording.setOnClickListener {
            stopRecording()
        }

        binding.buttonTestRecording.setOnClickListener {
            Toast.makeText(this, "Antes de", Toast.LENGTH_SHORT).show()

            val test: String = "5:30"

            alarmasRef.child("Prueba").setValue(test).addOnCompleteListener {
                    Toast.makeText(this, "Funcionó", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { err ->
                    Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()
                }

            Toast.makeText(this, "Despues de", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        if (grabadora.get_state()) {
            grabadora.parar_grabacion()

            output = grabadora.get_output()

            // Utilizamos el archivo para guardar la duración
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(output)
            mediaPlayer.prepare()
            val durationInSeconds = mediaPlayer.duration / 1000 // Duración en segundos
            mediaPlayer.release()

            // Testeando el titulo del audio
            val testTitulo = output.toString().substringAfter("cache/").substringBefore(".mp3")
            audioList.add(Audios(testTitulo, durationInSeconds, output))

            adapter.notifyDataSetChanged()
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
                grabadora.grabar_audio()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
