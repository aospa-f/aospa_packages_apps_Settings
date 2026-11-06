/*
 * Copyright (C) 2026 The AOSPA Project
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

package com.android.settings.gamespace;

import android.app.settings.SettingsEnums;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** GameSpace settings: the game app list plus the two in-game features. */
public class GameSpaceSettings extends SettingsPreferenceFragment {
    private static final String KEY_LOCK = "ax_gamespace_lock";
    private static final String KEY_DND = "ax_gamespace_dnd";
    private static final String KEY_APPS = "ax_gamespace_apps";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURES;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gamespace_settings, rootKey);

        final SwitchPreference lock = findPreference(KEY_LOCK);
        if (lock != null) {
            lock.setChecked(Settings.Secure.getInt(getContentResolver(), KEY_LOCK, 1) != 0);
            lock.setOnPreferenceChangeListener((preference, newValue) -> {
                Settings.Secure.putInt(getContentResolver(), KEY_LOCK,
                        (Boolean) newValue ? 1 : 0);
                return true;
            });
        }

        final SwitchPreference dnd = findPreference(KEY_DND);
        if (dnd != null) {
            dnd.setChecked(Settings.Secure.getInt(getContentResolver(), KEY_DND, 1) != 0);
            dnd.setOnPreferenceChangeListener((preference, newValue) -> {
                Settings.Secure.putInt(getContentResolver(), KEY_DND,
                        (Boolean) newValue ? 1 : 0);
                return true;
            });
        }

        setupAppList();
    }

    private void setupAppList() {
        final MultiSelectListPreference apps = findPreference(KEY_APPS);
        if (apps == null) {
            return;
        }
        final PackageManager pm = getPackageManager();
        final Intent launcher = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> infos = pm.queryIntentActivities(launcher, 0);
        final List<String[]> items = new ArrayList<>();
        final Set<String> seen = new HashSet<>();
        for (ResolveInfo info : infos) {
            if (info.activityInfo == null) {
                continue;
            }
            final String pkg = info.activityInfo.packageName;
            if (!seen.add(pkg)) {
                continue;
            }
            final CharSequence label = info.activityInfo.applicationInfo == null
                    ? pkg : pm.getApplicationLabel(info.activityInfo.applicationInfo);
            items.add(new String[] {label.toString(), pkg});
        }
        Collections.sort(items, (a, b) -> a[0].compareToIgnoreCase(b[0]));

        final CharSequence[] entries = new CharSequence[items.size()];
        final CharSequence[] values = new CharSequence[items.size()];
        for (int i = 0; i < items.size(); i++) {
            entries[i] = items.get(i)[0];
            values[i] = items.get(i)[1];
        }
        apps.setEntries(entries);
        apps.setEntryValues(values);

        final Set<String> selected = new HashSet<>();
        final String stored = Settings.Secure.getString(getContentResolver(), KEY_APPS);
        if (stored != null && !stored.isEmpty()) {
            selected.addAll(Arrays.asList(stored.split(",")));
        }
        apps.setValues(selected);
        updateSummary(apps, selected.size());
        apps.setOnPreferenceChangeListener((preference, newValue) -> {
            @SuppressWarnings("unchecked")
            final Set<String> chosen = (Set<String>) newValue;
            Settings.Secure.putString(getContentResolver(), KEY_APPS,
                    String.join(",", chosen));
            updateSummary(apps, chosen.size());
            return true;
        });
    }

    private void updateSummary(MultiSelectListPreference preference, int count) {
        preference.setSummary(count == 0
                ? getString(R.string.gamespace_apps_none)
                : getString(R.string.gamespace_apps_selected, count));
    }
}
