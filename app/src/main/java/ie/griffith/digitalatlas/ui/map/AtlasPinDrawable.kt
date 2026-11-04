package ie.griffith.digitalatlas.ui.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

//custom map pin drawn with shapes instead of a png, so it stays sharp on any screen density,
//can be recoloured to match the theme, and doesn't add an image file to the app
class AtlasPinDrawable(
    @ColorInt fillColor: Int,
    @ColorInt dotColor: Int,
    density: Float
) : Drawable() {

    //28x40dp pin, converted into pixels for this screen
    private val widthPx = (28 * density).toInt()
    private val heightPx = (40 * density).toInt()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fillColor }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dotColor }
    //see-through black for a small shadow under the tip so the pin doesn't look flat
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(70, 0, 0, 0) }
    //reusing one path object instead of making a new one on every draw
    private val tipPath = Path()

    override fun getIntrinsicWidth() = widthPx
    override fun getIntrinsicHeight() = heightPx

    override fun draw(canvas: Canvas) {
        val radius = bounds.width() / 2f
        val centerX = bounds.left + radius
        val centerY = bounds.top + radius
        val tipY = bounds.bottom - bounds.height() * 0.05f

        //shadow first so the pin is drawn on top of it
        canvas.drawOval(
            centerX - radius * 0.35f, tipY - radius * 0.12f,
            centerX + radius * 0.35f, tipY + radius * 0.12f,
            shadowPaint
        )

        //teardrop shape: a triangle pointing down to the tip, with a circle on top
        tipPath.reset()
        tipPath.moveTo(centerX - radius * 0.76f, centerY + radius * 0.65f)
        tipPath.lineTo(centerX, tipY)
        tipPath.lineTo(centerX + radius * 0.76f, centerY + radius * 0.65f)
        tipPath.close()
        canvas.drawPath(tipPath, fillPaint)
        canvas.drawCircle(centerX, centerY, radius, fillPaint)

        //white dot in the middle like a classic map pin
        canvas.drawCircle(centerX, centerY, radius * 0.38f, dotPaint)
    }

    //required by Drawable, passing alpha and colour filters on to the paints
    override fun setAlpha(alpha: Int) {
        fillPaint.alpha = alpha
        dotPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        dotPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}