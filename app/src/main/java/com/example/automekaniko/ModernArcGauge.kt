package com.example.automekaniko

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class ModernArcGauge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var value: Float = 0f
    private var targetValue: Float = 0f
    private var minValue: Float = 0f
    private var maxValue: Float = 100f
    private var unit: String = ""
    private var warningThreshold: Float = Float.MAX_VALUE
    private var warningIsAbove: Boolean = true

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = context.getColor(R.color.gauge_track)
    }

    private var normalColor = context.getColor(R.color.theme_blue)
    private var warningColor = context.getColor(R.color.theme_red)

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = normalColor
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.app_text_primary)
        textSize = 48f
        isFakeBoldText = true
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.app_text_tertiary)
        textSize = 24f
    }

    private val arcBounds = RectF()
    private var animator: ValueAnimator? = null

    init {
        // Handle custom attributes if needed in the future
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 20f
        val strokeWidth = w * 0.1f
        trackPaint.strokeWidth = strokeWidth
        progressPaint.strokeWidth = strokeWidth
        
        val size = Math.min(w, h).toFloat() - padding * 2 - strokeWidth
        val left = (w - size) / 2
        val top = (h - size) / 2
        arcBounds.set(left, top, left + size, top + size)
        
        valuePaint.textSize = size * 0.25f
        unitPaint.textSize = size * 0.12f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val startAngle = 135f
        val sweepAngle = 270f
        
        // Update color based on value
        val isWarning = if (warningIsAbove) value >= warningThreshold else value <= warningThreshold
        progressPaint.color = if (isWarning) warningColor else normalColor

        // Draw Track
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, trackPaint)

        // Draw Progress
        val progressSweep = ((value - minValue) / (maxValue - minValue)) * sweepAngle
        canvas.drawArc(arcBounds, startAngle, progressSweep.coerceIn(0f, sweepAngle), false, progressPaint)

        // Draw Value Text
        val centerX = width / 2f
        val centerY = height / 2f
        
        val displayValue = if (value >= 1000) "%.0f".format(value) else "%.1f".format(value)
        canvas.drawText(displayValue, centerX, centerY + (valuePaint.textSize / 3), valuePaint)
        
        // Draw Unit Text
        canvas.drawText(unit, centerX, centerY + (valuePaint.textSize / 3) + unitPaint.textSize + 10f, unitPaint)
    }

    fun setValue(newValue: Float, animated: Boolean = true) {
        targetValue = newValue.coerceIn(minValue, maxValue)
        
        if (!animated) {
            value = targetValue
            invalidate()
            return
        }

        animator?.cancel()
        animator = ValueAnimator.ofFloat(value, targetValue).apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                value = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun setRange(min: Float, max: Float) {
        minValue = min
        maxValue = max
        invalidate()
    }

    fun setUnit(u: String) {
        unit = u
        invalidate()
    }
    
    fun setGaugeColor(color: Int) {
        normalColor = color
        invalidate()
    }
    
    fun setWarningColor(color: Int) {
        warningColor = color
        invalidate()
    }

    fun setWarningThreshold(threshold: Float, isAbove: Boolean = true) {
        warningThreshold = threshold
        warningIsAbove = isAbove
        invalidate()
    }
}
