package com.ignacio.simpleperceptronkotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.withStyledAttributes

class PerceptronGuessView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), PerceptronTrainer.GuessDrawer {
    private var negativeGuessColor = 0
    private var positiveGuessColor = 0
    private var extraBackgroundColor = 0
    private var lineColor = 0
    private var radius = 0f

    private val negativePaint: Paint
    private val positivePaint: Paint
    private val linePaint: Paint

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private lateinit var perceptronTrainer: PerceptronTrainer


    init {
        context.withStyledAttributes(attrs, R.styleable.PerceptronGuessView) {
            negativeGuessColor = getColor(R.styleable.PerceptronGuessView_negativeGuess, 0)
            positiveGuessColor = getColor(R.styleable.PerceptronGuessView_positiveGuess, 0)
            extraBackgroundColor = getColor(R.styleable.PerceptronGuessView_extraBackgroundColor, 0)
            lineColor = getColor(R.styleable.PerceptronGuessView_lineColor, 0)
        }

        negativePaint = Paint().apply {
            color = negativeGuessColor
            isAntiAlias = true
        }

        positivePaint = Paint().apply {
            color = positiveGuessColor
            isAntiAlias = true
        }

        linePaint = Paint().apply {
            color = lineColor
            isAntiAlias = true
            strokeWidth = 12f
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        radius = (kotlin.math.min(width, height) / 100).toFloat()
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(extraBackgroundColor)

        initializeTraining(width, height)
        initializeGraphic()
        perceptronTrainer.train()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    fun initializeTraining(width: Int, height: Int) {
        perceptronTrainer = PerceptronTrainer(
            GraphicSize(width, height), this
        )
        perceptronTrainer.initialize(2000) { x -> x-5}
    }

    fun initializeGraphic() {
        perceptronTrainer.trainingSet[0]
        extraCanvas.drawPoints(perceptronTrainer.getOriginalPoints(), linePaint)
    }

    override fun draw(inputs: FloatArray, guess: Int) {
        extraCanvas.drawCircle(inputs[0], inputs[1], radius, if(guess < 0) negativePaint else positivePaint)
        invalidate()
    }

    fun close() {
        perceptronTrainer.close()
    }
}