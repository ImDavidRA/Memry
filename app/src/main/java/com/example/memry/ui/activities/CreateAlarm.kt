package com.example.memry.ui.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memry.R
import com.example.memry.databinding.ActivityCreateAlarmBinding
import com.example.memry.helpers.AlarmReceiver
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class CreateAlarm : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAlarmBinding
    private lateinit var vibrator: Vibrator
    private val ALARM_REQUEST_CODE = 100

    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = FirebaseDatabase.getInstance()
        dbReference = database.getReference("Test")

        binding = ActivityCreateAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val datePicker: DatePicker = binding.datePicker
        datePicker.setCalendarViewShown(false)

        datePicker.init(datePicker.year, datePicker.month, datePicker.dayOfMonth) { view, year, monthOfYear, dayOfMonth ->
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(10)
            }
        }

        val hourPicker: TimePicker = binding.timePicker
        hourPicker.setIs24HourView(true)

        hourPicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(10)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val button = binding.botonAceptar.setOnClickListener {

            setAlarm(datePicker.year, datePicker.month, datePicker.dayOfMonth, hourPicker.hour, hourPicker.minute, null)
        }

    }






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

}