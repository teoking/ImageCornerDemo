package com.teoking.imagecornerdemo

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.core.graphics.scale
import kotlin.system.measureTimeMillis

interface CornerResize {
    fun changeCorner(percent: Int)
}

/**
 * Draw view's round corner with the [Path]
 */
internal class ShaderRoundCornerImage(context: Context) : CornerResizeView(context) {

    private val tag = "ShaderRoundCornerImage"

    private val bitmap = BitmapFactory.decodeResource(resources, R.drawable.butterfly02)
    private lateinit var bounds: RectF
    private var radii: FloatArray = floatArrayOf(
        20f, 20f, 20f, 20f, 20f, 20f, 20f, 20f
    )

    private val paint = Paint()
    private val path = Path()

    private var viewWidth = 0
    private var viewHeight = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewWidth = w
        viewHeight = h

        // Update paint shader to a new BitmapShader with a scaled bitmap
        // by the changed width and height
        paint.shader = BitmapShader(
            bitmap.scale(viewWidth, viewHeight),
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )

        // Draw bounds is same with view's bounds
        bounds = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        val t = measureTimeMillis {
            drawPath(canvas)
        }
        Log.d(tag, "layerType=$layerType onDraw cost~$t")
    }

    // Draw the round corner path and the image
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun drawPath(canvas: Canvas) {
        path.reset()
        path.addRoundRect(bounds, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }

    override fun changeCorner(percent: Int) {
        val radius = width * percent / 100f
        radii.fill(radius)
        invalidate()
    }
}

private class ClipOutlineProvider(var radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(
            0, 0, view.width, view.height, radius
        )
    }
}

internal abstract class CornerResizeView(context: Context) : View(context), CornerResize

/**
 * Draw view's round corner with the [ViewOutlineProvider].
 */
internal class OutlineRoundCornerImage(context: Context) : CornerResizeView(context) {

    private val bitmap = BitmapFactory.decodeResource(resources, R.drawable.butterfly01)
    private val paint = Paint()
    private val imageMatrix = Matrix()
    private val clipOutlineProvider = ClipOutlineProvider(20f)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        outlineProvider = clipOutlineProvider
        clipToOutline = true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        val t = measureTimeMillis {
            drawBitmap(canvas)
        }
        Log.d("OutlineRoundCornerImage", "layerType=$layerType onDraw cost~$t")
    }

    private fun drawBitmap(canvas: Canvas) {
        imageMatrix.reset()
        imageMatrix.postScale(width / bitmap.width.toFloat(), height / bitmap.height.toFloat())
        canvas.drawBitmap(bitmap, imageMatrix, paint)
    }

    override fun changeCorner(percent: Int) {
        clipOutlineProvider.radius = width * percent / 100f
        invalidateOutline()
    }
}

/**
 * Draw view's round corner with the [CornerPathEffect].
 */
internal class CornerEffectCornerImage(context: Context) : CornerResizeView(context) {

    private var cornerEffect = CornerPathEffect(20f)
    private val bitmap = BitmapFactory.decodeResource(resources, R.drawable.butterfly03)
    private val drawable = ShapeDrawable()
    private val paint = Paint()
    private lateinit var shader: BitmapShader
    private lateinit var bitmapTmp: Bitmap
    private lateinit var canvasTmp: Canvas
    private val filter = PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private lateinit var bounds: RectF

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bounds = RectF(0f, 0f, w.toFloat(), h.toFloat())

        update(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        val t = measureTimeMillis {
            // Install the corner effect to the drawable's paint
            drawable.paint.pathEffect = cornerEffect
            // Set the bitmap shader to the drawable's paint
            drawable.paint.shader = shader
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)
        }
        Log.d("CornerEffectCornerImage", "layerType=$layerType onDraw cost~$t")
    }

    override fun changeCorner(percent: Int) {
        if (width == 0 || height == 0) {
            return
        }

        // Percent to radius value
        val radius = width * percent / 100f

        // Update effect with new radius
        cornerEffect = CornerPathEffect(radius)

        update(width, height)

        invalidate()
    }

    private fun update(w: Int, h: Int) {
        bitmapTmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        canvasTmp = Canvas(bitmapTmp)
        canvasTmp.drawFilter = filter
        canvasTmp.drawBitmap(bitmap, null, bounds, paint)

        shader = BitmapShader(bitmapTmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

}