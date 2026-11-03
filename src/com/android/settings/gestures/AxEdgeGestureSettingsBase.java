/*
 * Copyright (C) 2026 The Paranoid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.gestures;

import android.app.settings.SettingsEnums;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/** Base fragment for the Axion edge gestures. */
public abstract class AxEdgeGestureSettingsBase extends SettingsPreferenceFragment {

    protected abstract int getPreferenceXml();

    protected abstract String getEnabledKey();

    protected abstract String getBlockedAppsKey();

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURES;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(getPreferenceXml(), rootKey);

        final SwitchPreference enabled = findPreference(getEnabledKey());
        if (enabled != null) {
            enabled.setChecked(
                    Settings.Secure.getInt(getContentResolver(), getEnabledKey(), 1) != 0);
            enabled.setOnPreferenceChangeListener((preference, newValue) -> {
                Settings.Secure.putInt(
                        getContentResolver(), getEnabledKey(), (Boolean) newValue ? 1 : 0);
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final Preference blocked = findPreference(getBlockedAppsKey());
        if (blocked == null) {
            return;
        }
        final String stored = Settings.Secure.getString(
                getContentResolver(), getBlockedAppsKey());
        final int count = TextUtils.isEmpty(stored) ? 0 : stored.split(",").length;
        blocked.setSummary(count == 0
                ? getString(R.string.ax_edge_blocked_apps_none)
                : getResources().getQuantityString(
                        R.plurals.ax_edge_blocked_apps_count, count, count));
    }
}
