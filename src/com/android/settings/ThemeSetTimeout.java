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
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import com.android.internal.view.RotationPolicy;
import android.util.Slog;

public class ThemeSetTimeout{
    private static final String TAG = "ThemeSetTimeout";
    private static final String FORCE_ROTATION_LOCK = "persist.sys.force.lock";
    private static final String THEME_LOCK = "persist.sys.theme.lock";

    public void setTimeout(final Activity activity, long timeout){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String rotationLock = SystemProperties.get(FORCE_ROTATION_LOCK, "none");
                if(rotationLock.equals("true")) {
                    RotationPolicy.setRotationLock(activity, true);
                }else if(rotationLock.equals("false")) {
                    RotationPolicy.setRotationLock(activity, false);
                }
                SystemProperties.set(FORCE_ROTATION_LOCK, "none");
                SystemProperties.set(THEME_LOCK, "false");

            }
        }, timeout);
    }
}

