
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

