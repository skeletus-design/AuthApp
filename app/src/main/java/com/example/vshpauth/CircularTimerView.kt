package com.example.vshpauth

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CircularTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint: Paint
    private val progressPaint: Paint
    private val arcBounds = RectF()
    private var radius = 0f
    private var progress = 0f

    // Кастомные атрибуты
    private var backgroundColor = Color.GRAY
    private var progressColor = Color.BLUE
    private var strokeWidth = 5f
    private var startAngle = -90f // Начинаем сверху (12 часов)
    private var clockwise = true

    init {
        // Получаем кастомные атрибуты
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularTimerView,
            0,
            0
        ).apply {
            try {
                backgroundColor = getColor(R.styleable.CircularTimerView_background_color, Color.GRAY)
                progressColor = getColor(R.styleable.CircularTimerView_progress_color, Color.BLUE)
                strokeWidth = getDimension(R.styleable.CircularTimerView_stroke_width, 5f)
                startAngle = getFloat(R.styleable.CircularTimerView_start_angle, -90f)
                clockwise = getBoolean(R.styleable.CircularTimerView_clockwise, true)
            } finally {
                recycle()
            }
        }

        // Настраиваем кисть для фона
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.STROKE
            strokeWidth = this@CircularTimerView.strokeWidth
            strokeCap = Paint.Cap.ROUND
        }

        // Настраиваем кисть для прогресса
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = this@CircularTimerView.strokeWidth
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Учитываем strokeWidth при расчете радиуса
        radius = (min(w, h) - strokeWidth) / 2f

        // Устанавливаем границы для дуги с учетом strokeWidth
        val padding = strokeWidth / 2f
        arcBounds.set(
            padding,
            padding,
            w - padding,
            h - padding
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Рисуем фоновый круг
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Рисуем прогресс
        val sweepAngle = progress * 360f * (if (clockwise) 1 else -1)
        canvas.drawArc(arcBounds, startAngle, sweepAngle, false, progressPaint)
    }

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    // Дополнительные методы для динамического изменения свойств
    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        backgroundPaint.strokeWidth = width
        progressPaint.strokeWidth = width
        requestLayout() // Необходимо пересчитать размеры
    }
}