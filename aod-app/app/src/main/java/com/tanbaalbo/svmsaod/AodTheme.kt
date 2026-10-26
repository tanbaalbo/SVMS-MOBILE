package com.tanbaalbo.svmsaod

import android.graphics.Color
import android.graphics.Typeface

/** AOD 색·글꼴·장식 묶음. 배경은 번인과 배터리를 위해 항상 검정. */
enum class AodTheme(
    val id: String,
    val main: Int,      // 큰 시계
    val sub: Int,       // 날짜·선박 정보
    val accent: Int,    // 강조(당직 바, 장식)
    val dim: Int,       // 흐린 글씨
    val typeface: Typeface,
) {
    CLASSIC(
        "classic",
        Color.rgb(235, 235, 235), Color.rgb(170, 170, 170),
        Color.rgb(120, 170, 255), Color.rgb(95, 95, 95),
        Typeface.create("sans-serif-light", Typeface.NORMAL),
    ),

    /** 발로란트 느낌: 빨강 + 크림색, 각진 장식 */
    TACTICAL(
        "tactical",
        Color.rgb(236, 232, 225), Color.rgb(190, 186, 180),
        Color.rgb(255, 70, 85), Color.rgb(110, 105, 100),
        Typeface.create("sans-serif-condensed", Typeface.BOLD),
    ),

    /** 마인크래프트 느낌: 고정폭 굵은 글씨, 잔디/흙 블록 줄, 경험치 바, 하트 배터리 */
    PIXEL(
        "pixel",
        Color.rgb(255, 255, 255), Color.rgb(200, 200, 200),
        Color.rgb(128, 255, 32), Color.rgb(110, 110, 110),
        Typeface.create(Typeface.MONOSPACE, Typeface.BOLD),
    ),

    /** 레이더 화면 느낌의 녹색 */
    RADAR(
        "radar",
        Color.rgb(80, 255, 120), Color.rgb(60, 200, 95),
        Color.rgb(255, 190, 60), Color.rgb(30, 110, 50),
        Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL),
    );

    companion object {
        fun byId(id: String): AodTheme = entries.firstOrNull { it.id == id } ?: TACTICAL
    }
}
