package com.ruler

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager
import java.util.*
import kotlin.collections.ArrayList

class RulerPackage : ReactPackage {
    private val rulerViewManager = RulerViewManager()

    override fun createViewManagers(
            reactContext: ReactApplicationContext): List<ViewManager<*,*>> {
        return Arrays.asList(
                rulerViewManager
        )
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules = ArrayList<NativeModule>()

        modules.add(RulerModule(reactContext, rulerViewManager))

        return modules
    }
}
