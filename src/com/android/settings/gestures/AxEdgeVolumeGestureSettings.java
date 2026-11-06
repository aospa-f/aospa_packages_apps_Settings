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

import android.os.Bundle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.widget.SeekBarPreference;

/** Settings page for the edge volume gestures. */
public class AxEdgeVolumeGestureSettings extends AxEdgeGestureSettingsBase {

    private static final String STEP_KEY = "ax_edge_volume_step_dp";
    private static final int STEP_DEFAULT = 8;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        SeekBarPreference step = findPreference(STEP_KEY);
        if (step != null) {
            step.setMin(4);
            step.setMax(24);
            int value = Settings.Secure.getInt(getContentResolver(), STEP_KEY, STEP_DEFAULT);
            step.setProgress(Math.max(4, Math.min(24, value)));
            step.setOnPreferenceChangeListener((preference, newValue) -> {
                Settings.Secure.putInt(getContentResolver(), STEP_KEY, (Integer) newValue);
                return true;
            });
        }
    }

    @Override
    protected int getPreferenceXml() {
        return R.xml.ax_edge_volume_gesture_settings;
    }

    @Override
    protected String getEnabledKey() {
        return "ax_edge_volume_enabled";
    }

    @Override
    protected String getBlockedAppsKey() {
        return "ax_edge_volume_blocked_apps";
    }
}
