/*
 * Copyright 2014 JCROM Project
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

package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.EditText;
import android.content.Intent;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import android.util.DisplayMetrics;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;

public class JapaneseCustomRomBatterySaver extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "JCBatterySaver";

    private static final String BATTERY_SAVER_MAX_FREQ_PROPERTY   = "persist.sys.max.freq";
    private static final String BATTERY_SAVER_STATUSBAR_PROPERTY  = "persist.sys.bar.color";
    private static final String BATTERY_SAVER_ANIMATION_PROPERTY  = "persist.sys.animation";
    private static final String BATTERY_SAVER_BACKGROUND_PROPERTY = "persist.sys.background";
    private static final String BATTERY_SAVER_VIBRATOR_PROPERTY   = "persist.sys.vibrator";

    private static final String BATTERY_SAVER_MAX_FREQ_KEY   = "batterysaver_maxfreq";
    private static final String BATTERY_SAVER_STATUSBAR_KEY  = "batterysaver_statusbar";
    private static final String BATTERY_SAVER_ANIMATION_KEY  = "batterysaver_animation";
    private static final String BATTERY_SAVER_BACKGROUND_KEY = "batterysaver_background";
    private static final String BATTERY_SAVER_VIBRATOR_KEY   = "batterysaver_vibrator";

    private final ArrayList<Preference> mBatterySaverMaxFreqPrefs = new ArrayList<Preference>();
    private ListPreference mBatterySaverMaxFreq;

    private CheckBoxPreference mBatterySaverBarColor;
    private CheckBoxPreference mBatterySaverAnimation;
    private CheckBoxPreference mBatterySaverBackground;
    private CheckBoxPreference mBatterySaverVibrator;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.jcrom_batterysaver);

        mBatterySaverMaxFreq = (ListPreference) findPreference(BATTERY_SAVER_MAX_FREQ_KEY);
        mBatterySaverMaxFreqPrefs.add(mBatterySaverMaxFreq);
        mBatterySaverMaxFreq.setOnPreferenceChangeListener(this);
        selectBatterySaverMaxFreq();

        mBatterySaverBarColor = (CheckBoxPreference) findPreference(BATTERY_SAVER_STATUSBAR_KEY);
        mBatterySaverAnimation = (CheckBoxPreference) findPreference(BATTERY_SAVER_ANIMATION_KEY);
        mBatterySaverBackground = (CheckBoxPreference) findPreference(BATTERY_SAVER_BACKGROUND_KEY);
        mBatterySaverVibrator = (CheckBoxPreference) findPreference(BATTERY_SAVER_VIBRATOR_KEY);

    }

    @Override
    public void onResume() {
        super.onResume();

        updateBatterySaverBarColorOptions();
        updateBatterySaverAnimationOptions();
        updateBatterySaverBackgroundOptions();
        updateBatterySaverVibratorOptions();
    }

    private void setBatterySaverMaxFreq(String select_maxfreq) {
        SystemProperties.set(BATTERY_SAVER_MAX_FREQ_PROPERTY, select_maxfreq);
    }
    private void writemBatterySaverBarColorOptions() {
        SystemProperties.set(BATTERY_SAVER_STATUSBAR_PROPERTY, mBatterySaverBarColor.isChecked() ? "true" : "false");
    }
    private void writemBatterySaverAnimationOptions() {
        SystemProperties.set(BATTERY_SAVER_ANIMATION_PROPERTY, mBatterySaverAnimation.isChecked() ? "true" : "false");
    }
    private void writemBatterySaverBackgroundOptions() {
        SystemProperties.set(BATTERY_SAVER_BACKGROUND_PROPERTY, mBatterySaverBackground.isChecked() ? "true" : "false");
    }
    private void writemBatterySaverVibratorOptions() {
        SystemProperties.set(BATTERY_SAVER_VIBRATOR_PROPERTY, mBatterySaverVibrator.isChecked() ? "true" : "false");
    }


    private void selectBatterySaverMaxFreq() {
        int select = SystemProperties.getInt(BATTERY_SAVER_MAX_FREQ_PROPERTY, -1);
        if(select != -1) {
            mBatterySaverMaxFreq.setValueIndex(select);
            mBatterySaverMaxFreq.setSummary(mBatterySaverMaxFreq.getEntries()[select]);
        } else {
            mBatterySaverMaxFreq.setValueIndex(0);
            mBatterySaverMaxFreq.setSummary(mBatterySaverMaxFreq.getEntries()[0]);
        }
    }

    private void updateBatterySaverBarColorOptions() {
        mBatterySaverBarColor.setChecked(SystemProperties.getBoolean(BATTERY_SAVER_STATUSBAR_PROPERTY, true));
    }
    private void updateBatterySaverAnimationOptions() {
        mBatterySaverAnimation.setChecked(SystemProperties.getBoolean(BATTERY_SAVER_ANIMATION_PROPERTY, true));
    }
    private void updateBatterySaverBackgroundOptions() {
        mBatterySaverBackground.setChecked(SystemProperties.getBoolean(BATTERY_SAVER_BACKGROUND_PROPERTY, true));
    }
    private void updateBatterySaverVibratorOptions() {
        mBatterySaverVibrator.setChecked(SystemProperties.getBoolean(BATTERY_SAVER_VIBRATOR_PROPERTY, true));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mBatterySaverBarColor) {
            writemBatterySaverBarColorOptions();
        } else if (preference == mBatterySaverAnimation) {
            writemBatterySaverAnimationOptions();
        } else if (preference == mBatterySaverBackground) {
            writemBatterySaverBackgroundOptions();
        } else if (preference == mBatterySaverVibrator) {
            writemBatterySaverVibratorOptions();
        } else {
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatterySaverMaxFreq) {
            SystemProperties.set(BATTERY_SAVER_MAX_FREQ_PROPERTY, newValue.toString());
            selectBatterySaverMaxFreq();
            setBatterySaverMaxFreq(newValue.toString());
            return true;
        }

        return false;
    }

}
