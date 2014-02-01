
package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import com.android.internal.view.RotationPolicy;
import java.io.File;

public class ThemeClearIntentActivity extends Activity 
	implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

	private static final String MY_THEME_PROPERTY = "persist.sys.theme";
    private static final String MY_HOBBY_PROPERTY = "persist.sys.force.hobby";
    private static final String THEME_LOCK = "persist.sys.theme.lock";
	private Activity mActivity = this;
	private ProgressDialog mProgressDialog;
	private AlertDialog mConfirmDialog;

	@Override
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		setContentView(R.layout.theme_selector_intent_activity);

		Intent intent = getIntent();
		String packageName = intent.getStringExtra("package_name");
		String fromSelectorValue = intent.getStringExtra("manual_reset");

		if(null != packageName) {
			String property_name = "persist.sys." + packageName.substring(16);
    	    String uninstallTheme = SystemProperties.get(property_name);
        	String currentTheme = SystemProperties.get(MY_THEME_PROPERTY);
        	deleteThemeFile(uninstallTheme);
        	deleteThemeInfo(uninstallTheme);
        	SystemProperties.set(property_name, "");
	        if(currentTheme.equals(uninstallTheme)) {
    	    	onButtonSelected(false);
        	}else {
        		finish();
        	}
        } else if((null != fromSelectorValue) && fromSelectorValue.equals("true")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.unset_theme_confirm_reboot);
			builder.setOnCancelListener(this);
			builder.setPositiveButton(R.string.set_theme_confirm_yes, this);
			builder.setNegativeButton(R.string.set_theme_confirm_no, this);
		
			mConfirmDialog = builder.create();
			mConfirmDialog.show();
        } else {
        	onButtonSelected(false);
        }
	}

	private void deleteThemeFile(String themeName) {
		StringBuilder ibuilder = new StringBuilder();
		ibuilder.append(Environment.getExternalStorageDirectory().toString() + "/mytheme/" + themeName + ".jc");
		String deleteFileName = ibuilder.toString();
		File deleteFile = new File(deleteFileName);
		deleteFile.delete();
	}

    private void deleteThemeInfoFile(File iDir) {
        if (iDir.isDirectory()) {
            String[] children = iDir.list();
            for (int i = 0; i < children.length; i++) {
                File iFile = new File(iDir, children[i]);
                iFile.delete();
            }
        }
    }

	private void deleteThemeInfo(String uninstallTheme) {
		StringBuilder ibuilder = new StringBuilder();
		ibuilder.append(Environment.getExternalStorageDirectory().toString() + "/.mytheme/" + uninstallTheme + "/");
		String deleteDirName = ibuilder.toString();
		File deleteDir = new File(deleteDirName);
		deleteThemeInfoFile(deleteDir);
		deleteDir.delete();
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
            SystemProperties.set(THEME_LOCK, "false");
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                finish();
            }
        }
    };

	private void onButtonSelected(boolean performReset){
		showProgress(R.string.progress_clear_theme);
		setThemeClear();
		new ThemeManager(mActivity).clearTheme(closeProgress, performReset);
	}

	private void setThemeClear() {
		SystemProperties.set(MY_HOBBY_PROPERTY, "false");
		SystemProperties.set(MY_THEME_PROPERTY, "");
	}

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
		}
	}

	@Override
	public void onCancel(DialogInterface dialog){
		finish();
	}

}
