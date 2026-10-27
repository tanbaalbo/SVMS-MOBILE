package com.tanbaalbo.svmsaod

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlin.random.Random

/**
 * AOD 화면 전체(시계 + 선택적인 SVMS 패널)를 만들고 돌린다.
 * 전체화면 액티비티와 화면 보호기(DreamService)가 같이 쓴다.
 */
class AodScreen(private val context: Context, private val onExit: () -> Unit) {

    private val handler = Handler(Looper.getMainLooper())
    private var config = AodConfig.load(context)

    val root = FrameLayout(context).apply { setBackgroundColor(Color.BLACK) }
    private val content = LinearLayout(context)
    private val aod = AodView(context)
    private var web: WebView? = null

    private val tick = object : Runnable {
        override fun run() {
            aod.invalidate()
            val now = System.currentTimeMillis()
            val step = if (config.showSeconds) 1000L else 60_000L
            handler.postDelayed(this, step - now % step + 20)
        }
    }

    private val shift = object : Runnable {
        override fun run() {
            if (config.pixelShift) {
                val max = 12f * context.resources.displayMetrics.density
                content.translationX = Random.nextFloat() * 2 * max - max
                content.translationY = Random.nextFloat() * 2 * max - max
            }
            handler.postDelayed(this, 60_000L)
        }
    }

    private val refresh = object : Runnable {
        override fun run() {
            web?.reload()
            handler.postDelayed(this, config.svmsRefreshMinutes * 60_000L)
        }
    }

    private val battery = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = readBattery(i)
    }

    init {
        root.addView(content, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // 시계 영역을 두 번 탭하면 닫는다
        val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onDoubleTap(e: MotionEvent): Boolean { onExit(); return true }
        })
        aod.setOnTouchListener { _, e -> gestures.onTouchEvent(e) }
    }

    fun start() {
        config = AodConfig.load(context)
        aod.config = config
        buildLayout()
        context.registerReceiver(battery, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let(::readBattery)
        handler.post(tick)
        handler.post(shift)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        runCatching { context.unregisterReceiver(battery) }
        CookieManager.getInstance().flush()
    }

    fun destroy() {
        stop()
        content.removeAllViews()
        web?.destroy()
        web = null
    }

    /** 화면이 돌아가면 시계/SVMS 배치(좌우 ↔ 위아래)를 다시 잡는다 */
    fun onConfigurationChanged() = buildLayout()

    private fun buildLayout() {
        content.removeAllViews()
        val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        content.orientation = if (landscape) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL

        content.addView(aod, LinearLayout.LayoutParams(0, 0, 1f).fill(landscape))
        if (config.svmsEnabled) {
            val w = web ?: createWebView().also { web = it }
            content.addView(w, LinearLayout.LayoutParams(0, 0, 1f).fill(landscape))
            handler.removeCallbacks(refresh)
            if (config.svmsAutoRefresh) handler.postDelayed(refresh, config.svmsRefreshMinutes * 60_000L)
        } else {
            web?.destroy()
            web = null
        }
    }

    private fun LinearLayout.LayoutParams.fill(landscape: Boolean) = apply {
        if (landscape) height = ViewGroup.LayoutParams.MATCH_PARENT else width = ViewGroup.LayoutParams.MATCH_PARENT
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView = WebView(context).apply {
        setBackgroundColor(Color.BLACK)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        CookieManager.getInstance().setAcceptCookie(true)
        alpha = 0.85f // 시계보다 한 단계 흐리게
        // 잠금 화면 위에 떠 있을 때는 잠금을 풀지 않고 SVMS를 만질 수 없게 막는다
        val keyguard = context.getSystemService(KeyguardManager::class.java)
        setOnTouchListener { _, _ -> keyguard?.isKeyguardLocked == true }
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (config.svmsDark) view.evaluateJavascript(DARK_JS, null)
                if (config.svmsMobile && Uri.parse(url).host == SVMS_HOST) view.evaluateJavascript(MOBILE_JS, null)
            }
        }
        loadUrl(config.svmsUrl)
    }

    private fun readBattery(i: Intent) {
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        if (level >= 0 && scale > 0) aod.batteryPercent = level * 100 / scale
        aod.charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        aod.invalidate()
        if (lowBattery(config, aod.batteryPercent, aod.charging)) onExit()
    }

    companion object {
        const val SVMS_HOST = "svms.sinokor.co.kr"

        /** 충전 중이 아니고 배터리가 설정한 하한 이하이면 AOD를 띄우지 않는다 */
        fun lowBattery(c: AodConfig, percent: Int, charging: Boolean) =
            c.minBattery > 0 && !charging && percent in 0..c.minBattery

        /**
         * AOD 창의 밝기를 낮추고, 패널이 지원하는 가장 낮은 주사율을 요청한다.
         * 화면 내용은 1분에 한 번만 다시 그리지만, 패널은 내용이 그대로여도 주사율만큼
         * 계속 화면을 다시 내보내므로 120Hz → 60Hz 등으로 낮춰 두면 그만큼 덜 쓴다.
         * 어디까지 내려가는지는 패널이 알려 주는 모드에 달려 있다.
         */
        fun applyLowPower(window: Window, config: AodConfig) {
            @Suppress("DEPRECATION")
            val display = window.windowManager.defaultDisplay
            val current = display.mode
            val lowest = display.supportedModes
                .filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
                .minByOrNull { it.refreshRate }
            window.attributes = window.attributes.apply {
                screenBrightness = config.brightness
                if (lowest != null) {
                    preferredDisplayModeId = lowest.modeId
                    preferredRefreshRate = lowest.refreshRate
                }
            }
        }

        /** 흰 배경 페이지를 검게 뒤집는다 (사진·그림은 다시 뒤집어 원래 색) */
        private const val DARK_JS = """(function(d){if(d.getElementById('svms-aod-dark'))return;
var s=d.createElement('style');s.id='svms-aod-dark';
s.textContent='html{filter:invert(1) hue-rotate(180deg);background:#fff!important}img,video,canvas,svg image{filter:invert(1) hue-rotate(180deg)}';
d.head.appendChild(s)})(document)"""

        /** 북마크와 같은 svms-mobile.js를 불러와 좁은 화면 배치를 적용한다 (이미 적용됐으면 건너뜀) */
        private const val MOBILE_JS = """(function(d){if(window.__svmMobile||d.getElementById('svms-aod-loader'))return;
var s=d.createElement('script');s.id='svms-aod-loader';
s.src='https://tanbaalbo.github.io/SVMS-MOBILE/svms-mobile.js?t='+Date.now();d.head.appendChild(s)})(document)"""
    }
}
