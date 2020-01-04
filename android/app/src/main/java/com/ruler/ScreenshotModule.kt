package com.ruler

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService

import android.graphics.Bitmap
import android.media.Image.Plane
import com.facebook.react.bridge.*
import android.R.attr.bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.reflect.KTypeProjection


class ScreenshotModule internal constructor(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    override fun getName(): String {
        return "ScreenshotModule"
    }

    init {
        reactContext = context
    }

    companion object {
        private lateinit var reactContext: ReactApplicationContext

        private val DURATION_SHORT_KEY = "SHORT"
        private val DURATION_LONG_KEY = "LONG"
    }

    override fun getConstants(): Map<String, Any>? {
        val constants = HashMap<String, Any>()
        constants[DURATION_SHORT_KEY] = Toast.LENGTH_SHORT
        constants[DURATION_LONG_KEY] = Toast.LENGTH_LONG
        return constants
    }

    @ReactMethod
    fun show(message: String, duration: Int) {
        Toast.makeText(reactApplicationContext, message, duration).show()
    }

    var mProjection: MediaProjection? = null

    @ReactMethod
    fun captureScreen(promise: Promise) {
        val projectionManager = getSystemService(reactContext, MediaProjectionManager::class.java)

        fun capture() {
            val wm = reactContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.getDefaultDisplay()
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val size = Point()
            display.getRealSize(size)
            val mWidth = size.x
            val mHeight = size.y
            val mDensity = metrics.densityDpi

            val mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2)

            val handler = Handler()

            val flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC

            mProjection!!.createVirtualDisplay("screen-mirror", mWidth, mHeight, mDensity, flags, mImageReader.surface, null, handler)

            mImageReader.setOnImageAvailableListener({ reader ->
                reader.setOnImageAvailableListener(null, handler)

                val image = reader.acquireLatestImage()

                val planes = image.planes
                val buffer = planes[0].buffer

                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * metrics.widthPixels
                // create bitmap
                val bmp = Bitmap.createBitmap(metrics.widthPixels + (rowPadding.toFloat() / pixelStride.toFloat()).toInt(), metrics.heightPixels, Bitmap.Config.ARGB_8888)
                bmp.copyPixelsFromBuffer(buffer)

                image.close()
                reader.close()

                val realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.height)
                bmp.recycle()

                /* do something with [realSizeBitmap] */
                val byteArrayOutputStream = ByteArrayOutputStream()
                realSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)

                promise.resolve(encoded)
            }, handler)
        }

        if (mProjection == null) {
            val listener = object : MainActivity.ActivityResultListener {
                override fun onResult(resultCode: Int, data: Intent?) {
                    mProjection = projectionManager!!.getMediaProjection(resultCode, data)
                    capture()
                }
            }

            (reactContext.currentActivity as MainActivity).activityResultListener = listener

            startActivityForResult(reactContext!!.currentActivity!!, projectionManager!!.createScreenCaptureIntent(), 1, null)
        } else {
            capture()
        }
    }
}