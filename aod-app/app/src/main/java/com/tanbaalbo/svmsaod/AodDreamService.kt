package com.tanbaalbo.svmsaod

import android.content.res.Configuration
import android.service.dreams.DreamService

/** 화면 보호기: 충전 중 화면이 꺼질 때 AOD를 띄운다. */
class AodDreamService : DreamService() {

    private var screen: AodScreen? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        isScreenBright = false
        // SVMS 화면을 만질 수 있도록 터치를 받는다. 두 번 탭하면 끝난다.
        isInteractive = true
        val s = AodScreen(this) { finish() }
        screen = s
        setContentView(s.root)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        screen?.start()
        window?.let { AodScreen.applyLowPower(it, AodConfig.load(this)) }
    }

    override fun onDreamingStopped() {
        screen?.stop()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        screen?.destroy()
        screen = null
        super.onDetachedFromWindow()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screen?.onConfigurationChanged()
    }
}
