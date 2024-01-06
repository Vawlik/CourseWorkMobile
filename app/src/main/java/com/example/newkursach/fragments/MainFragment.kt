package com.example.newkursach.fragments

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newkursach.R
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.databinding.FragmentMainBinding
import com.example.newkursach.secondary.OnTimeListener
import com.example.newkursach.secondary.TimerRecord
import com.example.newkursach.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date

const val REQUEST_CODE = 200

class MainFragment : Fragment(), OnTimeListener {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var wave: List<Float>
    private var perm = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permGrand = false
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRec = false
    private var isPau = false
    private var duration = ""
    private lateinit var timerRecord: TimerRecord

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)

        permGrand =
            ActivityCompat.checkSelfPermission(
                requireContext(),
                perm[0]
            ) == PackageManager.PERMISSION_GRANTED
        if (!permGrand) ActivityCompat.requestPermissions(requireActivity(), perm, REQUEST_CODE)

        timerRecord = TimerRecord(this)

        binding.record.setOnClickListener {
            when {
                isPau -> resumeRec()
                isRec -> pauseRec()
                else -> startRec()
            }
        }
        binding.menu.setOnClickListener {
            Toast.makeText(requireContext(), "Сохраненные записи", Toast.LENGTH_SHORT).show()
            val action = MainFragmentDirections.actionMainFragmentToCardAudioFragment()
            findNavController().navigate(action)
        }
        binding.donebut.setOnClickListener {
            stopRec()
            showSaveDialog()
            Toast.makeText(requireContext(), "Запись сохранена", Toast.LENGTH_SHORT).show()
        }

        binding.close.setOnClickListener {
            stopRec()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
        }
        binding.close.isClickable = false
        return binding.root
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
            ActivityCompat.requestPermissions(requireActivity(), perm, REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${requireActivity().externalCacheDir?.absolutePath}/"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date: String = simpleDateFormat.format(Date())
        filename = "audio_record_$date"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")
            try {
                prepare()
            } catch (_: IOException) {
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
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Сохранить запись?")

        val input = EditText(requireContext())
        input.hint = "Введите название файла"
        input.setText(filename)

        alertDialogBuilder.setView(input)
        alertDialogBuilder.setPositiveButton("Сохранить") { _, _ ->
            val fileName = input.text.toString()
            if (fileName != filename) {
                val newFile = File("$dirPath$fileName.mp3")
                File("$dirPath$filename.mp3").renameTo(newFile)
            }
            Toast.makeText(requireContext(), "Запись сохранена как $fileName", Toast.LENGTH_SHORT)
                .show()
            val filePath = "$dirPath$fileName.mp3"
            val timestamp = Date().time
            val wavesPath = "$dirPath$fileName"

            try {
                val fos = FileOutputStream(wavesPath)
                val out = ObjectOutputStream(fos)
                out.writeObject(wave)
                fos.close()
                out.close()
            } catch (_: IOException) {

            }
            viewModel.insertAudio(
                AudioRecord(
                    null,
                    fileName,
                    filePath,
                    timestamp,
                    duration,
                    wavesPath
                )
            )
        }

        alertDialogBuilder.setNegativeButton("Удалить") { _, _ ->
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
        }
        alertDialogBuilder.show()
    }

}