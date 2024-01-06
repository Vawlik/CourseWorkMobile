package com.example.kursach2

import android.os.Looper

class TimerRecord(listener: OnTimeListener) {
    interface OnTimeListener {
        fun onTime(duration: String)
    }

    private var handler = android.os.Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var duration = 0L
    private var delay = 100L

    init {
        runnable = Runnable {
            duration += delay
            handler.postDelayed(runnable, delay)
            listener.onTime(format())
        }
    }

    fun start() {
        handler.postDelayed(runnable, delay)
    }

    fun pause() {
        handler.removeCallbacks(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0L
    }

    fun format(): String {
        val milsec = duration % 1000
        val sec = (duration / 1000) % 60
        val min = (duration / (1000 * 60)) % 60
        val hour = (duration / (1000 * 60 * 60))
        var formatted = if (hour > 0)
            "%02d:%02d:%02d:%02d".format(hour, min, sec, milsec / 10)
        else
            "%02d:%02d:%02d".format(min, sec, milsec / 10)
        return formatted
    }
}