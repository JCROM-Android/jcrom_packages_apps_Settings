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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import java.io.File;

public class ThemeClearIntentActivity extends Activity {

	private static final String MY_THEME_PROPERTY = "persist.sys.theme";
    private static final String MY_HOBBY_PROPERTY = "persist.sys.force.hobby";
    private static final String THEME_LOCK = "persist.sys.theme.lock";
	private Activity mActivity = this;
	private ProgressDialog mProgressDialog;

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
    	    	setThemeClear();
        	}else {
        		finish();
        	}
        } else {
        	setThemeClear();
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

    private final Runnable closeProcess = new Runnable() {
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

                finish();
            }
        }
    };

	private void setThemeClear() {
		showProgress(R.string.progress_clear_theme);
		SystemProperties.set(MY_HOBBY_PROPERTY, "false");
		SystemProperties.set(MY_THEME_PROPERTY, "");
		new ThemeManager(mActivity).clearTheme(closeProcess);
	}
}
