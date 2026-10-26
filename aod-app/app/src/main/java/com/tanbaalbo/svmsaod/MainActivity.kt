package com.tanbaalbo.svmsaod

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
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
        onClick("open_dream_settings") {
            try {
                startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "설정 > 디스플레이 > 화면 보호기에서 고르세요.", Toast.LENGTH_LONG).show()
            }
        }
        onClick("svms_login") { startActivity(Intent(requireContext(), SvmsLoginActivity::class.java)) }

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

    private fun onClick(key: String, action: () -> Unit) {
        findPreference<Preference>(key)?.setOnPreferenceClickListener { action(); true }
    }
}
