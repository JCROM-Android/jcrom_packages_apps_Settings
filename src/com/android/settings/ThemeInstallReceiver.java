package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;

public class ThemeInstallReceiver extends BroadcastReceiver {

	private static final String TAG = "ThemeInstallReceiver";
	private static final String JCROM_THEME_PKG = "net.jcrom.theme";

	@Override
	public void onReceive
	(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals("android.intent.action.JcromThemeInstall")) {
			String package_name = intent.getStringExtra("package");
			String theme_name = intent.getStringExtra("theme");
			String property_name = "persist.sys." + package_name.substring(16);
        	SystemProperties.set(property_name, theme_name);
		} else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			if(pkgName.length() >= 16) {
				String checkName = pkgName.substring(0,15);
				if(checkName.equals(JCROM_THEME_PKG)) {
					String themeName = pkgName.substring(16);
					Intent in = new Intent();
					in.setClassName(pkgName, pkgName + ".JcromThemeInstaller");
					in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			context.startActivity(in);
				}
			}
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			Uri data = intent.getData();
			String pkgName = data.getEncodedSchemeSpecificPart();
			if(pkgName.length() >= 16) {
				String checkName = pkgName.substring(0,15);
				if(checkName.equals(JCROM_THEME_PKG)) {
					Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse("jcrom:///clear_theme"));
					in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					in.putExtra("package_name", pkgName);
					context.startActivity(in);
				}
			}
		}
	}

}
