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

public class JapaneseCustomRomSettings extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String SELECT_UI_PROPERTY = "persist.sys.ui.select";
    private static final String SELECT_UI_PHONE_PROPERTY = "persist.sys.ui.phone";
    private static final String SELECT_UI_TABLET_PROPERTY = "persist.sys.ui.tablet";
    private static final String SELECT_UI_PHABLET_PROPERTY = "persist.sys.ui.phablet";
    private static final String ACTIONBAR_BOTTOM_PROPERTY = "persist.sys.actionbar.bottom";
    private static final String MY_HOBBY_PROPERTY = "persist.sys.force.hobby";
    private static final String MY_THEME_PROPERTY = "persist.sys.theme";
    private static final String MY_SEFFECTS_PROPERTY = "persist.sys.sound.effects";
    private static final String MY_WALLPAPER_PROPERTY = "persist.sys.fixed.wallpaper";
    private static final String MY_HOMESCREEN_PROPERTY = "persist.sys.num.homescreen";
    private static final String MY_GRADIENT_PROPERTY = "persist.sys.prop.gradient";
    private static final String LAUNCHER_LANDSCAPE_PROPERTY = "persist.sys.launcher.landscape";
    private static final String LOCKSCREEN_ROTATE_PROPERTY = "persist.sys.lockscreen.rotate";
    private static final String NAVIKEY_ALPHA_PROPERTY = "persist.sys.alpha.navikey";
    private static final String MY_SEARCHBAR_PROPERTY = "persist.sys.prop.searchbar";
    private static final String MY_NOTIFICATION_PROPERTY = "persist.sys.notification";
    private static final String SELECT_DENSITY_PROPERTY = "persist.sys.ui.density";
    private static final String SELECT_BATTERY_PROPERTY = "persist.sys.battery.select";
    private static final String VOICE_CAPABLE_PROPERTY = "persist.sys.voice.capable";
    private static final String SMS_CAPABLE_PROPERTY = "persist.sys.sms.capable";
    private static final String LAUNCHER_DRAWER_PROPERTY = "persist.sys.launcher.drawer";
    private static final String SELECT_KEYLAYOUT_PROPERTY = "persist.sys.keylayout.select";
    private static final String SELECT_BLACKLIST_PROPERTY = "persist.sys.blacklist";

    private static final String SELECT_UI_KEY = "select_ui";
    private static final String ACTIONBAR_BOTTOM_KEY = "actionbar_bottom";
    private static final String FORCE_MY_HOBBY_KEY = "force_my_hobby";
    private static final String THEME_KEY = "theme_setting";
    private static final String DEVINFO_KEY = "jcrom_developer";
    private static final String FORCE_FIXED_WALLPAPER = "force_fixed_wallpaper";
    private static final String NUM_OF_HOMESCREEN = "number_of_homescreen";
    private static final String FORCE_MY_ANDROID_ID_KEY = "force_my_android_id";
    private static final String GRADIENT_KEY = "gradient_setting";
    private static final String ALLOW_LAUNCHER_LANDSCAPE_KEY = "launcher_landscape";
    private static final String LOCKSCREEN_ROTATE_KEY = "lockscreen_rotate";
    private static final String NAVIKEY_ALPHA_KEY = "navikey_alpha";
    private static final String SEARCHBAR_KEY = "searchbar_setting";
    private static final String NOTIFICATION_KEY = "notification_setting";
    private static final String HIDE_THEME_IMAGES = "hide_theme_images";
    private static final String SELECT_BATTERY_KEY = "select_battery";
    private static final String VOICE_CAPABLE_KEY = "voice_capable";
    private static final String SMS_CAPABLE_KEY = "sms_capable";
    private static final String LAUNCHER_DRAWER_KEY = "drawer_transmission";
    private static final String SELECT_KEYLAYOUT_KEY = "select_keylayout";
    private static final String INSTALL_BLACK_LIST_KEY = "install_blacklist";
    private static final String SELECT_BLACKLIST_KEY = "enable_blacklist";

    private static final String TAG = "JapaneseCustomRomSettings";

    private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();
    private final ArrayList<Preference> mBatteryPrefs = new ArrayList<Preference>();
    private final ArrayList<Preference> mKeyLayoutPrefs = new ArrayList<Preference>();
    private ListPreference mSelectUi;
    private CheckBoxPreference mActionBarBottom;
    private CheckBoxPreference mForceMyHobby;
    private PreferenceScreen mTheme;
    private CheckBoxPreference mFixedWallpaper;
    private ListPreference mNumHomescreen;
    private PreferenceScreen mForceMyAndroidId;
    private CheckBoxPreference mGradientStat;
    private CheckBoxPreference mLauncherLandscape;
    private CheckBoxPreference mLockscreenRotate;
    private ProgressDialog mProgressDialog;
    private CheckBoxPreference mNavikeyAlpha;
    private CheckBoxPreference mDisableSearchbar;
    private CheckBoxPreference mNotification;
    private CheckBoxPreference mHideThemeImages;
    private CheckBoxPreference mBatteryPercentage;
    private ListPreference mSelectBattery;
    private CheckBoxPreference mVoiceCapable;
    private CheckBoxPreference mSmsCapable;
    private CheckBoxPreference mLauncherDrawer;
    private ListPreference mSelectKeyLayout;
    private EditTextPreference mEditInstallBlackList;
    private CheckBoxPreference mSelectBlackList;

    private String mAndroidId;

    private static final int INTENT_SET_THEME = 1;
    private static final int RESULT_OK = -1;
    private static final int RESULT_CANCELED = 0;

    private static final int SELECT_BATTERY_NORMAL = 0;
    private static final int SELECT_BATTERY_PERCENTAGE = 1;
    private static final int SELECT_BATTERY_THEME = 2;

    private static final String THEME_LOCK = "persist.sys.theme.lock";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.jcrom_settings);
        mSelectUi = (ListPreference) findPreference(SELECT_UI_KEY);
        mAllPrefs.add(mSelectUi);
        mSelectUi.setOnPreferenceChangeListener(this);
        selectUi();

        mSelectBattery = (ListPreference) findPreference(SELECT_BATTERY_KEY);
        mBatteryPrefs.add(mSelectBattery);
        mSelectBattery.setOnPreferenceChangeListener(this);
        selectBattery();

        mSelectKeyLayout = (ListPreference) findPreference(SELECT_KEYLAYOUT_KEY);
        mKeyLayoutPrefs.add(mSelectKeyLayout);
        mSelectKeyLayout.setOnPreferenceChangeListener(this);
        selectKeyLayout();

        mActionBarBottom = (CheckBoxPreference) findPreference(ACTIONBAR_BOTTOM_KEY);
        mForceMyHobby = (CheckBoxPreference) findPreference(FORCE_MY_HOBBY_KEY);
        mTheme = (PreferenceScreen) findPreference(THEME_KEY);
        mFixedWallpaper = (CheckBoxPreference) findPreference(FORCE_FIXED_WALLPAPER);
        mNumHomescreen = (ListPreference) findPreference(NUM_OF_HOMESCREEN);
        mForceMyAndroidId = (PreferenceScreen) findPreference(FORCE_MY_ANDROID_ID_KEY);
        mGradientStat = (CheckBoxPreference) findPreference(GRADIENT_KEY);
        mLauncherLandscape = (CheckBoxPreference) findPreference(ALLOW_LAUNCHER_LANDSCAPE_KEY);
        mLockscreenRotate = (CheckBoxPreference) findPreference(LOCKSCREEN_ROTATE_KEY);
        mNavikeyAlpha = (CheckBoxPreference) findPreference(NAVIKEY_ALPHA_KEY);
        mDisableSearchbar = (CheckBoxPreference) findPreference(SEARCHBAR_KEY);
        mNotification = (CheckBoxPreference) findPreference(NOTIFICATION_KEY);
        mHideThemeImages = (CheckBoxPreference) findPreference(HIDE_THEME_IMAGES);
        mVoiceCapable = (CheckBoxPreference) findPreference(VOICE_CAPABLE_KEY);
        mSmsCapable = (CheckBoxPreference) findPreference(SMS_CAPABLE_KEY);
        mLauncherDrawer = (CheckBoxPreference) findPreference(LAUNCHER_DRAWER_KEY);
        mEditInstallBlackList = (EditTextPreference) findPreference(INSTALL_BLACK_LIST_KEY);
        mSelectBlackList = (CheckBoxPreference) findPreference(SELECT_BLACKLIST_KEY);

        if ((SystemProperties.get(MY_THEME_PROPERTY) != null) && (SystemProperties.get(MY_THEME_PROPERTY) != "")) {
            mTheme.setSummary(SystemProperties.get(MY_THEME_PROPERTY));
        }

        String screenNumStr = SystemProperties.get("persist.sys.num.homescreen");
        if(screenNumStr == null || screenNumStr.length() == 0) {
            mNumHomescreen.setSummary(R.string.number_of_homescreen_summary);
        } else if (Integer.valueOf(screenNumStr) == 0){
            mNumHomescreen.setSummary(R.string.number_of_homescreen_summary_default);
        } else {
            mNumHomescreen.setSummary(SystemProperties.get(MY_HOMESCREEN_PROPERTY));
        }

        mNumHomescreen.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue){
                        ListPreference _list = (ListPreference)findPreference(NUM_OF_HOMESCREEN);

                        if(_list == preference && newValue != null){
                            String screenNum = (String)newValue.toString();

                            if(Integer.valueOf(screenNum) == 0){
                                _list.setSummary(R.string.number_of_homescreen_summary_default);
                            } else {
                                _list.setSummary(screenNum);
                            }
                            writeNumberofScreenOptions(screenNum);
                            new ThemeManager(getActivity()).restartLauncher();
                        }
                        return true;
                    }
                });

        mAndroidId = Settings.Secure.getString(
                getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        mForceMyAndroidId.setSummary(mAndroidId);

        mGradientStat.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        CheckBoxPreference _cb = (CheckBoxPreference) findPreference(GRADIENT_KEY);

                        if (_cb == preference && newValue != null) {
                            new ThemeManager(getActivity()).restartLauncher();
                        }
                        return true;
                    }
                });

        mHideThemeImages.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    CheckBoxPreference _cb = (CheckBoxPreference) findPreference(HIDE_THEME_IMAGES);

                    String themeDir = Environment.getExternalStorageDirectory().toString() + "/mytheme/";
                    File file = new File(themeDir + ".nomedia");

                    try{
                        if (_cb == preference && ((Boolean)newValue).booleanValue()) {
                            file.createNewFile();
                        } else if (_cb == preference && !((Boolean)newValue).booleanValue()) {
                            file.delete();
                        }
                        confirmReset();
                    } catch(Exception e) {
                        return false;
                    }
                    return true;
                }
            }
        );

        mVoiceCapable.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    CheckBoxPreference _cb = (CheckBoxPreference) findPreference(VOICE_CAPABLE_KEY);
                    if (_cb == preference && newValue != null) {
                        confirmReset();
                    }
                    return true;
                }
            });

        mSmsCapable.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    CheckBoxPreference _cb = (CheckBoxPreference) findPreference(SMS_CAPABLE_KEY);
                    if (_cb == preference && newValue != null) {
                        confirmReset();
                    }
                    return true;
                }
            });

        mLauncherDrawer.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    CheckBoxPreference _cb = (CheckBoxPreference) findPreference(LAUNCHER_DRAWER_KEY);
                    if (_cb == preference && newValue != null) {
                        new ThemeManager(getActivity()).restartLauncher();
                    }
                    return true;
                }
            });

        mEditInstallBlackList.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue){
                        if (newValue != null) {
                            Settings.Secure.putString(getActivity().getContentResolver(),Settings.Secure.INSTALL_BLACK_LIST,newValue.toString());
                        }
                        return true;
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();

        closeProgress.run();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateActionBarBottomOptions();
        updateNotificationOptions();
        updateLockscreenRotate();
        updateFixedWallpaper();
        updateLauncherLandscape();
        updateGradientStat();
        updateNavikeyAlpha();
        updateDisableSearchbar();
        updateNumHomescreen();
        updateForceMyHobby();
        updateVoiceCapable();
        updateSmsCapable();
        updateLauncherDrawer();
        updateBlackList();
    }

    private void updateActionBarBottomOptions() {
        mActionBarBottom.setChecked(SystemProperties.getBoolean(ACTIONBAR_BOTTOM_PROPERTY, false));
    }
    private void updateNotificationOptions() {
        mNotification.setChecked(SystemProperties.getBoolean(MY_NOTIFICATION_PROPERTY, false));
    }
    private void updateLockscreenRotate() {
        mLockscreenRotate.setChecked(SystemProperties.getBoolean(LOCKSCREEN_ROTATE_PROPERTY, false));
    }
    private void updateFixedWallpaper() {
        mFixedWallpaper.setChecked(SystemProperties.getBoolean(MY_WALLPAPER_PROPERTY, false));
    }
    private void updateLauncherLandscape() {
        mLauncherLandscape.setChecked(SystemProperties.getBoolean(LAUNCHER_LANDSCAPE_PROPERTY, false));
    }
    private void updateGradientStat() {
        mGradientStat.setChecked(SystemProperties.getBoolean(MY_GRADIENT_PROPERTY, false));
    }
    private void updateNavikeyAlpha() {
        mNavikeyAlpha.setChecked(SystemProperties.getBoolean(NAVIKEY_ALPHA_PROPERTY, false));
    }
    private void updateDisableSearchbar() {
        mDisableSearchbar.setChecked(SystemProperties.getBoolean(MY_SEARCHBAR_PROPERTY, false));
    }
    private void updateNumHomescreen() {
        mNumHomescreen.setValueIndex(getNumHomescreen());
    }
    private void updateForceMyHobby() {
        mForceMyHobby.setChecked(SystemProperties.getBoolean(MY_HOBBY_PROPERTY, false));
        if (SystemProperties.get(MY_THEME_PROPERTY) != null) {
            if (SystemProperties.get(MY_THEME_PROPERTY) != "") {
                mTheme.setSummary(SystemProperties.get(MY_THEME_PROPERTY));
            } else {
                mTheme.setSummary("");
            }
        }
    }
    private void updateVoiceCapable() {
        mVoiceCapable.setChecked(SystemProperties.getBoolean(VOICE_CAPABLE_PROPERTY, true));
    }
    private void updateSmsCapable() {
        mSmsCapable.setChecked(SystemProperties.getBoolean(SMS_CAPABLE_PROPERTY, true));
    }
    private void updateLauncherDrawer() {
        mLauncherDrawer.setChecked(SystemProperties.getBoolean(LAUNCHER_DRAWER_PROPERTY, false));
    }
    private void updateInstallBlacklist() {
        String blacklist = Settings.Secure.getString(getActivity().getContentResolver(),Settings.Secure.INSTALL_BLACK_LIST);
        if (blacklist == null) blacklist = "";
        mEditInstallBlackList.setText(blacklist);
    }
    private void updateBlackList() {
        mSelectBlackList.setChecked(SystemProperties.getBoolean(SELECT_BLACKLIST_PROPERTY, false));
    }

    private int getNumHomescreen() {
        int num = 0;
        String sNum = SystemProperties.get(MY_HOMESCREEN_PROPERTY, "0");
        int iNum = Integer.parseInt(sNum);
        switch(iNum) {
            case 0:
                num = 0;
                break;
            case 1:
                num = 1;
                break;
            case 3:
                num = 2;
                break;
            case 5:
                num = 3;
                break;
            case 7:
                num = 4;
                break;
            default:
                num = 0;
                break;
        }
        return num;
    }

    private int defaultUi() {
        int defaultDensity = SystemProperties.getInt("ro.sf.lcd_density", DisplayMetrics.DENSITY_DEFAULT);

        if (defaultDensity < 600)
            return 0;
        else
            return 1;
    }

    private void setUiMode(String select_ui) {
        int select = Integer.parseInt(select_ui);
        int defaultDensity = SystemProperties.getInt("ro.sf.lcd_density", DisplayMetrics.DENSITY_DEFAULT);
        int density = DisplayMetrics.DENSITY_DEFAULT;

        if (select == 0) {
            density = SystemProperties.getInt(SELECT_UI_PHONE_PROPERTY, defaultDensity);
        } else if (select == 1) {
            density = SystemProperties.getInt(SELECT_UI_TABLET_PROPERTY, defaultDensity);
        } else if (select == 2) {
            density = SystemProperties.getInt(SELECT_UI_PHABLET_PROPERTY, defaultDensity);
        } else {
            density = defaultDensity;
        }

        SystemProperties.set(SELECT_DENSITY_PROPERTY, String.valueOf(density));
    }

    private void setBatteryMode(String select_battery) {
        int select = Integer.parseInt(select_battery);
        int battery_mode = SELECT_BATTERY_NORMAL;

        if (select == SELECT_BATTERY_PERCENTAGE) {
            battery_mode = SELECT_BATTERY_PERCENTAGE;
        } else if (select == SELECT_BATTERY_THEME) {
            battery_mode = SELECT_BATTERY_THEME;
        } else {
            battery_mode = SELECT_BATTERY_NORMAL;
        }

        SystemProperties.set(SELECT_BATTERY_PROPERTY, String.valueOf(battery_mode));
    }

    private void setKeyLayoutMode(String select_keylayout) {
        SystemProperties.set(SELECT_KEYLAYOUT_PROPERTY, select_keylayout);
    }

    private void selectUi() {
        int select = SystemProperties.getInt(SELECT_UI_PROPERTY, -1);
        if(select != -1) {
            mSelectUi.setValueIndex(select);
            mSelectUi.setSummary(mSelectUi.getEntries()[select]);
        } else {
            mSelectUi.setValueIndex(defaultUi());
            mSelectUi.setSummary(mSelectUi.getEntries()[defaultUi()]);
        }
    }

    private void selectBattery() {
        int select = SystemProperties.getInt(SELECT_BATTERY_PROPERTY, -1);
        if(select != -1) {
            mSelectBattery.setValueIndex(select);
            mSelectBattery.setSummary(mSelectBattery.getEntries()[select]);
        } else {
            mSelectBattery.setValueIndex(SELECT_BATTERY_NORMAL);
            mSelectBattery.setSummary(mSelectBattery.getEntries()[SELECT_BATTERY_NORMAL]);
        }
    }

    private void selectKeyLayout() {
        int select = SystemProperties.getInt(SELECT_KEYLAYOUT_PROPERTY, -1);
        if(select != -1) {
            mSelectKeyLayout.setValueIndex(select);
            mSelectKeyLayout.setSummary(mSelectKeyLayout.getEntries()[select]);
        } else {
            mSelectKeyLayout.setValueIndex(0);
            mSelectKeyLayout.setSummary(mSelectKeyLayout.getEntries()[0]);
        }
    }

    private void writeNotificationOptions() {
        SystemProperties.set(MY_NOTIFICATION_PROPERTY, mNotification.isChecked() ? "true" : "false");
        //new ThemeManager(getActivity()).restartSystemUI(closeProgress);
    }

    private void writeActionBarBottomOptions() {
        SystemProperties.set(ACTIONBAR_BOTTOM_PROPERTY, mActionBarBottom.isChecked() ? "true" : "false");
    }

    private void updateMyHobbyOptions() {
        mForceMyHobby.setChecked(SystemProperties.getBoolean(MY_HOBBY_PROPERTY, false));
    }

    private void writeMyHobbyOptions() {
        SystemProperties.set(MY_HOBBY_PROPERTY, mForceMyHobby.isChecked() ? "true" : "false");
        if (!(mForceMyHobby.isChecked())) {
            SystemProperties.set(MY_THEME_PROPERTY, "");
            mTheme.setSummary("");
            //confirmResetForUnetTheme();
            showProgress(R.string.progress_clear_theme);
            new ThemeManager(getActivity()).clearTheme(closeProgress);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        
        if(requestCode == INTENT_SET_THEME){
            if(resultCode == RESULT_OK){
                // SetTheme succeeded.
                mTheme.setSummary(SystemProperties.get(MY_THEME_PROPERTY));
            }else if(resultCode == RESULT_CANCELED){
                // SetTheme canceled by user.
            }

        }
    }

    private void writeMyWallpaperOptions() {
        SystemProperties.set(MY_WALLPAPER_PROPERTY, mFixedWallpaper.isChecked() ? "true" : "false");
    }

    private void writeNumberofScreenOptions(String screenNum) {
        SystemProperties.set(MY_HOMESCREEN_PROPERTY, screenNum);
    }

    private void writeForceMyAndroidId(String newAndroidId) {
        if(newAndroidId == null || newAndroidId.equals("")) {
            final SecureRandom random = new SecureRandom();
            newAndroidId = Long.toHexString(random.nextLong());
        }
        Settings.Secure.putString(getActivity().getContentResolver()
                , Settings.Secure.ANDROID_ID, newAndroidId);
        mForceMyAndroidId.setSummary(newAndroidId);
        mAndroidId = newAndroidId;
    }

    private void writeGradientOptions() {
        SystemProperties.set(MY_GRADIENT_PROPERTY, mGradientStat.isChecked() ? "true" : "false");
    }

    private void writeLauncherLandscape() {
        SystemProperties.set(LAUNCHER_LANDSCAPE_PROPERTY, mLauncherLandscape.isChecked() ? "true" : "false");
        new ThemeManager(getActivity()).restartLauncher();
    }

    private void writeLockscreenRotate() {
        SystemProperties.set(LOCKSCREEN_ROTATE_PROPERTY, mLockscreenRotate.isChecked() ? "true" : "false");
    }

    private void writeNavikeyAlphaOptions() {
        SystemProperties.set(NAVIKEY_ALPHA_PROPERTY, mNavikeyAlpha.isChecked() ? "true" : "false");
    }

    private void writeSearchbarOptions() {
        SystemProperties.set(MY_SEARCHBAR_PROPERTY, mDisableSearchbar.isChecked() ? "true" : "false");
    }

    private void writeVoiceCapable() {
        SystemProperties.set(VOICE_CAPABLE_PROPERTY, mVoiceCapable.isChecked() ? "true" : "false");
    }

    private void writeSmsCapable() {
        SystemProperties.set(SMS_CAPABLE_PROPERTY, mSmsCapable.isChecked() ? "true" : "false");
    }

    private void writeLauncherDrawer() {
        SystemProperties.set(LAUNCHER_DRAWER_PROPERTY, mLauncherDrawer.isChecked() ? "true" : "false");
    }

    private void writeBlackListOptions() {
        SystemProperties.set(SELECT_BLACKLIST_PROPERTY, mSelectBlackList.isChecked() ? "true" : "false");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mActionBarBottom) {
            writeActionBarBottomOptions();
        } else if (preference == mNotification) {
            writeNotificationOptions();
        } else if (preference == mForceMyHobby) {
            writeMyHobbyOptions();
        } else if (preference == mTheme) {
            if(mForceMyHobby.isChecked()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("jcrom:///disp_theme"));
                startActivityForResult(intent, INTENT_SET_THEME);
            }
        } else if (preference == mFixedWallpaper) {
            writeMyWallpaperOptions();
        } else if (preference == mForceMyAndroidId) {
            showNewAndroidIdDialog();
        } else if (preference == mGradientStat) {
            writeGradientOptions();
        } else if (preference == mLauncherLandscape) {
            writeLauncherLandscape();
        } else if (preference == mLockscreenRotate) {
            writeLockscreenRotate();
        } else if (preference == mNavikeyAlpha){
            writeNavikeyAlphaOptions();
        } else if (preference == mDisableSearchbar) {
            writeSearchbarOptions();
        } else if (preference == mVoiceCapable) {
            writeVoiceCapable();
        } else if (preference == mSmsCapable) {
            writeSmsCapable();
        } else if (preference == mLauncherDrawer) {
            writeLauncherDrawer();
        } else if (preference == mSelectBlackList) {
            writeBlackListOptions();
        } else {
        }


        return false;
    }

    private void showProgress(int resid) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(resid));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private final Runnable closeProgress = new Runnable() {
        @Override
        public void run() {
            SystemProperties.set(THEME_LOCK, "false");
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;

                Intent intent = new Intent();
                intent.setClassName("com.android.jclauncher", "com.android.jclauncher.Launcher");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    };

    private void showNewAndroidIdDialog() {
        final EditText editView = new EditText(getActivity());
        editView.setText(mAndroidId);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.force_my_android_id);
        builder.setView(editView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newAndroidId = editView.getText().toString();
                writeForceMyAndroidId(newAndroidId);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        builder.show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSelectUi) {
            SystemProperties.set(SELECT_UI_PROPERTY, newValue.toString());
            selectUi();
            setUiMode(newValue.toString());
            confirmReset();
            return true;
        }
        if (preference == mSelectBattery) {
            SystemProperties.set(SELECT_BATTERY_PROPERTY, newValue.toString());
            selectBattery();
            setBatteryMode(newValue.toString());
            confirmReset();
            return true;
        }
        if (preference == mSelectKeyLayout) {
            SystemProperties.set(SELECT_KEYLAYOUT_PROPERTY, newValue.toString());
            selectKeyLayout();
            setKeyLayoutMode(newValue.toString());
            confirmReset();
            return true;
        }        
        return false;
    }

    private void confirmSystemUIReset() {
        new ThemeManager(getActivity()).restartSystemUI(closeProgress);
    }

    private void confirmReset() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
                    pm.reboot(null);
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.select_ui_confirm_reboot);
        builder.setPositiveButton(R.string.select_ui_confirm_yes, listener);
        builder.setNegativeButton(R.string.select_ui_confirm_no, listener);
        builder.show();
    }
    
    private String removeFileExtension(String filename) {
        int lastDotPos = filename.lastIndexOf('.');

        if (lastDotPos == -1) {
            return filename;
        } else if (lastDotPos == 0) {
            return filename;
        } else {
            return filename.substring(0, lastDotPos);
        }
    }

    private void confirmResetForUnetTheme() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean performReset = false;
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    performReset = true;
                }
                showProgress(R.string.progress_clear_theme);
                new ThemeManager(getActivity()).clearTheme(closeProgress);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unset_theme_confirm_reboot);
        builder.setPositiveButton(R.string.set_theme_confirm_yes, listener);
        builder.setNegativeButton(R.string.set_theme_confirm_no, listener);
        builder.show();
    }

}
