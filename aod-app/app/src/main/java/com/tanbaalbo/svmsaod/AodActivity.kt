package com.tanbaalbo.svmsaod

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 화면을 켜 둔 채 AOD를 전체화면으로 띄운다. 두 번 탭하면 닫힌다.
 * 잠금 화면 위에도 뜨고, 자동 켜기로 열리면 꺼진 화면을 다시 켠다.
 */
class AodActivity : AppCompatActivity() {

    private lateinit var screen: AodScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
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
        visible = true
        screen.start()
        AodScreen.applyLowPower(window, AodConfig.load(this))
    }

    override fun onStop() {
        visible = false
        hiddenAt = SystemClock.elapsedRealtime()
        screen.stop()
        super.onStop()
        // 전원 버튼 등으로 가려지면 닫는다. 다음에 화면을 켜면 AOD가 아니라 잠금 화면이 나온다.
        if (!isChangingConfigurations) finish()
    }

    override fun onDestroy() {
        screen.destroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        screen.onConfigurationChanged()
    }

    companion object {
        @Volatile private var visible = false
        @Volatile private var hiddenAt = 0L

        /**
         * AOD가 지금 떠 있거나 방금 가려졌는지.
         * AOD를 보던 중 전원 버튼을 눌러 꺼진 것이면 다시 띄우지 않고 화면을 완전히 끈다.
         */
        fun shownRecently() = visible || SystemClock.elapsedRealtime() - hiddenAt < 2_000
    }
}
