package nz.nix.pixelmeasure

import android.os.Bundle
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import android.app.Activity
import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import com.facebook.react.ReactApplication

class OverlayActivity : Activity(), DefaultHardwareBackBtnHandler {
    private var mReactRootView: ReactRootView? = null
    private var mReactInstanceManager: ReactInstanceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mReactRootView = ReactRootView(this)
        val reactApplication : ReactApplication = application as ReactApplication
        mReactInstanceManager = reactApplication.reactNativeHost.reactInstanceManager
        mReactRootView!!.startReactApplication(mReactInstanceManager, "Overlay", null)

        val wm = getWindowManager() as WindowManager

        val type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams()
        params.type = type
        params.format = PixelFormat.TRANSLUCENT
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        //params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FIRST_SUB_WINDOW
        params.height = WindowManager.LayoutParams.WRAP_CONTENT
        params.width = WindowManager.LayoutParams.WRAP_CONTENT
        params.x = -150
        params.y = 80

        wm.addView(mReactRootView, params)
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }
}
