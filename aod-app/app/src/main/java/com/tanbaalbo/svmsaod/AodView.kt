package com.tanbaalbo.svmsaod

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 시계·날짜·선박 정보·당직·배터리를 캔버스에 직접 그리는 AOD 본체.
 * 내용은 가운데에 세로로 쌓고, 화면 폭에 맞춰 글자 크기를 줄인다.
 */
class AodView(context: Context) : View(context) {

    var config: AodConfig = AodConfig.load(context)
        set(value) { field = value; invalidate() }

    var batteryPercent = -1
    var charging = false

    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 한 줄: 높이와 그리는 방법 (y = 그 줄의 위쪽) */
    private class Row(val height: Float, val draw: (Canvas, Float) -> Unit)

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        val c = config
        val t = c.theme
        val w = width.toFloat()
        val cx = w / 2
        val u = min(width, height) / 100f // 화면 크기에 비례하는 기본 단위
        val maxW = w * 0.92f

        val now = ZonedDateTime.now(c.mainZone)
        val ship = ZonedDateTime.now(c.shipOffset)
        val rows = ArrayList<Row>()

        // ── 위 라벨 ──
        val label = if (c.useShipClock) "SHIP TIME  UTC${offsetText(c.shipOffset)}" else "LOCAL TIME"
        rows += textRow(label, 3f * u, t.dim, t.typeface, maxW, cx, letterSpacing = 0.25f)

        // ── 큰 시계 ──
        val timeText = now.format(DateTimeFormatter.ofPattern(if (c.hour24) "HH:mm" else "h:mm"))
        val suffix = buildString {
            if (c.showSeconds) append(now.format(DateTimeFormatter.ofPattern(":ss")))
            if (!c.hour24) append(if (now.hour < 12) " AM" else " PM")
        }
        rows += clockRow(timeText, suffix, 24f * u, t, maxW, cx)

        // ── 날짜 ──
        val dow = now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        rows += textRow(now.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " ($dow)", 5f * u, t.sub, t.typeface, maxW, cx)

        // ── 테마 장식 ──
        rows += decorationRow(t, u, cx)

        // ── 다른 시간들 ──
        val clocks = ArrayList<String>()
        if (!c.useShipClock) clocks += "선박 " + ship.format(HM) + "  (UTC${offsetText(c.shipOffset)})"
        if (c.showUtc) clocks += "UTC " + ZonedDateTime.now(ZoneOffset.UTC).format(HM)
        if (clocks.isNotEmpty()) rows += textRow(clocks.joinToString("   ·   "), 4f * u, t.sub, t.typeface, maxW, cx)

        // ── 선박 ──
        val shipLine = listOfNotNull(
            c.shipName.ifEmpty { null },
            c.voyage.ifEmpty { null }?.let { "V.$it" },
        ).joinToString("  ·  ")
        if (shipLine.isNotEmpty()) rows += textRow(shipLine, 4.2f * u, t.main, t.typeface, maxW, cx)

        // ── 당직 ──
        if (c.showWatch) rows += watchRows(ship, c, u, cx, maxW)

        // ── 다음 항구 / ETA ──
        val port = listOfNotNull(
            c.nextPort.ifEmpty { null }?.let { "NEXT  $it" },
            c.eta.ifEmpty { null }?.let { "ETA  $it" },
        ).joinToString("   ·   ")
        if (port.isNotEmpty()) rows += textRow(port, 3.6f * u, t.sub, t.typeface, maxW, cx)

        if (c.memo.isNotEmpty()) rows += textRow(c.memo, 3.4f * u, t.dim, t.typeface, maxW, cx)

        // ── 배터리 ──
        if (c.showBattery && batteryPercent >= 0) rows += batteryRow(t, u, cx, maxW)

        val gap = 1.6f * u
        val total = rows.sumOf { it.height.toDouble() }.toFloat() + gap * (rows.size - 1)
        var y = (height - total) / 2
        for (r in rows) {
            r.draw(canvas, y)
            y += r.height + gap
        }
    }

    // ───────────────────────── 줄 만들기 ─────────────────────────

    private fun textRow(
        s: String, size: Float, color: Int, face: android.graphics.Typeface,
        maxW: Float, cx: Float, letterSpacing: Float = 0f,
    ): Row {
        val p = Paint(text).apply {
            typeface = face
            this.letterSpacing = letterSpacing
            textSize = size
            this.color = color
        }
        fitWidth(p, s, maxW)
        val fm = p.fontMetrics
        val h = fm.descent - fm.ascent
        return Row(h) { cv, y -> cv.drawText(s, cx, y - fm.ascent, p) }
    }

    private fun clockRow(main: String, suffix: String, size: Float, t: AodTheme, maxW: Float, cx: Float): Row {
        val big = Paint(text).apply { typeface = t.typeface; textSize = size; color = t.main; textAlign = Paint.Align.LEFT }
        val small = Paint(big).apply { textSize = size * 0.32f; color = t.sub }
        fun totalW() = big.measureText(main) + small.measureText(suffix)
        val tw = totalW()
        if (tw > maxW) {
            val k = maxW / tw
            big.textSize *= k
            small.textSize *= k
        }
        val fm = big.fontMetrics
        // 숫자만 있으므로 위아래 여백을 조금 덜어낸다
        val top = fm.ascent * 0.82f
        val h = fm.descent - top
        return Row(h) { cv, y ->
            val x0 = cx - totalW() / 2
            val base = y - top
            cv.drawText(main, x0, base, big)
            if (suffix.isNotEmpty()) cv.drawText(suffix, x0 + big.measureText(main), base, small)
        }
    }

    private fun watchRows(ship: ZonedDateTime, c: AodConfig, u: Float, cx: Float, maxW: Float): List<Row> {
        val t = c.theme
        val minuteOfDay = ship.hour * 60 + ship.minute
        val current = Watch.at(ship.hour)
        val bandStart = (ship.hour / 4) * 4 * 60
        val left = bandStart + 240 - minuteOfDay
        val progress = (minuteOfDay - bandStart) / 240f

        val mine = c.myWatch
        val head = "WATCH  ${current.label}   교대까지 ${hm(left)}"
        val tail = when {
            mine == null -> null
            mine == current -> "내 당직 중"
            else -> "내 당직까지 ${hm(untilWatch(mine, minuteOfDay))}"
        }
        val onDuty = mine != null && mine == current
        val rows = arrayListOf(textRow(head, 3.8f * u, if (onDuty) t.accent else t.sub, t.typeface, maxW, cx))
        rows += progressRow(progress, t, u, cx)
        if (tail != null) rows += textRow(tail, 3.4f * u, if (onDuty) t.accent else t.dim, t.typeface, maxW, cx)
        return rows
    }

    /** 지금(분)부터 그 당직이 시작될 때까지 남은 분 */
    private fun untilWatch(w: Watch, minuteOfDay: Int): Int =
        listOf(w.startHour, w.startHour + 12)
            .map { ((it * 60 - minuteOfDay) % 1440 + 1440) % 1440 }
            .minOf { it }

    private fun progressRow(p: Float, t: AodTheme, u: Float, cx: Float): Row {
        val bw = 46f * u
        val bh = if (t == AodTheme.PIXEL) 1.6f * u else 0.8f * u
        return Row(bh) { cv, y ->
            val x0 = cx - bw / 2
            when (t) {
                AodTheme.PIXEL -> {
                    // 경험치 바: 칸으로 나뉜 초록 막대
                    val n = 18
                    val gap = 0.3f * u
                    val cw = (bw - gap * (n - 1)) / n
                    val filled = (p * n).roundToInt()
                    for (i in 0 until n) {
                        fill.color = if (i < filled) t.accent else Color.rgb(40, 40, 40)
                        val x = x0 + i * (cw + gap)
                        cv.drawRect(x, y, x + cw, y + bh, fill)
                    }
                }
                AodTheme.TACTICAL -> {
                    fill.color = Color.rgb(45, 42, 40)
                    cv.drawRect(x0, y, x0 + bw, y + bh, fill)
                    fill.color = t.accent
                    cv.drawRect(x0, y, x0 + bw * p, y + bh, fill)
                }
                else -> {
                    val r = bh / 2
                    fill.color = Color.rgb(40, 40, 40)
                    cv.drawRoundRect(RectF(x0, y, x0 + bw, y + bh), r, r, fill)
                    fill.color = t.accent
                    cv.drawRoundRect(RectF(x0, y, x0 + bw * p.coerceAtLeast(0.02f), y + bh), r, r, fill)
                }
            }
        }
    }

    private fun decorationRow(t: AodTheme, u: Float, cx: Float): Row = when (t) {
        AodTheme.TACTICAL -> Row(1.6f * u) { cv, y ->
            // 가운데 빨간 줄 + 왼쪽 비스듬한 조각 + 오른쪽 작은 사각형들
            val half = 26f * u
            val mid = y + 0.8f * u
            fill.color = t.accent
            cv.drawRect(cx - half, mid - 0.15f * u, cx + half, mid + 0.15f * u, fill)
            val path = Path().apply {
                moveTo(cx - half - 4f * u, mid + 0.8f * u)
                lineTo(cx - half - 2.6f * u, mid - 0.8f * u)
                lineTo(cx - half + 0.6f * u, mid - 0.8f * u)
                lineTo(cx - half - 0.8f * u, mid + 0.8f * u)
                close()
            }
            cv.drawPath(path, fill)
            for (i in 0..2) {
                val x = cx + half + 1.2f * u + i * 1.4f * u
                cv.drawRect(x, mid - 0.4f * u, x + 0.8f * u, mid + 0.4f * u, fill)
            }
        }
        AodTheme.PIXEL -> Row(4f * u) { cv, y ->
            // 잔디 블록 한 줄
            val n = 9
            val b = 4f * u
            val x0 = cx - n * b / 2
            val px = b / 8
            for (i in 0 until n) {
                val x = x0 + i * b
                fill.color = Color.rgb(121, 85, 58)
                cv.drawRect(x, y, x + b, y + b, fill)
                fill.color = Color.rgb(93, 64, 42)
                for (k in 0 until 4) {
                    val dx = ((i * 3 + k * 5) % 7) * px
                    val dy = (3 + (i + k * 2) % 4) * px
                    cv.drawRect(x + dx, y + dy, x + dx + px, y + dy + px, fill)
                }
                fill.color = Color.rgb(93, 160, 48)
                cv.drawRect(x, y, x + b, y + 2 * px, fill)
                for (k in 0 until 8 step 2) {
                    val drop = ((i + k) % 3) * px
                    cv.drawRect(x + k * px, y + 2 * px, x + (k + 1) * px, y + 2 * px + drop, fill)
                }
            }
        }
        AodTheme.RADAR -> Row(2.4f * u) { cv, y ->
            // 나침반 눈금
            val half = 30f * u
            fill.color = t.dim
            var i = 0
            var x = cx - half
            while (x <= cx + half + 0.01f) {
                val tall = i % 5 == 0
                val th = if (tall) 1.8f * u else 0.9f * u
                cv.drawRect(x, y + 2.4f * u - th, x + 0.2f * u, y + 2.4f * u, fill)
                x += 2f * u
                i++
            }
            fill.color = t.accent
            val tri = Path().apply {
                moveTo(cx, y + 0.9f * u)
                lineTo(cx - 0.8f * u, y)
                lineTo(cx + 0.8f * u, y)
                close()
            }
            cv.drawPath(tri, fill)
        }
        AodTheme.CLASSIC -> Row(0.6f * u) { cv, y ->
            fill.color = t.dim
            cv.drawRect(cx - 12f * u, y + 0.2f * u, cx + 12f * u, y + 0.35f * u, fill)
        }
    }

    private fun batteryRow(t: AodTheme, u: Float, cx: Float, maxW: Float): Row {
        if (t != AodTheme.PIXEL) {
            val s = (if (charging) "충전 중  " else "배터리  ") + "$batteryPercent%"
            val color = if (batteryPercent <= 15 && !charging) Color.rgb(255, 90, 70) else t.dim
            return textRow(s, 3.2f * u, color, t.typeface, maxW, cx)
        }
        // 픽셀 테마: 하트 10개 = 100%
        val cell = 0.55f * u
        val hw = 7 * cell
        val gap = 0.8f * u
        val n = 10
        val full = (batteryPercent / 10f).roundToInt()
        return Row(6 * cell) { cv, y ->
            val x0 = cx - (n * hw + (n - 1) * gap) / 2
            for (i in 0 until n) {
                fill.color = if (i < full) {
                    if (charging) Color.rgb(255, 210, 60) else Color.rgb(230, 30, 40)
                } else Color.rgb(50, 50, 50)
                val hx = x0 + i * (hw + gap)
                HEART.forEachIndexed { r, line ->
                    line.forEachIndexed { col, ch ->
                        if (ch == 'X') cv.drawRect(hx + col * cell, y + r * cell, hx + (col + 1) * cell, y + (r + 1) * cell, fill)
                    }
                }
            }
        }
    }

    // ───────────────────────── 도우미 ─────────────────────────

    private fun fitWidth(p: Paint, s: String, maxW: Float) {
        val w = p.measureText(s)
        if (w > maxW) p.textSize *= maxW / w
    }

    private fun hm(minutes: Int) = "%d:%02d".format(minutes / 60, minutes % 60)

    private fun offsetText(o: ZoneOffset): String {
        val total = o.totalSeconds / 60
        val sign = if (total < 0) "-" else "+"
        val a = kotlin.math.abs(total)
        return if (a % 60 == 0) "$sign${a / 60}" else "$sign${a / 60}:%02d".format(a % 60)
    }

    companion object {
        private val HM = DateTimeFormatter.ofPattern("HH:mm")
        private val HEART = listOf(
            ".XX.XX.",
            "XXXXXXX",
            "XXXXXXX",
            ".XXXXX.",
            "..XXX..",
            "...X...",
        )
    }
}
