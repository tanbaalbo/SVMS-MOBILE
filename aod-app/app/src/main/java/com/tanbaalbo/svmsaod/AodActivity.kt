package com.tanbaalbo.svmsaod

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/** 화면을 켜 둔 채 AOD를 전체화면으로 띄운다. 두 번 탭하면 닫힌다. */
class AodActivity : AppCompatActivity() {

    private lateinit var screen: AodScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) setShowWhenLocked(true)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        screen = AodScreen(this) { finish() }
        setContentView(screen.root)
    }

    override fun onStart() {
        super.onStart()
        screen.start()
        window.attributes = window.attributes.apply { screenBrightness = AodConfig.load(this@AodActivity).brightness }
    }

    override fun onStop() {
        screen.stop()
        super.onStop()
    }

    override fun onDestroy() {
        screen.destroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screen.onConfigurationChanged()
    }
}
