package com.example.newkursach.fragments

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newkursach.R
import com.example.newkursach.databinding.FragmentAudioPlayerBinding
import com.example.newkursach.viewmodel.AudioPlayerViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip

class AudioPlayerFragment : Fragment() {
    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playbut: ImageButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var filenamebar: TextView
    private lateinit var backbut: ImageButton
    private lateinit var progressTimeStart: TextView
    private lateinit var progressTimeEnd: TextView

    private val args by navArgs<AudioPlayerFragmentArgs>()
    private val fileName by lazy { args.filename }
    private val filePath by lazy { args.filepath }
    private val viewModel: AudioPlayerViewModel by viewModels {
        AudioPlayerViewModel.Factory(
            fileName,
            filePath
        )
    }


    private lateinit var forwardbut: ImageButton
    private lateinit var speedchip: Chip
    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay = 1000L
    private var jumpVal = 1000
    private var playSpeed = 1.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        _binding = FragmentAudioPlayerBinding.inflate(layoutInflater, container, false)

        toolbar = binding.toolBar
        filenamebar = binding.filenameBar

        progressTimeEnd = binding.progressTimeEnd
        progressTimeStart = binding.progressTimeStart

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            val action =
                AudioPlayerFragmentDirections.actionAudioPlayerFragmentToCardAudioFragment()
            findNavController().navigate(action)
        }

        binding.filenameBar.text = fileName

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        binding.progressTimeStart.text = dateFormat(mediaPlayer.duration)

        backbut = binding.backTo
        forwardbut = binding.forwardTo
        playbut = binding.playAudio
        speedchip = binding.speedChip
        seekBar = binding.seekbar

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            binding.progressTimeStart.text = dateFormat(mediaPlayer.currentPosition)
            handler.postDelayed(runnable, delay)
        }

        playbut.setOnClickListener {
            playPause()

        }
        playPause()
        seekBar.max = mediaPlayer.duration
        mediaPlayer.setOnCompletionListener {
            playbut.background =
                ResourcesCompat.getDrawable(resources, R.drawable.playaudio, requireContext().theme)
            handler.removeCallbacks(runnable)
        }
        forwardbut.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpVal)
            seekBar.progress += jumpVal
        }
        backbut.setOnClickListener {
            mediaPlayer.seekTo(mediaPlayer.currentPosition - jumpVal)
            seekBar.progress -= jumpVal
        }
        speedchip.setOnClickListener {
            if (playSpeed != 2f)
                playSpeed += 0.5f
            else
                playSpeed = 0.5f
            mediaPlayer.playbackParams = PlaybackParams().setSpeed(playSpeed)
            speedchip.text = "x $playSpeed"
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mediaPlayer.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        return binding.root
    }

    private fun playPause() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            playbut.background =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.pauseaudio,
                    requireContext().theme
                )
            handler.postDelayed(runnable, delay)
        } else {
            mediaPlayer.pause()
            playbut.background =
                ResourcesCompat.getDrawable(resources, R.drawable.playaudio, requireContext().theme)
            handler.removeCallbacks(runnable)
        }
    }


    private fun dateFormat(duration: Int): String {
        val dur = duration / 1000
        val sec = dur % 60
        val min = dur / 60 % 60
        val hour = dur / 3600
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }

}