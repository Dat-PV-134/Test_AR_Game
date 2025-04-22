package com.rekoj134.argamedemo

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import java.io.IOException
import java.io.InputStream

class TransitionView : View {
    private val myPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    }
    private var backgroundBitmap: Bitmap? = null
    private var characterBitmap: Bitmap? = null

    private var isStartAnim = false
    private var radiusRipple = 1500f
    private var rippleX: Float = 0f
    private var rippleY: Float = 0f
    private var isReverse = true

    private var percentScale = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        backgroundBitmap = getImageBitmap(
            context,
            "transition_background.webp"
        )
        characterBitmap = getImageBitmap(
            context,
            "character.webp"
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isReverse) canvas.drawColor(Color.BLACK)
        drawBackground(canvas)
        drawCharacter(canvas)
    }

    var onEnd: (() -> Unit)? = null

    fun startAnimRipple(isReverse: Boolean) {
        this.rippleX = width / 2f
        this.rippleY = height / 2f
        isStartAnim = true
        this.isReverse = isReverse

        val animator =
            if (!isReverse) ValueAnimator.ofFloat(1f, 0f) else ValueAnimator.ofFloat(0f, 1f)
        val animatorScale =
            if (isReverse) ValueAnimator.ofFloat(1f, 0f) else ValueAnimator.ofFloat(0f, 1f)

        animatorScale.duration = 360
        animatorScale.interpolator = FastOutLinearInInterpolator()
        animatorScale.addUpdateListener { animation ->
            percentScale = animation.animatedValue as Float
            invalidate()
        }
        animatorScale.doOnEnd {
            if (isReverse) animator.start()
            invalidate()
        }

        animator.duration = 360
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener { animation ->
            radiusRipple = 1500f * animation.animatedValue as Float
            if(!isReverse && animatorScale.isRunning == false && (animation.animatedValue as Float) < 0.5f) {
                animatorScale.start()
            }
            invalidate()
        }
        animator.doOnEnd {
            radiusRipple = 0f
            isStartAnim = false
            onEnd?.invoke()
            invalidate()
        }


        if(!isReverse) animator.start()
        else animatorScale.start()
    }

    private fun drawCharacter(canvas: Canvas) {
        characterBitmap?.let { character ->
            canvas.drawBitmap(
                character,
                Rect(0, 0, character.width, character.height),
                RectF(
                    width / 2f - (width * 0.7f) * percentScale / 2f,
                    height / 2f - (width * 0.7f) * percentScale * character.height / character.width,
                    width / 2f + (width * 0.7f) * percentScale / 2f,
                    height / 2f + (width * 0.7f) * percentScale * character.height / character.width
                ),
                myPaint
            )
        }
    }

    private fun drawBackground(canvas: Canvas) {
        if (isStartAnim) {
            val path = Path()
            path.addCircle(rippleX, rippleY, radiusRipple, Path.Direction.CW)
            canvas.save()
            canvas.clipPath(path, Region.Op.DIFFERENCE)
            backgroundBitmap?.let { background ->
                val scale = maxOf(
                    width.toFloat() / background.width,
                    height.toFloat() / background.height
                )
                val scaledWidth = background.width * scale
                val scaledHeight = background.height * scale
                val left = (width - scaledWidth) / 2f
                val top = (height - scaledHeight) / 2f
                canvas.drawBitmap(
                    background,
                    Rect(0, 0, background.width, background.height),
                    RectF(left, top, left + scaledWidth, top + scaledHeight),
                    myPaint
                )
            }
            canvas.restore()
        } else {
            if (!isReverse) {
                backgroundBitmap?.let { background ->
                    val scale = maxOf(
                        width.toFloat() / background.width,
                        height.toFloat() / background.height
                    )
                    val scaledWidth = background.width * scale
                    val scaledHeight = background.height * scale
                    val left = (width - scaledWidth) / 2f
                    val top = (height - scaledHeight) / 2f
                    canvas.drawBitmap(
                        background,
                        Rect(0, 0, background.width, background.height),
                        RectF(left, top, left + scaledWidth, top + scaledHeight),
                        myPaint
                    )
                }
            }
        }
    }
}

fun getImageBitmap(context: Context, filePath: String): Bitmap? {
    val assetManager = context.assets
    val inputStream: InputStream
    var bitmap: Bitmap? = null
    try {
        inputStream = assetManager.open(filePath)
        bitmap = BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return bitmap
}