package com.example.appnote

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appnote.databinding.ActivityWriteNoteBinding
import java.io.FileOutputStream
import java.util.*

class WriteNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteNoteBinding
    private var reminderCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener para el selector de fecha
        binding.btnSelectDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                reminderCalendar.set(Calendar.YEAR, year)
                reminderCalendar.set(Calendar.MONTH, month)
                reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeDisplay()
            }, reminderCalendar.get(Calendar.YEAR), reminderCalendar.get(Calendar.MONTH), reminderCalendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        // Listener para el selector de hora
        binding.btnSelectTime.setOnClickListener {
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                reminderCalendar.set(Calendar.MINUTE, minute)
                updateDateTimeDisplay()
            }, reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), true)
            timePickerDialog.show()
        }

        // Listener para el botón de guardar
        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString()
            val content = binding.etNoteContent.text.toString()

            // Guardar la nota en el almacenamiento interno
            saveNoteToFile(title, content)

            // Si el usuario desea establecer un recordatorio
            if (binding.cbSetReminder.isChecked) {
                setReminder(reminderCalendar.timeInMillis, title, content)
            }

            // Mostrar mensaje de confirmación
            Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show()
            // Cerrar la actividad
            finish()
        }
    }

    private fun updateDateTimeDisplay() {
        binding.tvDateTime.text = reminderCalendar.time.toString()
    }

    private fun saveNoteToFile(title: String, content: String) {
        val filename = "notes.txt"
        val fileContents = "Title: $title\nContent: $content\n\n"

        try {
            // Abre un flujo de salida para escribir en el archivo
            val fileOutputStream: FileOutputStream = openFileOutput(filename, Context.MODE_APPEND)
            // Escribe los datos en el archivo
            fileOutputStream.write(fileContents.toByteArray())
            // Cierra el flujo de salida
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setReminder(timeInMillis: Long, title: String, content: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderBroadcastReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("content", content)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        // Configurar recordatorios adicionales (un día antes y una hora antes)
        val reminderDayBefore = reminderCalendar.clone() as Calendar
        reminderDayBefore.add(Calendar.DAY_OF_YEAR, -1)
        val dayBeforeIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderDayBefore.timeInMillis, dayBeforeIntent)

        val reminderHourBefore = reminderCalendar.clone() as Calendar
        reminderHourBefore.add(Calendar.HOUR_OF_DAY, -1)
        val hourBeforeIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderHourBefore.timeInMillis, hourBeforeIntent)
    }
}
