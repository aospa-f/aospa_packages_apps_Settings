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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Full-screen app block list used by the Axion edge gestures. */
public abstract class AxEdgeAppBlocklistSettings extends SettingsPreferenceFragment {

    protected abstract String getBlockedAppsKey();

    private final Set<String> mBlocked = new HashSet<>();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final PreferenceScreen screen =
                getPreferenceManager().createPreferenceScreen(getContext());
        setPreferenceScreen(screen);
        getActivity().setTitle(R.string.ax_edge_blocked_apps_title);

        final String stored = Settings.Secure.getString(
                getContentResolver(), getBlockedAppsKey());
        if (!TextUtils.isEmpty(stored)) {
            mBlocked.addAll(Arrays.asList(stored.split(",")));
        }

        final PackageManager packageManager = getPackageManager();
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = new ArrayList<>(
                packageManager.queryIntentActivities(launcherIntent, 0));
        apps.sort(Comparator.comparing(
                app -> app.loadLabel(packageManager).toString(),
                String.CASE_INSENSITIVE_ORDER));

        final Set<String> seen = new HashSet<>();
        for (ResolveInfo app : apps) {
            final String packageName = app.activityInfo.packageName;
            if (!seen.add(packageName)) {
                continue;
            }
            final SwitchPreference preference = new SwitchPreference(getContext());
            preference.setKey(packageName);
            preference.setTitle(app.loadLabel(packageManager));
            preference.setSummary(packageName);
            preference.setChecked(mBlocked.contains(packageName));
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                if ((Boolean) newValue) {
                    mBlocked.add(packageName);
                } else {
                    mBlocked.remove(packageName);
                }
                Settings.Secure.putString(
                        getContentResolver(),
                        getBlockedAppsKey(),
                        TextUtils.join(",", mBlocked));
                return true;
            });
            screen.addPreference(preference);
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURES;
    }
}
