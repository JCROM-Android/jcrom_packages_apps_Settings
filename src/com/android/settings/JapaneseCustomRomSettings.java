
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

public class JapaneseCustomRomSettings extends PreferenceFragment
        implements OnPreferenceChangeListener, FileListDialog.onFileListDialogListener {

    private static final String SELECT_UI_PROPERTY = "persist.sys.ui.select";
    private static final String SELECT_UI_PHONE_PROPERTY = "persist.sys.ui.phone";
    private static final String SELECT_UI_TABLET_PROPERTY = "persist.sys.ui.tablet";
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
    private static final String BATTERY_PERCENTAGE_PROPERTY = "persist.sys.battery.percentage";
    private static final String VOICE_CAPABLE_PROPERTY = "persist.sys.voice.capable";
    private static final String SMS_CAPABLE_PROPERTY = "persist.sys.sms.capable";
    private static final String LAUNCHER_DRAWER_PROPERTY = "persist.sys.launcher.drawer";

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
    private static final String BATTERY_PERCENTAGE_KEY = "battery_percentage";
    private static final String VOICE_CAPABLE_KEY = "voice_capable";
    private static final String SMS_CAPABLE_KEY = "sms_capable";
    private static final String LAUNCHER_DRAWER_KEY = "drawer_transmission";

    private static final String TAG = "JapaneseCustomRomSettings";

    private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();
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
    private CheckBoxPreference mVoiceCapable;
    private CheckBoxPreference mSmsCapable;
    private CheckBoxPreference mLauncherDrawer;

    private String mAndroidId;

    private static final int INTENT_SET_THEME = 1;
    private static final int RESULT_OK = -1;
    private static final int RESULT_CANCELED = 0;

    private static final String THEME_LOCK = "persist.sys.theme.lock";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.jcrom_settings);
        mSelectUi = (ListPreference) findPreference(SELECT_UI_KEY);
        mAllPrefs.add(mSelectUi);
        mSelectUi.setOnPreferenceChangeListener(this);
        selectUi();
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
        mBatteryPercentage = (CheckBoxPreference) findPreference(BATTERY_PERCENTAGE_KEY);
        mVoiceCapable = (CheckBoxPreference) findPreference(VOICE_CAPABLE_KEY);
        mSmsCapable = (CheckBoxPreference) findPreference(SMS_CAPABLE_KEY);
        mLauncherDrawer = (CheckBoxPreference) findPreference(LAUNCHER_DRAWER_KEY);

        if ((SystemProperties.get(MY_THEME_PROPERTY) != null) && (SystemProperties.get(MY_THEME_PROPERTY) != "")) {
            mTheme.setSummary(SystemProperties.get(MY_THEME_PROPERTY));
        }

        // No way to setsummary after changing number of homescreen ? undertesting.
        if ((SystemProperties.get(MY_HOMESCREEN_PROPERTY) != null) && (SystemProperties.get(MY_HOMESCREEN_PROPERTY) != "")) {
            mNumHomescreen.setSummary(SystemProperties.get(MY_HOMESCREEN_PROPERTY));
        }else{
            mNumHomescreen.setSummary(R.string.number_of_homescreen_summary);
        }

        mNumHomescreen.setOnPreferenceChangeListener(
                new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue){
                        // TODO hand-generated method stub;;
                          ListPreference _list = (ListPreference)findPreference(NUM_OF_HOMESCREEN);

                          if(_list == preference && newValue != null){
                              String screenNum = (String)newValue.toString();

                              _list.setSummary(screenNum);
                              writeNumberofScreenOptions(screenNum);

                            try {
                                ActivityManager am = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                                am.forceStopPackage("com.android.launcher3");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                        // TODO hand-generated method stub;;
                        CheckBoxPreference _cb = (CheckBoxPreference) findPreference(GRADIENT_KEY);

                        if (_cb == preference && newValue != null) {
                            showProgress(R.string.gradient_setting_progress);
                            new ThemeManager(getActivity()).restartLauncher(closeProgress);
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

        mBatteryPercentage.setOnPreferenceChangeListener(
            new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    CheckBoxPreference _cb = (CheckBoxPreference) findPreference(BATTERY_PERCENTAGE_KEY);
                    if (_cb == preference && newValue != null) {
                        showProgress(R.string.battery_percentage_progress);
                        new ThemeManager(getActivity()).restartLauncher(closeProgress);
                    }
                    return true;
                }
            });

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
                        showProgress(R.string.battery_percentage_progress);
                        new ThemeManager(getActivity()).restartLauncher(closeProgress);
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
        updateBatteryPercentage();
        updateVoiceCapable();
        updateSmsCapable();
        updateLauncherDrawer();
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
    private void updateBatteryPercentage() {
        mBatteryPercentage.setChecked(SystemProperties.getBoolean(BATTERY_PERCENTAGE_PROPERTY, false));
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

    private int getNumHomescreen() {
        int num = 2;
        String sNum = SystemProperties.get(MY_HOMESCREEN_PROPERTY, "5");
        int iNum = Integer.parseInt(sNum);
        switch(iNum) {
            case 1:
                num = 0;
                break;
            case 3:
                num = 1;
                break;
            case 5:
                num = 2;
                break;
            case 7:
                num = 3;
                break;
            default:
                num = 2;
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
        } else {
            density = defaultDensity;
        }

        SystemProperties.set(SELECT_DENSITY_PROPERTY, String.valueOf(density));
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

    private void writeNotificationOptions() {
        SystemProperties.set(MY_NOTIFICATION_PROPERTY, mNotification.isChecked() ? "true" : "false");
        showProgress(R.string.notification_progress);
        new ThemeManager(getActivity()).restartLauncher(closeProgress);
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
            confirmResetForUnetTheme();
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
        showProgress(R.string.launcher_landscape_progress);
        new ThemeManager(getActivity()).restartLauncher(closeProgress);
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

    private void writeBatteryPercentage() {
        SystemProperties.set(BATTERY_PERCENTAGE_PROPERTY, mBatteryPercentage.isChecked() ? "true" : "false");
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

    @Override
    public void onClickFileList(File file) {
        if(file != null) {
            SystemProperties.set(MY_THEME_PROPERTY, removeFileExtension(file.getName()));
            mTheme.setSummary(removeFileExtension(file.getName()));
            confirmResetForSetTheme(file);
        }
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
        } else if (preference == mBatteryPercentage) {
            writeBatteryPercentage();
        } else if (preference == mVoiceCapable) {
            writeVoiceCapable();
        } else if (preference == mSmsCapable) {
            writeSmsCapable();
        } else if (preference == mLauncherDrawer) {
            writeLauncherDrawer();
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
        return false;
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

    private void confirmResetForSetTheme(final File file) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean performReset = false;
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    performReset = true;
                }
                showProgress(R.string.progress_set_theme);
                new ThemeManager(getActivity()).setTheme(removeFileExtension(file.getName()), closeProgress, performReset);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.set_theme_confirm_reboot);
        builder.setPositiveButton(R.string.set_theme_confirm_yes, listener);
        builder.setNegativeButton(R.string.set_theme_confirm_no, listener);
        builder.show();
    }

    private void confirmResetForUnetTheme() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                boolean performReset = false;
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    performReset = true;
                }
                showProgress(R.string.progress_clear_theme);
                new ThemeManager(getActivity()).clearTheme(closeProgress, performReset);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.unset_theme_confirm_reboot);
        builder.setPositiveButton(R.string.set_theme_confirm_yes, listener);
        builder.setNegativeButton(R.string.set_theme_confirm_no, listener);
        builder.show();
    }

}
