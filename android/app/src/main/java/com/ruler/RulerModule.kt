package com.ruler

import android.app.Activity
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ReactRoot
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView
import android.content.Context.DISPLAY_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.R.string.no
import android.R.attr.name
import android.content.Context


class RulerModule internal constructor(context: ReactApplicationContext, rulerViewManager: RulerViewManager) : ReactContextBaseJavaModule(context) {
    private val rulerViewManager = rulerViewManager
    private var mReactRootView : RNGestureHandlerEnabledRootView? = null
    private var previousScreenOrientation : Int = 0
    private var mOrientation : String = "PORTRAIT"

    override fun getName(): String {
        return "RulerModule"
    }

    init {
        reactContext = context
    }

    companion object {
        private lateinit var reactContext: ReactApplicationContext
        private var running = false
    }

    @ReactMethod
    fun scrollTo(position : Int) {
        val rulerView = rulerViewManager.rulerViewInstance

        rulerView.zeroY = position

        if (mOrientation == "PORTRAIT") {
            rulerView.cursorY = position
        } else {
            rulerView.cursorX = position
        }
    }

    @ReactMethod
    fun oneCursorPixelUp() {
        val rulerView = rulerViewManager.rulerViewInstance

        if (mOrientation == "PORTRAIT") {
            rulerView.cursorY = rulerView.cursorY - 1
        } else {
            rulerView.cursorX = rulerView.cursorX - 1
        }
    }

    @ReactMethod
    fun oneCursorPixelDown() {
        val rulerView = rulerViewManager.rulerViewInstance

        if (mOrientation == "PORTRAIT") {
            rulerView.cursorY = rulerView.cursorY + 1
        } else {
            rulerView.cursorX = rulerView.cursorX + 1
        }
    }

    @ReactMethod
    fun flip() {
        val rulerView = rulerViewManager.rulerViewInstance
        rulerView.flipped = !rulerView.flipped
    }

    @ReactMethod
    fun setFlipped(flipped: Boolean) {
        val rulerView = rulerViewManager.rulerViewInstance
        rulerView.flipped = flipped
    }

    @ReactMethod
    fun onePixelUp() {
        val rulerView = rulerViewManager.rulerViewInstance
        rulerView.zeroY = rulerView.zeroY - 1
    }

    @ReactMethod
    fun onePixelDown() {
        val rulerView = rulerViewManager.rulerViewInstance
        rulerView.zeroY = rulerView.zeroY + 1
    }

    @ReactMethod
    fun restart(orientation: String) {
        if (!running) {
            return
        }

        launch(orientation, false)
    }

    @ReactMethod
    fun destroy() {
        val wm = reactContext.currentActivity!!.getWindowManager() as WindowManager

        wm.removeView(mReactRootView)
        mReactRootView = null

        running = false

    }

    @ReactMethod
    fun launch(orientation : String, initial: Boolean = false) {
        mOrientation = orientation

//        val isActivityInForeground = (reactContext.currentActivity!! as AppCompatActivity).lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        val wm = reactContext.currentActivity!!.getWindowManager() as WindowManager

        val screenOrientation = wm.defaultDisplay.orientation

        if (running && initial) {
            return
        }

        if (running) {
            wm.removeView(mReactRootView)
        }

        val size = Point()
        wm.defaultDisplay.getRealSize(size)

        mReactRootView = RNGestureHandlerEnabledRootView(reactContext.currentActivity)

            val reactApplication : ReactApplication = reactContext.currentActivity!!.application as ReactApplication
            val mReactInstanceManager = reactApplication.reactNativeHost.reactInstanceManager
            reactContext.currentActivity

            val displayMetrics = DisplayMetrics()
            reactContext.currentActivity!!.windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            val height = displayMetrics.heightPixels / displayMetrics.density
            val width = displayMetrics.widthPixels / displayMetrics.density

            val initialProps = Bundle()
            initialProps.putInt("screenHeight", size.y)
            initialProps.putInt("screenWidth", size.x)
            initialProps.putString("orientation", orientation)
            initialProps.putInt("screenOrientation", screenOrientation)

            Handler(Looper.getMainLooper()).post(Runnable {
                mReactRootView?.startReactApplication(mReactInstanceManager, "Ruler", initialProps)


            val type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            val params = WindowManager.LayoutParams()
            params.type = type
            params.format = PixelFormat.TRANSLUCENT
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FIRST_SUB_WINDOW

            val size = Point()
            wm.defaultDisplay.getRealSize(size)

            //if (orientation == "PORTRAIT") {
            //    params.width = WindowManager.LayoutParams.WRAP_CONTENT
            //    params.height = size.y
            //} else {
            //    params.width = size.x
            //    params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //}

            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT

            val rectangle = Rect();
            val window = reactContext.currentActivity!!.getWindow() as Window
            window.getDecorView().getWindowVisibleDisplayFrame(rectangle)
            val statusBarHeight = rectangle.top
            val contentViewTop = window.findViewById<View>(Window.ID_ANDROID_CONTENT).getTop()
            val titleBarHeight = contentViewTop - statusBarHeight;

                Log.d("LEROY", "orientation: ${orientation}, screenOrientation: ${screenOrientation}, previousScreenOrientation: ${previousScreenOrientation}")

                val screenOrientationString = if (screenOrientation == 0 || screenOrientation == 2) "PORTRAIT" else "LANDSCAPE"

                params.x = 0
                params.y = 0

                when(screenOrientationString) {
                    "PORTRAIT" -> {
                        if (orientation == "PORTRAIT") {
                            params.x = 0
                            params.y = -titleBarHeight - 2
                        }
                    }
                    "LANDSCAPE" -> {
                        if (orientation == "LANDSCAPE") {
                            if (screenOrientation == 1) {
                                // Move left.
                                params.x = -titleBarHeight - 2
                            } else {
                                // Move right.
                                params.x = (-titleBarHeight - 2) * 2
                            }
                        }
                    }
                }

                val resources = reactContext.getResources()
                val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
                val navigationBarHeight = if (resourceId > 0) {
                    resources.getDimensionPixelSize(resourceId)
                } else 0

                if (screenOrientation == 3 && orientation == "LANDSCAPE") {
                    params.x = (-navigationBarHeight + -titleBarHeight - 2)
                }

                if (screenOrientation == 3 && orientation == "LANDSCAPE" && previousScreenOrientation == 3) {
                    params.x = 0
                }

                previousScreenOrientation = screenOrientation

                mReactRootView?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewRemoved(parent: View, child: View) {}

                    override fun onChildViewAdded(parent: View, child: View) {
                        child.setOnTouchListener(object : View.OnTouchListener {
                            private var initialX: Int = -displayMetrics.widthPixels / 2
                            private var initialY: Int = -displayMetrics.heightPixels / 2
                            private var initialTouchX: Float = 0.toFloat()
                            private var initialTouchY: Float = 0.toFloat()

                            override fun onTouch(v: View, event: MotionEvent): Boolean {
                                val c = child as ViewGroup
                                for (i in 0..c.childCount) {
                                    val grandChild = c.getChildAt(i)
                                    if (grandChild != null) {
                                        grandChild.dispatchTouchEvent(event)
                                    }
                                }

                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {

                                        //remember the initial position.
                                        initialX = params.x
                                        initialY = params.y

                                        //get the touch location
                                        initialTouchX = event.rawX
                                        initialTouchY = event.rawY
                                    }
                                    MotionEvent.ACTION_UP -> {
                                        return true
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        //Calculate the X and Y coordinates of the view.
                                        if (orientation == "PORTRAIT") {
                                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                                        }

                                        if (orientation == "LANDSCAPE") {
                                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                                        }

                                        //Update the layout with new X & Y coordinate
                                        wm.updateViewLayout(mReactRootView, params)
                                        return true
                                    }
                                }
                                return false
                            }
                        })
                    }
                })

                wm.addView(mReactRootView, params)
            })

            running = true
        }
    }
