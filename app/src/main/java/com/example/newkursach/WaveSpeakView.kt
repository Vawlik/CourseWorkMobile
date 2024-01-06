package com.example.newkursach

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveSpeakView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var paint = Paint()
    private var wave = ArrayList<Float>()
    private var spike = ArrayList<RectF>()
    private var radius = 6f
    private var width = 9f
    private var d = 6f
    private var swidth = 0f
    private var shight = 400f
    private var maxSpike = 0

    init {
        paint.color = Color.rgb(244, 81, 30)
        swidth = resources.displayMetrics.widthPixels.toFloat()
        maxSpike = (swidth / (width + d)).toInt()
    }

    fun addWave(amp: Float) {
        var norm = Math.min(amp.toInt() / 50, 400).toFloat()
        wave.add(norm)

        spike.clear()
        var waves = wave.takeLast(maxSpike)
        for (i in waves.indices) {
            var left = swidth - i * (width + d)
            var top = (shight / 2 - waves[i] / 2) + 10f
            var right = left + width
            var bottom = (top + 5f) + waves[i]
            spike.add(RectF(left, top, right, bottom))
        }
        invalidate()
    }

    fun clear(): ArrayList<Float> {
        var waves = wave.clone() as ArrayList<Float>
        wave.clear()
        spike.clear()
        invalidate()
        return waves
    }


    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        spike.forEach {
            canvas.drawRoundRect(it, radius, radius, paint)
        }
    }
}