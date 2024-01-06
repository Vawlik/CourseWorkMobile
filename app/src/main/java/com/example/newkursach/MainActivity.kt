package com.example.newkursach

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.kursach2.TimerRecord
import com.example.newkursach.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date

const val REQUEST_CODE = 200
const val LOCATION_PERMISSION_REQUEST_CODE = 201

class MainActivity : AppCompatActivity(), TimerRecord.OnTimeListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var wave: ArrayList<Float>
    private var perm = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permGrand = false
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRec = false
    private var isPau = false
    private var duration = ""
    private lateinit var timerRecord: TimerRecord

    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permGrand =
            ActivityCompat.checkSelfPermission(this, perm[0]) == PackageManager.PERMISSION_GRANTED
        if (!permGrand) ActivityCompat.requestPermissions(this, perm, REQUEST_CODE)


        db = Room.databaseBuilder(this, AppDatabase::class.java, "audioRecords").build()

        timerRecord = TimerRecord(this)

        binding.record.setOnClickListener {
            when {
                isPau -> resumeRec()
                isRec -> pauseRec()
                else -> startRec()
            }
        }
        binding.menu.setOnClickListener {
            Toast.makeText(this, "Сохраненные записи", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CardAudioActivity::class.java))
        }
        binding.donebut.setOnClickListener {
            stopRec()
            showSaveDialog()
            Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show()
        }

        binding.close.setOnClickListener {
            stopRec()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show()
        }
        binding.close.isClickable = false
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) permGrand =
            grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun pauseRec() {
        recorder.pause()
        isPau = true
        binding.record.setImageResource(R.drawable.cyclerec)
        timerRecord.pause()
    }

    private fun resumeRec() {
        recorder.resume()
        isPau = false
        binding.record.setImageResource(R.drawable.pause)
        timerRecord.start()
    }

    private fun startRec() {
        if (!permGrand) {
            ActivityCompat.requestPermissions(this, perm, REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date: String = simpleDateFormat.format(Date())
        filename = "audio_record_$date"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            try {
                prepare()
            } catch (e: IOException) {
            }
            start()
        }
        binding.record.setImageResource(R.drawable.pause)
        isRec = true
        isPau = false
        timerRecord.start()
        binding.close.isClickable = true
        binding.close.setImageResource(R.drawable.close)
        binding.menu.visibility = View.GONE
        binding.donebut.visibility = View.VISIBLE
    }

    private fun stopRec() {
        timerRecord.stop()
        recorder.apply {
            stop()
            release()
        }
        isPau = false
        isRec = false
        binding.menu.visibility = View.VISIBLE
        binding.donebut.visibility = View.GONE
        binding.close.isClickable = false
        binding.close.setImageResource(R.drawable.close)
        binding.record.setImageResource(R.drawable.cyclerec)
        binding.timerMain.text = "00:00:00"
        wave = binding.waveSpeak.clear()
    }

    override fun onTime(duration: String) {
        binding.timerMain.text = duration
        this.duration = duration.dropLast(3)
        binding.waveSpeak.addWave(recorder.maxAmplitude.toFloat())

    }

    private fun showSaveDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Сохранить запись?")

        val input = EditText(this)
        input.hint = "Введите название файла"
        input.setText(filename)

        alertDialogBuilder.setView(input)
        alertDialogBuilder.setPositiveButton("Сохранить") { _, _ ->
            val fileName = input.text.toString()
            if (fileName != filename) {
                var newFile = File("$dirPath$fileName.mp3")
                File("$dirPath$filename.mp3").renameTo(newFile)
            }
            Toast.makeText(this, "Запись сохранена как $fileName", Toast.LENGTH_SHORT).show()
            var filePath = "$dirPath$fileName.mp3"
            var timestamp = Date().time
            var wavesPath = "$dirPath$fileName"

            try {
                var fos = FileOutputStream(wavesPath)
                var out = ObjectOutputStream(fos)
                out.writeObject(wave)
                fos.close()
                out.close()
            } catch (e: IOException) {

            }
            var record = AudioRecord(fileName, filePath, timestamp, duration, wavesPath)
            GlobalScope.launch {
                db.audioRecordDao().insert(record)
            }
        }

        alertDialogBuilder.setNegativeButton("Удалить") { _, _ ->
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show()
        }
//привет
        alertDialogBuilder.show()
    }
}