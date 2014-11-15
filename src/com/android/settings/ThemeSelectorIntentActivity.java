
package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import com.android.internal.view.RotationPolicy;

public class ThemeSelectorIntentActivity extends Activity
	implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener{

	private static final String MY_THEME_PROPERTY = "persist.sys.theme";
	private static final String MY_HOBBY_PROPERTY = "persist.sys.force.hobby";
	private static final String FORCE_ROTATION_LOCK = "persist.sys.force.lock";
	private static final String THEME_LOCK = "persist.sys.theme.lock";
	private static final int JC_LIMIT = (72 * 1000);

	private Activity mActivity = this;
	private ProgressDialog mProgressDialog;
	private String newTheme;
	private AlertDialog mConfirmDialog;

	@Override
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		setContentView(R.layout.theme_selector_intent_activity);

		if(SystemProperties.getBoolean(MY_HOBBY_PROPERTY, false)){
			Intent intent = getIntent();
			String[] strs = (getIntent().getData().toString()).split("\\+");
			if((strs.length == 4) && (strs[1].equals("jcrom.new.theme"))) {
				newTheme = strs[2];
			} else {
				newTheme = intent.getStringExtra("jcrom.new.theme");
			}
			setThemeManager();
		}else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.theme_disabled_title);
			builder.setMessage(R.string.theme_disabled_message);
			builder.setOnCancelListener(this);
			builder.setPositiveButton(R.string.theme_disabled_dialog_accept, this);
			builder.setNegativeButton(R.string.theme_disabled_dialog_decline, this);
			mConfirmDialog = builder.create();
			mConfirmDialog.show();
		}
	}

	private void showProgress(int resid) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(resid));
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	private final Runnable closeProcess = new Runnable() {
		@Override
		public void run() {
			SystemProperties.set(THEME_LOCK, "false");
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;

				setResult(RESULT_OK);
                revertRotationLock();

                Intent intent = new Intent();
                intent.setClassName("com.android.launcher3", "com.android.launcher3.Launcher");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

				finish();
			}
	    }
	};

	@Override
	public void onClick(DialogInterface dialog, int which){
		
		if(mConfirmDialog == dialog) {
			switch(which) {
				case DialogInterface.BUTTON1:
					Intent intent = new Intent();
					intent.setAction("android.settings.JAPANESE_CUSTOM_ROM_SETTINGS");
					startActivity(intent);
					break;
				case DialogInterface.BUTTON2:
					setResult(RESULT_CANCELED);
					finish();
					break;
			}
		}
	}

	private void setThemeManager(){

		boolean rotationLock = RotationPolicy.isRotationLocked(mActivity);

		if(rotationLock) {
			SystemProperties.set(FORCE_ROTATION_LOCK, "true");
		}else {
			SystemProperties.set(FORCE_ROTATION_LOCK, "false");
		}

        RotationPolicy.setRotationLock(mActivity, true);

        showProgress(R.string.progress_set_theme);

		SystemProperties.set(MY_THEME_PROPERTY, newTheme);
		SystemProperties.set(THEME_LOCK, "true");

        ThemeSetTimeout themeTimeout = new ThemeSetTimeout();
        themeTimeout.setTimeout(mActivity, JC_LIMIT);

		new ThemeManager(mActivity).setTheme(newTheme, closeProcess);
	}

	@Override
	public void onCancel(DialogInterface dialog){
		setResult(RESULT_CANCELED);
		finish();
	}

	private void revertRotationLock() {
		String rotationLock = SystemProperties.get(FORCE_ROTATION_LOCK, "none");
		if(rotationLock.equals("true")) {
			RotationPolicy.setRotationLock(mActivity, true);
		}else if(rotationLock.equals("false")) {
			RotationPolicy.setRotationLock(mActivity, false);
		}
		SystemProperties.set(FORCE_ROTATION_LOCK, "none");
		SystemProperties.set(THEME_LOCK, "false");
	}
}
