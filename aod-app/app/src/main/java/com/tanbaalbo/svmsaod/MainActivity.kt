package com.tanbaalbo.svmsaod

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

/** 첫 화면: AOD 켜기 + 모든 설정 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
        }
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        onClick("start_aod") { startActivity(Intent(requireContext(), AodActivity::class.java)) }
        onClick("open_dream_settings") { open(Intent(Settings.ACTION_DREAM_SETTINGS), "설정 > 디스플레이 > 화면 보호기에서 고르세요.") }
        onClick("svms_login") { startActivity(Intent(requireContext(), SvmsLoginActivity::class.java)) }
        onClick("overlay_permission") { openOverlaySettings() }
        onClick("battery_optimization") {
            open(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + requireContext().packageName)),
                "설정 > 애플리케이션 > SVMS AOD > 배터리에서 '제한 없음'을 고르세요.",
            )
        }

        findPreference<Preference>("auto_aod")?.setOnPreferenceChangeListener { _, value ->
            if (value == true) {
                if (!Settings.canDrawOverlays(requireContext())) {
                    Toast.makeText(requireContext(), "'다른 앱 위에 표시'를 허용해야 자동으로 켜집니다.", Toast.LENGTH_LONG).show()
                    openOverlaySettings()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                }
            }
            // 값이 저장된 뒤에 서비스를 맞춘다
            view?.post { context?.let(AodService::sync) }
            true
        }

        // 시차는 해석된 값으로 보여 준다 (잘못 쓰면 +9로 돌아간 것이 바로 보이게)
        findPreference<Preference>("utc_offset")?.summaryProvider = Preference.SummaryProvider<Preference> { p ->
            val raw = preferenceManager.sharedPreferences?.getString(p.key, "+9").orEmpty()
            "UTC" + AodConfig.parseOffset(raw).id.replace("Z", "+00:00")
        }
        findPreference<Preference>("eta")?.summaryProvider = Preference.SummaryProvider<Preference> { p ->
            preferenceManager.sharedPreferences?.getString(p.key, "").orEmpty().ifEmpty { "예: 10/28 14:00" }
        }
        findPreference<Preference>("svms_url")?.summaryProvider = Preference.SummaryProvider<Preference> { p ->
            preferenceManager.sharedPreferences?.getString(p.key, "").orEmpty()
        }
    }

    override fun onResume() {
        super.onResume()
        // 권한 화면에서 돌아왔을 때 상태를 다시 보여 주고, 꺼져 있던 대기 서비스를 살린다
        val ctx = requireContext()
        findPreference<Preference>("overlay_permission")?.summary =
            if (Settings.canDrawOverlays(ctx)) "허용됨" else "필요함: 눌러서 허용하세요 (자동 켜기에 필요)"
        val ignoring = ctx.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(ctx.packageName) == true
        findPreference<Preference>("battery_optimization")?.summary =
            if (ignoring) "빠짐" else "권장: 빼 두지 않으면 삼성 절전 기능이 대기 서비스를 끌 수 있습니다"
        AodService.sync(ctx)
    }

    private fun openOverlaySettings() = open(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + requireContext().packageName)),
        "설정 > 애플리케이션 > SVMS AOD > 다른 앱 위에 표시를 켜세요.",
    )

    private fun open(intent: Intent, fallback: String) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), fallback, Toast.LENGTH_LONG).show()
        }
    }

    private fun onClick(key: String, action: () -> Unit) {
        findPreference<Preference>(key)?.setOnPreferenceClickListener { action(); true }
    }
}
