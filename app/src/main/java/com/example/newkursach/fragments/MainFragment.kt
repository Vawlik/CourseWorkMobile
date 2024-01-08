package com.example.newkursach.fragments

import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newkursach.R
import com.example.newkursach.data.AudioRecord
import com.example.newkursach.databinding.FragmentMainBinding
import com.example.newkursach.secondary.MICROPHONE_REQUEST_CODE
import com.example.newkursach.secondary.OnTimeListener
import com.example.newkursach.secondary.PERMISSIONS_REQUEST_LOCATION
import com.example.newkursach.secondary.TimerRecord
import com.example.newkursach.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date


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
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var timerRecord: TimerRecord
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        permGrand = ActivityCompat.checkSelfPermission(
            requireContext(), perm[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permGrand) ActivityCompat.requestPermissions(
            requireActivity(), perm, MICROPHONE_REQUEST_CODE
        )

        timerRecord = TimerRecord(this)

        binding.record.setOnClickListener {
            when {
                isPau -> resumeRec()
                isRec -> pauseRec()
                else -> startRec()
            }
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }

        binding.menu.setOnClickListener {
            Toast.makeText(requireContext(), R.string.saved_records, Toast.LENGTH_SHORT).show()
            val action = MainFragmentDirections.actionMainFragmentToCardAudioFragment()
            findNavController().navigate(action)
        }

        binding.donebut.setOnClickListener {
            stopRec()
            showSaveDialog()
            Toast.makeText(requireContext(), R.string.recording_saved, Toast.LENGTH_SHORT).show()
        }

        binding.close.setOnClickListener {
            stopRec()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(requireContext(), R.string.recording_deleted, Toast.LENGTH_SHORT).show()
        }
        binding.close.isClickable = false
        return binding.root
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    this.latitude = location.latitude
                    this.longitude = location.longitude
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MICROPHONE_REQUEST_CODE) permGrand =
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            permGrand = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (permGrand) {
                requestLocation()
            }
        }
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
            ActivityCompat.requestPermissions(requireActivity(), perm, MICROPHONE_REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${requireActivity().externalCacheDir?.absolutePath}/"
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date: String = simpleDateFormat.format(Date())
        filename = "audio_record_$date"
        recorder.apply {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioChannels(1);
            recorder.setAudioEncodingBitRate(192000);
            recorder.setOutputFile("$dirPath$filename.mp3");

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

        alertDialogBuilder.setTitle(R.string.save_dialog_title)

        val input = EditText(requireContext())
        input.hint = getString(R.string.filename_hint)
        input.setText(filename)

        alertDialogBuilder.setView(input)

        alertDialogBuilder.setPositiveButton(R.string.save_button) { _, _ ->
            val fileName = input.text.toString()
            if (fileName != filename) {
                val newFile = File("$dirPath$fileName.mp3")
                File("$dirPath$filename.mp3").renameTo(newFile)
            }
            Toast.makeText(
                requireContext(), getString(R.string.record_saved_as, fileName), Toast.LENGTH_SHORT
            ).show()
            val filePath = "$dirPath$fileName.mp3"
            val timestamp = Date().time


            viewModel.insertAudio(
                AudioRecord(
                    null, fileName, filePath, timestamp, duration,  latitude, longitude
                )
            )
        }

        alertDialogBuilder.setNegativeButton(R.string.cancel_button) { _, _ ->
            Toast.makeText(requireContext(), R.string.save_cancelled, Toast.LENGTH_SHORT).show()
        }

        alertDialogBuilder.show()
    }

}