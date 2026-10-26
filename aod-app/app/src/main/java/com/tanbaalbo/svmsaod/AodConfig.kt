package com.tanbaalbo.svmsaod

import android.content.Context
import androidx.preference.PreferenceManager
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.roundToInt

/** 설정 화면에서 고른 값을 AOD가 쓰기 좋은 모양으로 모아 둔 것. */
data class AodConfig(
    val theme: AodTheme,
    val useShipClock: Boolean,
    val hour24: Boolean,
    val showSeconds: Boolean,
    val showBattery: Boolean,
    val brightness: Float,
    val pixelShift: Boolean,
    val shipName: String,
    val voyage: String,
    val shipOffset: ZoneOffset,
    val showUtc: Boolean,
    val myWatch: Watch?,
    val showWatch: Boolean,
    val nextPort: String,
    val eta: String,
    val memo: String,
    val svmsEnabled: Boolean,
    val svmsUrl: String,
    val svmsRefreshMinutes: Int,
    val svmsMobile: Boolean,
    val svmsDark: Boolean,
) {
    /** 큰 시계가 따르는 시간대 */
    val mainZone: ZoneId get() = if (useShipClock) shipOffset else ZoneId.systemDefault()

    companion object {
        fun load(context: Context): AodConfig {
            PreferenceManager.setDefaultValues(context, R.xml.preferences, false)
            val p = PreferenceManager.getDefaultSharedPreferences(context)
            fun str(key: String, def: String = "") = p.getString(key, def)?.trim().orEmpty()
            return AodConfig(
                theme = AodTheme.byId(str("theme", "tactical")),
                useShipClock = str("clock_source", "ship") == "ship",
                hour24 = p.getBoolean("hour24", true),
                showSeconds = p.getBoolean("show_seconds", false),
                showBattery = p.getBoolean("show_battery", true),
                brightness = p.getInt("brightness", 15).coerceIn(1, 100) / 100f,
                pixelShift = p.getBoolean("pixel_shift", true),
                shipName = str("ship_name"),
                voyage = str("voyage"),
                shipOffset = parseOffset(str("utc_offset", "+9")),
                showUtc = p.getBoolean("show_utc", true),
                myWatch = Watch.byId(str("my_watch", "none")),
                showWatch = p.getBoolean("show_watch", true),
                nextPort = str("next_port"),
                eta = str("eta"),
                memo = str("memo"),
                svmsEnabled = p.getBoolean("svms_enabled", false),
                svmsUrl = str("svms_url", "https://svms.sinokor.co.kr/").ifEmpty { "https://svms.sinokor.co.kr/" },
                svmsRefreshMinutes = str("svms_refresh", "10").toIntOrNull()?.coerceAtLeast(1) ?: 10,
                svmsMobile = p.getBoolean("svms_mobile", true),
                svmsDark = p.getBoolean("svms_dark", true),
            )
        }

        /** "+9", "-5", "+5.5", "+5:30" 같은 시차 글자를 UTC 오프셋으로 바꾼다. 잘못 쓰면 +9. */
        fun parseOffset(text: String): ZoneOffset {
            val t = text.replace(" ", "").removePrefix("UTC").removePrefix("utc")
            val minutes = runCatching {
                if (':' in t) {
                    val sign = if (t.startsWith("-")) -1 else 1
                    val (h, m) = t.trimStart('+', '-').split(':').map { it.toInt() }
                    sign * (h * 60 + m)
                } else {
                    (t.toDouble() * 60).roundToInt()
                }
            }.getOrNull() ?: (9 * 60)
            return ZoneOffset.ofTotalSeconds(minutes.coerceIn(-12 * 60, 14 * 60) * 60)
        }
    }
}

/** 4시간 3교대 당직. 시작 시각(0/4/8시)부터 4시간, 12시간 뒤에 한 번 더. */
enum class Watch(val id: String, val label: String, val startHour: Int) {
    SECOND("2O", "2/O", 0),
    CHIEF("CO", "C/O", 4),
    THIRD("3O", "3/O", 8);

    val hours: String get() = "%02d–%02d, %02d–%02d".format(startHour, startHour + 4, startHour + 12, startHour + 16)

    companion object {
        fun byId(id: String): Watch? = entries.firstOrNull { it.id == id }

        /** 그 시각(시)의 당직 */
        fun at(hour: Int): Watch = entries[(hour % 12) / 4]
    }
}
