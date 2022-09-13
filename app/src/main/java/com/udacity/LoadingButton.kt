package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private val attebrs = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton)
    private val backgroundColorOnStart = attebrs.getColor(
        R.styleable.LoadingButton_backgroundColor,
        context.getColor(R.color.colorPrimary)
    )
    private val bgColorOnLoading = attebrs.getColor(
        R.styleable.LoadingButton_backgroundColor,
        context.getColor(R.color.colorPrimaryDark)
    )
    private val circuleColor = attebrs.getColor(
        R.styleable.LoadingButton_circleColor,
        context.getColor(R.color.colorAccent)
    )
    private val textColor = attebrs.getColor(
        R.styleable.LoadingButton_textColor,
        context.getColor(R.color.white)
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        color = backgroundColorOnStart
    }
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
        color = circuleColor

    }
    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        color = textColor
    }


    private var txt = context.getString(R.string.button_text)

    private var sweepAngle = 0.0f
    private var completence = 0.0f
    private var progressRect = RectF()
    private var circleRect = RectF()
    private val txtRect = RectF()

    private val valueAnimator = ValueAnimator.ofFloat(0f, 100f).apply {
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateInterpolator()
        addUpdateListener {
            completence = animatedFraction
            sweepAngle = 360f * completence
            progressRect.right = widthSize * completence
            if (buttonState == ButtonState.Completed) {
                cancel()
            }
            super.invalidate()
        }
    }


    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                txt = context.getString(R.string.button_text)
            }
            ButtonState.Loading -> {
                txt = context.getString(R.string.button_loading)
                val txtRect2 = Rect()
                txtPaint.getTextBounds(txt, 0, txt.length, txtRect2)
                circleRect.set(
                    widthSize / 2f + txtRect.width() / 2f + txtRect.height() / 2f,
                    heightSize / 2f - txtRect.height() / 2f,
                    widthSize / 2f + txtRect.width() / 2f + txtRect.height() * 1.5f,
                    heightSize / 2f + txtRect.height() / 2f
                )
            }

            ButtonState.Completed -> {
                txt = context.getString(R.string.button_text)
                valueAnimator.end()
                valueAnimator.setCurrentFraction(0f)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h
        progressRect.bottom = heightSize.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColorOnStart)
        canvas.drawText(
            context.getString(R.string.button_text),
            (widthSize / 2).toFloat(),
            (heightSize / 2).toFloat(),
            txtPaint
        )
        if (buttonState == ButtonState.Loading) {
            canvas.drawColor(bgColorOnLoading)
            canvas.drawRect(progressRect, paint)
            canvas.drawArc(circleRect, 270f, sweepAngle, true, circlePaint)
            canvas.drawText(
                context.getString(R.string.button_loading),
                (widthSize / 2).toFloat(),
                (heightSize / 2).toFloat(),
                txtPaint
            )
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return buttonState != ButtonState.Loading
    }

    fun changeState(state: ButtonState) {
        buttonState = state
    }

    init {
        isClickable = true
    }
}