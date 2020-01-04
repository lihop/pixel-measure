package com.ruler

import android.content.res.Configuration
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewProps
import com.facebook.react.uimanager.annotations.ReactProp


class RulerViewManager : SimpleViewManager<RulerView>() {
    val REACT_CLASS = "RCTRulerView"

    lateinit var rulerViewInstance : RulerView

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun createViewInstance(context: ThemedReactContext): RulerView {
        rulerViewInstance = RulerView(context)
        return rulerViewInstance
    }

    @ReactProp(name = "src")
    fun setSrc(view : RulerView, sources : ReadableArray?) {
        //view.setSource(sources)
    }

    @ReactProp(name = "borderRadius", defaultFloat = 0f)
    override fun setBorderRadius(view : RulerView, borderRadius : Float) {
    }

    @ReactProp(name = ViewProps.RESIZE_MODE)
    fun setResizeMode(view : RulerView, resizeMode : String?) {
    }

    @ReactProp(name = "paintColor")
    fun setPaintColor(view: RulerView, colorString : String) {
        view.setPaintColor(colorString)
    }

    @ReactProp(name = "orientation", defaultInt = Configuration.ORIENTATION_PORTRAIT)
    fun setOrientation(view: RulerView, orientation: String) {
        if (orientation == "PORTRAIT") {
            view.mOrientation = Configuration.ORIENTATION_PORTRAIT
        } else if (orientation == "LANDSCAPE") {
            view.mOrientation = Configuration.ORIENTATION_LANDSCAPE
        }
    }

    @ReactProp(name = "lockRuler", defaultBoolean = false)
    fun setLockRuler(view: RulerView, locked: Boolean) {
        view.lockRuler = locked
    }

    override fun getExportedCustomBubblingEventTypeConstants() : Map<String, Any> {
        val builder : MapBuilder.Builder<String, Any> = MapBuilder.builder()
        builder.put("orientationChanged", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onOrientationChanged")))
        builder.put("flipped", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onFlipped")))

        invalidate()

        return builder.build()
    }
}