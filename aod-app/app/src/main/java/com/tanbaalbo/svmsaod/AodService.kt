package com.tanbaalbo.svmsaod

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * 화면이 꺼지는 순간을 기다렸다가 AOD를 띄운다.
 * 앱이 꺼져 있어도 신호를 받으려면 알림이 붙은 포그라운드 서비스로 계속 떠 있어야 한다.
 */
class AodService : Service() {

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            if (i.action == Intent.ACTION_SCREEN_OFF) showAod()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val n = notification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, n)
        }
        registerReceiver(screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!AodConfig.load(this).autoAod) stopSelf()
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenOff) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showAod() {
        val config = AodConfig.load(this)
        if (!config.autoAod) { stopSelf(); return }
        // 다른 앱 위에 표시 권한이 있어야 뒤에서 화면을 띄울 수 있다
        if (!Settings.canDrawOverlays(this)) return
        if (AodActivity.shownRecently()) return
        val b = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (b != null) {
            val level = b.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = b.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val status = b.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
            if (AodScreen.lowBattery(config, percent, charging)) return
        }
        startActivity(Intent(this, AodActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "AOD 대기", NotificationManager.IMPORTANCE_MIN).apply { setShowBadge(false) }
            )
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_aod)
            .setContentTitle("AOD 대기 중")
            .setContentText("화면이 꺼지면 AOD를 띄웁니다.")
            .setContentIntent(open)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        private const val CHANNEL = "aod_wait"
        private const val NOTIFICATION_ID = 1

        /** 설정에 맞춰 서비스를 켜거나 끈다 */
        fun sync(context: Context) {
            val intent = Intent(context, AodService::class.java)
            if (AodConfig.load(context).autoAod) ContextCompat.startForegroundService(context, intent)
            else context.stopService(intent)
        }
    }
}

/** 재부팅하거나 앱을 업데이트한 뒤에도 자동 켜기가 이어지게 한다 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            AodService.sync(context)
        }
    }
}
