/* SPDX-License-Identifier: Apache-2.0 */
package com.android.settings.custom.spoofing

import android.app.ActivityManager
import android.os.Bundle
import com.android.settings.R
import com.android.settings.SettingsPreferenceFragment
import androidx.preference.Preference
import com.android.internal.logging.nano.MetricsProto

/**
 * Device RKP keybox generation (Duck). Generates a genuine Google-issued RKP keybox
 * through the device's KeyMint trusted app. The keybox is saved to
 * /data/system/duck_rkp.xml (mode 0600) and can be installed into any keystore module.
 */
class TrickyStore : SettingsPreferenceFragment() {

    private var rkpController: DuckRkpPreferenceController? = null
    private var keyboxFilePreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.tricky_store)
        keyboxFilePreference = preferenceScreen.findPreference("ts_keybox_file")
        rkpController = DuckRkpPreferenceController(this) { refreshKeyboxFile() }
        rkpController?.start()
    }

    override fun onResume() {
        super.onResume()
        refreshKeyboxFile()
    }

    private fun refreshKeyboxFile() {
        val state = runCatching { ActivityManager.getService().duckRkpState }.getOrNull()
        val has = state?.getBoolean("has_keybox") == true
        keyboxFilePreference?.summary = if (has) {
            getString(R.string.ts_keybox_file_present)
        } else {
            getString(R.string.ts_keybox_file_absent)
        }
    }

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.VIEW_UNKNOWN
}
