package nz.nix.pixelmeasure

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import com.github.anastr.rulerview.RulerUnit
import android.graphics.DashPathEffect
import android.view.*
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter


/**
 * Created by Anas Altair on 8/8/2018.
 * Modified by Leroy Hopson on 13/01/2020.
 */
class RulerView : View {
    private val colorPaintMask = Paint(Paint.ANTI_ALIAS_FLAG)
    private val grayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val grayPaintReplace: Paint
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val textPaintReplace: TextPaint
    private val bluePaint = Paint()
    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val greenPaint = Paint() // Don't use ANTI_ALIAS_FLAG for hairline strokes (i.e. 1px wide)
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val cursorWidth = 75
    private val cursorHeight = 75

    var flipped = false
        set(value) {
            field = value
            //val event = Arguments.createMap()
            //event.putBoolean("flipped", value)

            //val reactContext = context as ReactContext
            //reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
            //        id,
            //        "flipped",
            //        event)
            invalidate()
        }

    var cursorY = 0
        set(value) {
            field = value
            calculateMeasurement()
            invalidate()
        }

    var cursorX = 0
        set(value) {
            field = value
            calculateMeasurement()
            invalidate()
        }

    private var moveCursor = false

    var pixelMeasurement : Int = 0
        set(value) {
            if ((value - 1) < 0) {
                field = 0
                dpMeasurement = 0f
            } else {
                field = value - 1
                dpMeasurement = (value - 1) /  context.resources.displayMetrics.density
            }
            invalidate()
        }

    var dpMeasurement : Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var lockRuler = false

    var mOrientation = Configuration.ORIENTATION_PORTRAIT
        set(value) {
            field = value
            zeroY = width / 2
            invalidate()
        }

    var zeroY = height / 2
        set(value) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                when {
                    value < 0 -> {
                        field = -1
                    }
                    value > height -> {
                        field = height + 1
                    }
                    else -> {
                        field = value
                    }
                }
            } else {
                when {
                    value < 0 -> {
                        field = -1
                    }
                    value > width -> {
                        field = width + 1
                    }
                    else -> {
                        field = value
                    }
                }
            }
            calculateMeasurement()
            invalidate()
        }

    var rulerX = 0f

    var markCmWidth = dpTOpx(20f)
        set(value) {
            field = value
            invalidate()
        }
    var markHalfCmWidth = dpTOpx(15f)
        set(value) {
            field = value
            invalidate()
        }
    var markMmWidth = dpTOpx(10f)
        set(value) {
            field = value
            invalidate()
        }

    private var pointerY = 0f

    var unit: RulerUnit = RulerUnit.MM
        set(value) {
            field = value
            invalidate()
        }

    var coefficient = 1f
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        colorPaintMask.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        grayPaint.color = Color.DKGRAY
        grayPaintReplace = Paint(grayPaint)
        grayPaintReplace.color = Color.WHITE
        grayPaintReplace.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)

        bluePaint.color = Color.BLACK
        bluePaint.strokeWidth = 0f

        greenPaint.color = Color.GREEN
        greenPaint.strokeWidth = 0f

        redPaint.color = Color.RED
        redPaint.strokeWidth = 1f
        redPaint.setStyle(Paint.Style.STROKE)
        redPaint.setPathEffect(DashPathEffect(floatArrayOf(3f, 6f), 0f))

        textPaint.textSize = 20f
        textPaint.color = Color.BLACK
        textPaint.textAlign = Paint.Align.CENTER
        textPaintReplace = TextPaint(textPaint)
        textPaintReplace.color = Color.WHITE
        textPaintReplace.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)

        cursorPaint.color = Color.BLACK
        cursorPaint.style = Paint.Style.STROKE
        cursorPaint.strokeWidth = 2f

        cursorFillPaint.color = Color.BLACK
        cursorFillPaint.alpha = 33

        backgroundPaint.color = Color.WHITE
        backgroundPaint.alpha = 0

        mOrientation = Configuration.ORIENTATION_PORTRAIT

        ((context as ReactContext).currentActivity!!.application as MainApplication).rotateHandler = MainApplication.RotateHandler { ori ->
            Log.d("LEROYROTATE", "ROTATE CHANGED RulerView")

            val event = Arguments.createMap()
            event.putString("newOrientation", "${ori}")

            val reactContext = context as ReactContext
            reactContext.getJSModule(RCTEventEmitter::class.java).receiveEvent(
                    id,
                    "orientationChanged",
                    event)
        }
    }

    fun calculateMeasurement() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            pixelMeasurement = Math.abs(zeroY - cursorY)
        } else {
            pixelMeasurement = Math.abs(zeroY - cursorX)
        }
    }

    fun setOrientation(orientation: Int) {
        mOrientation = orientation
    }

    fun setPaintColor(colorString: String) {
        textPaint.color = Color.parseColor(colorString);
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (flipped) {
            canvas?.save()

            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                canvas?.scale(-1f, 1f, width / 2f, height / 2f)
            } else {
                canvas?.scale(1f, -1f, width / 2f, height / 2f)
            }
        }

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            canvas?.save()
            canvas?.rotate(90f, width / 2f, height / 2f)
            canvas?.restore()
        }

        val size = Point()
        display.getRealSize(size)

        drawMarks(canvas, bluePaint, textPaint)

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            canvas?.drawRect(0f, 0f, size.x.toFloat(), 150f, backgroundPaint)
        } else {
            canvas?.drawRect(0f, 0f, 150f, size.y.toFloat(), backgroundPaint)
        }

        // Cursor
        drawCursor(canvas, cursorPaint)

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            canvas?.drawLine(width.toFloat() / 2, zeroY.toFloat(), width.toFloat() / 2, cursorY.toFloat(), redPaint)
        } else {
            canvas?.drawLine(zeroY.toFloat(), height.toFloat() / 2, cursorX.toFloat(), height.toFloat() / 2, redPaint)
        }

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            canvas?.drawLine(left.toFloat(), cursorY.toFloat(), right.toFloat(), cursorY.toFloat(), greenPaint)
        } else {
            canvas?.drawLine(cursorX.toFloat(), top.toFloat(), cursorX.toFloat(), bottom.toFloat(), greenPaint)
        }

        drawMesaurement(canvas)

        if (flipped) {
            canvas?.restore()
        }
    }

    private fun drawMesaurement(canvas: Canvas?) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

        textPaint.setColor(Color.parseColor("#7df22f"));
        textPaint.setTextSize(20f);
        textPaint.setStyle(Paint.Style.FILL);

        outlinePaint.setTextSize(20f)
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.strokeWidth = 2f
        outlinePaint.setStyle(Paint.Style.STROKE)

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            var px = right.toFloat() - textPaint.textSize * 2
            var py = top.toFloat() + textPaint.textSize

            canvas?.save()
            canvas?.rotate(90f, px, py)
            if (flipped) canvas?.scale(-1f, 1f, px + (textPaint.textSize * 1.5f), py + textPaint.textSize)

            canvas?.drawText("${pixelMeasurement}px", px, py, outlinePaint)
            canvas?.drawText("${pixelMeasurement}px", px, py, textPaint)

            canvas?.drawText("${dpMeasurement}dp", px, py + (textPaint.textSize * 1.5f), outlinePaint)
            canvas?.drawText("${dpMeasurement}dp", px, py + (textPaint.textSize * 1.5f), textPaint)
            canvas?.restore()

            py = bottom.toFloat() - textPaint.textSize * 6

            canvas?.save()
            canvas?.rotate(90f, px, py)
            if (flipped) canvas?.scale(-1f, 1f, px + (textPaint.textSize * 1.5f), py + textPaint.textSize)

            canvas?.drawText("${pixelMeasurement}px", px, py, outlinePaint)
            canvas?.drawText("${pixelMeasurement}px", px, py, textPaint)

            canvas?.drawText("${dpMeasurement}dp", px, py + (textPaint.textSize * 1.5f), outlinePaint)
            canvas?.drawText("${dpMeasurement}dp", px, py + (textPaint.textSize * 1.5f), textPaint)

            canvas?.restore()
        } else {
            var px = 0f
            var py = 0f

            // LEFT
            canvas?.save()

            px = textPaint.textSize
            py = bottom.toFloat() - (textPaint.textSize * 2.5f)

            if (flipped) canvas?.scale(1f, -1f, 0f, py + (textPaint.textSize / 2))

            canvas?.drawText("${pixelMeasurement}px", px, py, outlinePaint)
            canvas?.drawText("${pixelMeasurement}px", px, py, textPaint)

            canvas?.drawText("${dpMeasurement}dp", textPaint.textSize, bottom.toFloat() - textPaint.textSize, outlinePaint)
            canvas?.drawText("${dpMeasurement}dp", textPaint.textSize, bottom.toFloat() - textPaint.textSize, textPaint)

            // RIGHT
            canvas?.drawText("${pixelMeasurement}px", right.toFloat() - textPaint.textSize * 4, bottom.toFloat() - (textPaint.textSize * 2.5f), outlinePaint)
            canvas?.drawText("${pixelMeasurement}px", right.toFloat() - textPaint.textSize * 4, bottom.toFloat() - (textPaint.textSize * 2.5f), textPaint)

            canvas?.drawText("${dpMeasurement}dp", right.toFloat() - textPaint.textSize * 4, bottom.toFloat() - textPaint.textSize, outlinePaint)
            canvas?.drawText("${dpMeasurement}dp", right.toFloat() - textPaint.textSize * 4, bottom.toFloat() - textPaint.textSize, textPaint)

            canvas?.restore()
        }
    }

    private fun drawCursor(canvas: Canvas?, paint: Paint) {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            canvas?.drawCircle(width / 2f, cursorY.toFloat(), cursorWidth / 2f, cursorPaint)
        } else {
            canvas?.drawCircle(cursorX.toFloat(), height / 2f, cursorWidth / 2f, cursorPaint)
        }
    }

    private fun drawMarks(canvas: Canvas?, paint: Paint, textPaint: Paint) {
        val max = if (mOrientation == Configuration.ORIENTATION_PORTRAIT) height else width

        for (i in -zeroY..max - zeroY) {
            val f = i.toFloat() + zeroY

            when {
                i == 0 -> {
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        canvas?.drawLine(left.toFloat(), f, (right.toFloat() / 2), f, paint)
                        canvas?.drawText("0", (right.toFloat() / 2) + textPaint.textSize, f, textPaint)
                    } else {
                        canvas?.drawLine(f, top.toFloat(), f, (bottom.toFloat() / 2), paint)
                        canvas?.drawText("0", f, (bottom.toFloat() / 2) + textPaint.textSize, textPaint)
                    }
                }
                i%100 == 0 -> {
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        canvas?.drawLine(left.toFloat(), f, right.toFloat() / 3, f, paint)

                        canvas?.save()
                        if (flipped) canvas?.scale(-1f, 1f, (right.toFloat() / 3) + textPaint.textSize, 0f)

                        canvas?.drawText("${i}", (right.toFloat() / 3) + textPaint.textSize, f, textPaint)

                        canvas?.restore()
                    } else {
                        canvas?.drawLine(f, top.toFloat(), f, bottom.toFloat() / 3, paint)

                        canvas?.save()
                        if (flipped) canvas?.scale(1f, -1f, 0f, (bottom.toFloat() / 3) + (textPaint.textSize / 2))

                        canvas?.drawText("${i}", f, (bottom.toFloat() / 3) + textPaint.textSize, textPaint)

                        canvas?.restore()
                    }
                }
                i%50 == 0 -> {
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        canvas?.drawLine(left.toFloat(), f, right.toFloat() / 4, f, paint)
                    } else {
                        canvas?.drawLine(f, top.toFloat(), f, bottom.toFloat() / 4, paint)
                    }
                }
                i%10 == 0 -> {
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        canvas?.drawLine(left.toFloat(), f, right.toFloat() / 8, f, paint)
                    } else {
                        canvas?.drawLine(f, top.toFloat(), f, bottom.toFloat() / 8, paint)
                    }
                }
                i%2 == 0 -> {
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        canvas?.drawLine(left.toFloat(), f, right.toFloat() / 16, f, paint)
                    } else {
                        canvas?.drawLine(f, top.toFloat(), f, bottom.toFloat() / 16, paint)
                    }
                }
                else -> {
                    // Do nothing?
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pointerY = event.y

                    if (event.y >= cursorY - (cursorWidth / 2) && event.y <= cursorY + (cursorWidth / 2) && event.x >= (width / 2) - cursorWidth / 2 && event.x <= (width / 2) + cursorWidth / 2) {
                        moveCursor = true
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = event.y - pointerY

                    if ((zeroY > -1 && zeroY < height + 1) || moveCursor) {
                        cursorY += dy.toInt()
                        // clamp
                        cursorY = Math.max(-1, Math.min(height + 1, cursorY))
                    }

                    if (!moveCursor && !lockRuler) {
                        zeroY += dy.toInt()
                        // clamp
                        zeroY = Math.max(-1, Math.min(height + 1, zeroY))
                    }

                    pointerY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    moveCursor = false
                    return false
                }
                MotionEvent.ACTION_CANCEL -> {
                    return false
                }
            }
        } else {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    pointerY = event.x

                    if (event.x >= cursorX - (cursorWidth / 2) && event.x <= cursorX + (cursorWidth / 2) && event.y >= (height / 2) - cursorWidth && event.y <= (height / 2) + cursorWidth / 2) {
                        moveCursor = true
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - pointerY

                    if ((zeroY > -1 && zeroY < width + 1) || moveCursor) {
                        cursorX += dx.toInt()
                        // clamp
                        cursorX = Math.max(-1, Math.min(width + 1, cursorX))
                    }

                    if (!moveCursor && !lockRuler) {
                        zeroY += dx.toInt()
                        // clamp
                        zeroY = Math.max(-1, Math.min(width + 1, zeroY))
                    }

                    pointerY = event.x
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    moveCursor = false
                    return false
                }
                MotionEvent.ACTION_CANCEL -> {
                    return false
                }
            }
        }
        return false
    }

    fun getDistance () = unit.convert(RulerUnit.pxToIn(rulerX, coefficient, resources.displayMetrics))

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putFloat("rulerX", rulerX)
        bundle.putFloat("coefficient", coefficient)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var _state = state
        val bundle = _state as Bundle
        rulerX = bundle.getFloat("rulerX")
        coefficient = bundle.getFloat("coefficient")
        _state = bundle.getParcelable("superState")
        super.onRestoreInstanceState(_state)
    }

    /**
     * convert dp to **pixel**.
     * @param dp to convert.
     * @return Dimension in pixel.
     */
    private fun dpTOpx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    /**
     * convert pixel to **dp**.
     * @param px to convert.
     * @return Dimension in dp.
     */
    fun pxTOdp(px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
}
