package com.example.newkursach

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.newkursach.databinding.ActivityAudioPlayerBinding
import com.example.newkursach.databinding.RecyclerItemBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioPlayerBinding
    private lateinit var recyclerBinding: RecyclerItemBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playbut: ImageButton
    private lateinit var toolbar: MaterialToolbar
    private lateinit var filenamebar: TextView
    private lateinit var backbut: ImageButton
    private lateinit var progressTimeStart: TextView
    private lateinit var progressTimeEnd: TextView


    private lateinit var forwardbut: ImageButton
    private lateinit var speedchip: Chip
    private lateinit var seekBar: SeekBar
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay = 1000L
    private var jumpVal = 1000
    private var playSpeed = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerBinding = RecyclerItemBinding.inflate(layoutInflater)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("filepath")
        val fileName = intent.getStringExtra("filename")

        toolbar = binding.toolBar
        filenamebar = binding.filenameBar

        progressTimeEnd = binding.progressTimeEnd
        progressTimeStart = binding.progressTimeStart

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
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
            playbut.background = ResourcesCompat.getDrawable(resources, R.drawable.playaudio, theme)
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
    }

    private fun playPause() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            playbut.background =
                ResourcesCompat.getDrawable(resources, R.drawable.pauseaudio, theme)
            handler.postDelayed(runnable, delay)
        } else {
            mediaPlayer.pause()
            playbut.background = ResourcesCompat.getDrawable(resources, R.drawable.playaudio, theme)
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
        super.onBackPressed()
    }

    private fun dateFormat(duration: Int): String {
        val dur = duration / 1000
        val sec = dur % 60
        val min = dur / 60 % 60
        val hour = dur / 3600
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }
}