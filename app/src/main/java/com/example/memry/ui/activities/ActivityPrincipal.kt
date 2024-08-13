package com.example.memry.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memry.R
import com.example.memry.databinding.ActivityPrincipalBinding
import com.example.memry.helpers.AlarmReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ActivityPrincipal : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dateButton : Button

    private val RC_NOTIFICATION = 99
    private val ALARM_REQUEST_CODE = 100

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbReference = database.getReference("Alarmas")

        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.VIBRATE, Manifest.permission.INTERNET), RC_NOTIFICATION)
        }

        binding.btnComencemos.setOnClickListener {
            //showDialogMemoria()
            startActivity(Intent(this, TestActivity::class.java))
            finish()
        }

        binding.imgSettings.setOnClickListener {
            showDialogMemoria()
        }
    }

    // Pedir permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_NOTIFICATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Allowed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initDatePicker(
    ) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val monthAdjusted = month + 1
            dateButton.text = "$day/$monthAdjusted/$year"
        }

        val cal: Calendar = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val style = android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK

        datePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)
    }

    private fun showDialogMemoria() {

        addDatatoFirebase("La hora de la alarma ha sido X")

        initDatePicker()

        val dialogMemory = Dialog(this)
        dialogMemory.setContentView(R.layout.dialog_new_memory)
        dialogMemory.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogMemory.window?.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custom_dialog_bg))
        dialogMemory.setCancelable(false)
        dialogMemory.setCanceledOnTouchOutside(true)

        // Variables tipo botones y textos
        val date_button: Button = dialogMemory.findViewById(R.id.datePickerButton)
        val aceptar_dialog_memory: Button = dialogMemory.findViewById(R.id.confirmPop)
        val cerrar_dialog_memory: Button = dialogMemory.findViewById(R.id.cancelPop)
        val edt_hora: EditText = dialogMemory.findViewById(R.id.edt_hora)
        val edt_nombre : EditText = dialogMemory.findViewById(R.id.edt_nombre)

        // Para obtener los valores actuales del día y la hora
        val cal: Calendar = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hora = cal.get(Calendar.HOUR_OF_DAY)
        val minutos = cal.get(Calendar.MINUTE)

        date_button.setText("$day/$month/$year")
        edt_hora.setText("$hora:$minutos")

        date_button.setOnClickListener {
            datePickerDialog.show()
        }

        aceptar_dialog_memory.setOnClickListener {
            createNotificationChannel()

            // TODO: Cambiar para hacerlo con un dropdown
            //val reason = "Cumpleaños"

            val title = if (edt_nombre.text.toString().isBlank()) {
                null
            } else {
                edt_nombre.text.toString().replaceFirstChar { it.uppercaseChar() }
            }

            // Para obtener el valor de horas y minutos del texto
            val hourText = edt_hora.text.toString()
            val hour = hourText.split(":")[0].toInt()
            val min = hourText.split(":")[1].toInt()

            //setAlarm(year, month, day, hour, min, title)

            dialogMemory.dismiss()
        }

        cerrar_dialog_memory.setOnClickListener {
            dialogMemory.dismiss()
        }

        dialogMemory.show()
    }

    private fun addDatatoFirebase(test: String) {
        dbReference.setValue(test)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Data added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Failed to add data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Fail to add data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Permite poner una alarma con la fecha especificada
    private fun setAlarm(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        title: String?
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        scheduleAlarm(this, calendar.timeInMillis, title)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_channel_id"
            val channelName = "Alarm Channel"
            val channelDescription = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun scheduleAlarm(
        context: Context,
        triggerTime: Long,
        title: String?
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            //putExtra("reason", reason)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

        Toast.makeText(this, "Alarma programada", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        fAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
