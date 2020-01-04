package com.ruler

import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.ReactApplication
import com.facebook.react.ReactRootView
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView


class MainActivity : ReactActivity() {
    var activityResultListener : ActivityResultListener? = null

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    override fun getMainComponentName(): String? {
        return "ruler"
    }

    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return object : ReactActivityDelegate(this, mainComponentName) {
            override fun createRootView(): ReactRootView {
                return RNGestureHandlerEnabledRootView(this@MainActivity)
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //createPoint()
        //createOverlay()
    }

    fun createPoint() {
        val mReactRootView = ReactRootView(this)
        val reactApplication : ReactApplication = application as ReactApplication
        val mReactInstanceManager = reactApplication.reactNativeHost.reactInstanceManager
        mReactRootView.startReactApplication(mReactInstanceManager, "Point", null)

        val wm = getWindowManager() as WindowManager

        val type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams()
        params.type = type
        params.format = PixelFormat.TRANSLUCENT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FIRST_SUB_WINDOW
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = -150
        params.y = 80

        wm.addView(mReactRootView, params)
    }

    fun createOverlay() {
        val mReactRootView = ReactRootView(this)
        val reactApplication : ReactApplication = application as ReactApplication
        val mReactInstanceManager = reactApplication.reactNativeHost.reactInstanceManager
        mReactRootView.startReactApplication(mReactInstanceManager, "Overlay", null)

        val wm = getWindowManager() as WindowManager

        val type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams()
        params.type = type
        params.format = PixelFormat.TRANSLUCENT
        //params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FIRST_SUB_WINDOW
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = -150
        params.y = 80

        mReactRootView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewRemoved(p0: View?, p1: View?) {}

            override fun onChildViewAdded(parent: View, child: View) {
                child.setOnTouchListener(object : View.OnTouchListener {
                    private var initialX: Int = 0
                    private var initialY: Int = 0
                    private var initialTouchX: Float = 0.toFloat()
                    private var initialTouchY: Float = 0.toFloat()

                    override fun onTouch(v: View, event: MotionEvent): Boolean {
                        Log.d("LEROY", "TESTING TOUCH happened!")
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {

                                //remember the initial position.
                                initialX = params.x
                                initialY = params.y

                                //get the touch location
                                initialTouchX = event.rawX
                                initialTouchY = event.rawY
                                return true
                            }
                            MotionEvent.ACTION_UP -> {
                                return true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                //Calculate the X and Y coordinates of the view.
                                params.x = initialX + (event.rawX - initialTouchX).toInt()
                                params.y = initialY + (event.rawY - initialTouchY).toInt()

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        activityResultListener?.onResult(resultCode, data)
    }

    interface ActivityResultListener {
        fun onResult(resultCode: Int, data: Intent?)
    }
}

