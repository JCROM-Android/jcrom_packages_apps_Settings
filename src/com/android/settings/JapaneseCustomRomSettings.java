
package com.android.settings;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.EditText;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;

public class JapaneseCustomRomSettings extends PreferenceFragment
        implements OnPreferenceChangeListener, FileListDialog.onFileListDialogListener {

    private static final String SELECT_UI_PROPERTY = "persist.sys.ui.select";
    private static final String ACTIONBAR_BOTTOM_PROPERTY = "persist.sys.actionbar.bottom";
    private static final String MY_FONT_PROPERTY = "persist.sys.force.myfont";
    private static final String MY_HOBBY_PROPERTY = "persist.sys.force.hobby";
    private static final String MY_THEME_PROPERTY = "persist.sys.theme";
    private static final String MY_SEFFECTS_PROPERTY = "persist.sys.sound.effects";
    private static final String MY_WALLPAPER_PROPERTY = "persist.sys.fixed.wallpaper";
    private static final String MY_HOMESCREEN_PROPERTY = "persist.sys.num.homescreen";
    private static final String MY_GRADIENT_PROPERTY = "persist.sys.prop.gradient";

    private static final String SELECT_UI_KEY = "select_ui";
    private static final String ACTIONBAR_BOTTOM_KEY = "actionbar_bottom";
    private static final String FORCE_MY_FONT_KEY = "force_my_font";
    private static final String FORCE_MY_HOBBY_KEY = "force_my_hobby";
    private static final String THEME_KEY = "theme_setting";
    private static final String DEVINFO_KEY = "jcrom_developer";
    private static final String FORCE_FIXED_WALLPAPER = "force_fixed_wallpaper";
    private static final String NUM_OF_HOMESCREEN = "number_of_homescreen";
    private static final String FORCE_MY_ANDROID_ID_KEY = "force_my_android_id";
    private static final String GRADIENT_KEY = "gradient_setting";
    private static final String FORCE_MY_SIM_KEY = "force_my_sim";

    private static final String TAG = "JapaneseCustomRomSettings";

    private final ArrayList<Preference> mAllPrefs = new ArrayList<Preference>();
    private ListPreference mSelectUi;
    private CheckBoxPreference mActionBarBottom;
    private CheckBoxPreference mForceMyFont;
    private CheckBoxPreference mForceMyHobby;
    private PreferenceScreen mTheme;
    private CheckBoxPreference mFixedWallpaper;
    private ListPreference mNumHomescreen;
    private PreferenceScreen mForceMyAndroidId;
    private CheckBoxPreference mGradientStat;
    private ProgressDialog mProgressDialog;
    private PreferenceScreen mForceMySIM;

    private String mAndroidId;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.jcrom_settings);
        mSelectUi = (ListPreference) findPreference(SELECT_UI_KEY);
        mAllPrefs.add(mSelectUi);
        mSelectUi.setOnPreferenceChangeListener(this);
        selectUi();
        mActionBarBottom = (CheckBoxPreference) findPreference(ACTIONBAR_BOTTOM_KEY);
        mForceMyFont = (CheckBoxPreference) findPreference(FORCE_MY_FONT_KEY);
        mForceMyHobby = (CheckBoxPreference) findPreference(FORCE_MY_HOBBY_KEY);
        mTheme = (PreferenceScreen) findPreference(THEME_KEY);
        mFixedWallpaper = (CheckBoxPreference) findPreference(FORCE_FIXED_WALLPAPER);
        mNumHomescreen = (ListPreference) findPreference(NUM_OF_HOMESCREEN);
        mForceMyAndroidId = (PreferenceScreen) findPreference(FORCE_MY_ANDROID_ID_KEY);
        mGradientStat = (CheckBoxPreference) findPreference(GRADIENT_KEY);
        mForceMySIM = (PreferenceScreen) findPreference(FORCE_MY_SIM_KEY);

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

                                 //Log.e(TAG, "ScreenNum" + screenNum);
                            _list.setSummary(screenNum);
                            writeNumberofScreenOptions(screenNum);

                            try {
                                      ActivityManager am = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
                                am.forceStopPackage("com.android.launcher");
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
    }

    @Override
    public void onStop() {
        super.onStop();

        closeProgress.run();
    }

    private void selectUi() {
        int select = SystemProperties.getInt(SELECT_UI_PROPERTY, -1);
        if(select != -1) {
            mSelectUi.setValueIndex(select);
            mSelectUi.setSummary(mSelectUi.getEntries()[select]);
        }
    }

    private void writeActionBarBottomOptions() {
        SystemProperties.set(ACTIONBAR_BOTTOM_PROPERTY, mActionBarBottom.isChecked() ? "true" : "false");
    }

    private void updateMyFontOptions() {
        mForceMyFont.setChecked(SystemProperties.getBoolean(MY_FONT_PROPERTY, false));
    }

    private void writeMyFontOptions() {
        SystemProperties.set(MY_FONT_PROPERTY, mForceMyFont.isChecked() ? "true" : "false");
    }

    private void updateMyHobbyOptions() {
        mForceMyFont.setChecked(SystemProperties.getBoolean(MY_HOBBY_PROPERTY, false));
    }

    private void writeMyHobbyOptions() {
        SystemProperties.set(MY_HOBBY_PROPERTY, mForceMyHobby.isChecked() ? "true" : "false");
        if (!(mForceMyHobby.isChecked())) {
            SystemProperties.set(MY_THEME_PROPERTY, "");
            mTheme.setSummary("");

            showProgress(R.string.progress_clear_theme);
            new ThemeManager(getActivity()).clearTheme(closeProgress);
        }
    }

    private void updateMyWallpaperOptions() {
        mFixedWallpaper.setChecked(SystemProperties.getBoolean(MY_WALLPAPER_PROPERTY, false));
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
        // Log.e(TAG, "Gradient setting changed");
    }

    @Override
    public void onClickFileList(File file) {
        if(file != null) {
            SystemProperties.set(MY_THEME_PROPERTY, file.getName());
            mTheme.setSummary(file.getName());

            showProgress(R.string.progress_set_theme);
            new ThemeManager(getActivity()).setTheme(file.getName(), closeProgress);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mActionBarBottom) {
            writeActionBarBottomOptions();
        } else if (preference == mForceMyFont) {
            writeMyFontOptions();
        } else if (preference == mForceMyHobby) {
            writeMyHobbyOptions();
        } else if (preference == mTheme) {
            if(mForceMyHobby.isChecked()) {
                FileListDialog dlg = new FileListDialog(getActivity());
                dlg.setOnFileListDialogListener(this);
                dlg.show( "/sdcard/mytheme/", "select theme");
            }
        } else if (preference == mFixedWallpaper) {
            writeMyWallpaperOptions();
        } else if (preference == mForceMyAndroidId) {
            showNewAndroidIdDialog();
        } else if (preference == mGradientStat) {
            writeGradientOptions();
        } else if (preference == mForceMySIM) {
            JapaneseCustomRomSimState.makeDialog(getActivity()).show();
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
}
