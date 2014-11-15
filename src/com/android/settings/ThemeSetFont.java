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
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.internal.view.RotationPolicy;
import android.util.Slog;
import java.util.Locale;

public class ThemeSetFont {
    private static final String TAG = "ThemeSetFont";
    private static final String FORCE_ROTATION_LOCK = "persist.sys.force.lock";
    private static final String THEME_LOCK = "persist.sys.theme.lock";
    private Handler mHandler = new Handler();
    private Context mContext;

    public void setTimeout(final Context context, long timeout){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int guard_count = 0;
                while (SystemProperties.getBoolean(THEME_LOCK, true)) {
                    try {
                        Thread.sleep(100);
                        guard_count++;
                    } catch(Exception e) {
                    }
                    if(guard_count > 100) {
                        break;
                    }
                }
                mContext = context;
                mHandler.postDelayed(setThemeFont, 2000);
            }
        }, timeout);
    }

    private final Runnable setThemeFont = new Runnable() {
        @Override
        public void run() {
            reflectionFont(mContext);
        }
    };

    private void reflectionFont(Context context) {
        Configuration conf = context.getResources().getConfiguration();
        Locale loc = conf.locale;
        Locale mLocale = loc;
        loc = new Locale("jc", "JC");
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            config.locale = loc;
            config.userSetLocale = true;
            am.updateConfiguration(config);
        } catch (RemoteException e) {
        }
        loc = mLocale;
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            config.locale = loc;
            config.userSetLocale = true;
            am.updateConfiguration(config);
        } catch (RemoteException e) {
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.forceStopPackage("com.android.settings");
    }

}

