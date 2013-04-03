
package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;


public class ThemeSelectorIntentActivity extends Activity
	implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener{

	private static final String MY_THEME_PROPERTY = "persist.sys.theme";
	private static final String MY_HOBBY_PROPERTY = "persist.sys.force.hobby";

	private Activity mActivity = this;
	private ProgressDialog mProgressDialog;
	private String newTheme;
	private AlertDialog mConfirmDialog;
	private AlertDialog mErrorDialog;

	@Override
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		setContentView(R.layout.theme_selector_intent_activity);

		if(SystemProperties.getBoolean(MY_HOBBY_PROPERTY, false)){

			Intent intent = getIntent();
			newTheme = intent.getStringExtra("jcrom.new.theme");

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.set_theme_confirm_reboot);
			builder.setOnCancelListener(this);
			builder.setPositiveButton(R.string.set_theme_confirm_yes, this);
			builder.setNegativeButton(R.string.set_theme_confirm_no, this);
		
			mConfirmDialog = builder.create();
			mConfirmDialog.show();
		}else{

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.theme_disabled_title);
			builder.setMessage(R.string.theme_disabled_message);
			builder.setOnCancelListener(this);
			builder.setPositiveButton(R.string.theme_disabled_dialog_accept, this);
			builder.setNegativeButton(R.string.theme_disabled_dialog_decline, this);

			mErrorDialog = builder.create();
			mErrorDialog.show();
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

	private final Runnable closeProgress = new Runnable() {
		@Override
		public void run() {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;

				setResult(RESULT_OK);
				finish();
			}
	        }
	};

	@Override
	public void onClick(DialogInterface dialog, int which){
		
		if(mConfirmDialog == dialog){
			boolean performReset = false;
		
			switch(which){
			case DialogInterface.BUTTON1:
				performReset = true;
				onButtonSelected(performReset);
				break;
	
			case DialogInterface.BUTTON2:
				onButtonSelected(performReset);
				break;
			}
		}else{
			switch(which){
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

	private void onButtonSelected(boolean performReset){

		showProgress(R.string.progress_set_theme);

		SystemProperties.set(MY_THEME_PROPERTY, newTheme);
		new ThemeManager(mActivity).setTheme(newTheme, closeProgress, performReset);
	}

	@Override
	public void onCancel(DialogInterface dialog){
		setResult(RESULT_CANCELED);
		finish();
	}
}
