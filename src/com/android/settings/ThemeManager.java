
package com.android.settings;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ThemeManager {

    private static final String sThemeDirs[] = {
            "bootanime",
            "frame",
            "launcher",
            "lockscreen",
            "navikey",
            "notification",
            "screenshot",
            "statusbar",
            "navibar",
            "simeji",
            "sounds/effect",
            "sounds/bootsound",
            "wallpaper",
            "font",
    };

    private Context mContext;
    private ContentResolver mContentResolver;
    private WallpaperManager mWallpaperManager;
    private WindowManager mWindowManager;
    private Handler mHandler;

    public ThemeManager(Activity activity) {
        mContext = activity;
        mContentResolver = mContext.getContentResolver();
        mWallpaperManager = WallpaperManager.getInstance(mContext);
        mWindowManager = activity.getWindowManager();
        mHandler = new Handler();
    }

    public void restartLauncher(final Runnable afterProc) {
        restartSystemUI(new Runnable() {
            public void run() {
                String forceHobby = SystemProperties.get("persist.sys.force.hobby");
                if (forceHobby.equals("true")) {
                    applyTheme();
                }
                if (afterProc != null) {
                    afterProc.run();
                }
            }
        });
    }

    public void setTheme(String themeName, final Runnable afterProc) {
        new Thread(new Runnable() {
            public void run() {
                themeAllClear();
                themeAllInstall();

                setDefaultSounds();
                setMySounds();

                restartSystemUI(new Runnable() {
                    public void run() {
                        applyTheme();
                        if (afterProc != null) {
                            afterProc.run();
                        }
                    }
                });
            }
        }).start();
    }

    public void clearTheme(final Runnable afterProc) {
        setDefaultSounds();
        themeAllClear();

        restartSystemUI(new Runnable() {
            public void run() {
                try {
                    mWallpaperManager.clear();
                } catch (IOException e) {
                }
                if (afterProc != null) {
                    afterProc.run();
                }
            }
        });
    }

    private void setDefaultSounds() {
        Settings.Global.putString(mContentResolver, Settings.Global.LOW_BATTERY_SOUND,
                "/system/media/audio/ui/LowBattery.ogg");
        Settings.Global.putString(mContentResolver, Settings.Global.DESK_DOCK_SOUND,
                "/system/media/audio/ui/Dock.ogg");
        Settings.Global.putString(mContentResolver, Settings.Global.DESK_UNDOCK_SOUND,
                "/system/media/audio/ui/Undock.ogg");
        Settings.Global.putString(mContentResolver, Settings.Global.CAR_DOCK_SOUND,
                "/system/media/audio/ui/Dock.ogg");
        Settings.Global.putString(mContentResolver, Settings.Global.CAR_UNDOCK_SOUND,
                "/system/media/audio/ui/Undock.ogg");
        Settings.Global.putString(mContentResolver, Settings.Global.LOCK_SOUND,
                "/system/media/audio/ui/Lock.ogg");
        Settings.Global.putString(mContentResolver, Settings.Global.UNLOCK_SOUND,
                "/system/media/audio/ui/Unlock.ogg");
    }

    private void setDataBase(String key, String name) {
        StringBuilder builder = new StringBuilder();
        //builder.append(Environment.getExternalStorageDirectory().toString() + "/mytheme/" + SystemProperties.get("persist.sys.theme") + "/sounds/effect/");
        builder.append(Environment.getDataDirectory().toString() + "/theme/sounds/effect/");
        builder.append(File.separator);
        builder.append(name);
        String filePath = builder.toString();
        File file = new File(filePath);
        if (file.exists()) {
            Settings.Global.putString(mContentResolver, key, filePath);
        }
    }

    private void setMySounds() {
        String forceHobby = SystemProperties.get("persist.sys.force.hobby");
        if (forceHobby.equals("true")) {
            setDataBase(Settings.Global.LOW_BATTERY_SOUND, "LowBattery.ogg");
            setDataBase(Settings.Global.DESK_DOCK_SOUND, "Dock.ogg");
            setDataBase(Settings.Global.DESK_UNDOCK_SOUND, "UnDock.ogg");
            setDataBase(Settings.Global.CAR_DOCK_SOUND, "CarDock.ogg");
            setDataBase(Settings.Global.CAR_UNDOCK_SOUND, "UnCarDock.ogg");
            setDataBase(Settings.Global.LOCK_SOUND, "Lock.ogg");
            setDataBase(Settings.Global.UNLOCK_SOUND, "unLock.ogg");
        }
    }

    public void themeCopy(File iDir, File oDir) {
        if (iDir.isDirectory()) {
            String[] children = iDir.list();
            for (int i = 0; i < children.length; i++) {
                File iFile = new File(iDir, children[i]);
                File oFile = new File(oDir, children[i]);

                try {
                    FileChannel iChannel = new FileInputStream(iFile).getChannel();
                    FileChannel oChannel = new FileOutputStream(oFile).getChannel();
                    iChannel.transferTo(0, iChannel.size(), oChannel);
                    iChannel.close();
                    oChannel.close();
                    oFile.setReadable(true, false);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void themeInstall(String parts) {
        StringBuilder ibuilder = new StringBuilder();
        StringBuilder obuilder = new StringBuilder();
        ibuilder.append(Environment.getExternalStorageDirectory().toString() + "/mytheme/" + SystemProperties.get("persist.sys.theme") + "/" + parts + "/");
        obuilder.append(Environment.getDataDirectory().toString() + "/theme/" + parts + "/");
        String iDirPath = ibuilder.toString();
        String oDirPath = obuilder.toString();
        File iDir = new File(iDirPath);
        File oDir = new File(oDirPath);
        themeCopy(iDir, oDir);
    }

    public void themeAllInstall() {
        for (String dir : sThemeDirs) {
            themeInstall(dir);
        }
    }

    public void themeDelete(File iDir) {
        if (iDir.isDirectory()) {
            String[] children = iDir.list();
            for (int i = 0; i < children.length; i++) {
                File iFile = new File(iDir, children[i]);
                iFile.delete();
            }
        }
    }

    public void themeClear(String parts) {
        StringBuilder ibuilder = new StringBuilder();
        ibuilder.append(Environment.getDataDirectory().toString() + "/theme/" + parts + "/");
        String iDirPath = ibuilder.toString();
        File iDir = new File(iDirPath);
        themeDelete(iDir);
    }

    public void themeAllClear() {
        for (String dir : sThemeDirs) {
            themeClear(dir);
        }
    }

    private void restartSystemUI(final Runnable postproc) {
        mHandler.post(new Runnable() {
            public void run() {
                try {
                    ActivityManager am = (ActivityManager) mContext
                            .getSystemService(Context.ACTIVITY_SERVICE);
                    am.forceStopPackage("com.android.launcher");
                    Intent jcservice = (new Intent())
                            .setClassName("com.android.systemui",
                                    "com.android.systemui.JcromService");
                    mContext.startActivity(jcservice);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.postDelayed(postproc, 7500/* ms */);
            }
        });
    }

    private void applyTheme() {
        Bitmap bitmapWallpaper;
        String MY_FRAME_FILE = "home_wallpaper.png";
        StringBuilder builder = new StringBuilder();
        //builder.append(Environment.getExternalStorageDirectory().toString() + "/mytheme/" + SystemProperties.get("persist.sys.theme") + "/wallpaper/");
        builder.append(Environment.getDataDirectory().toString() + "/theme/wallpaper/");
        builder.append(File.separator);
        builder.append(MY_FRAME_FILE);
        String filePath = builder.toString();
        bitmapWallpaper = BitmapFactory.decodeFile(filePath);
        if (null != bitmapWallpaper) {
            try {
                int srcWidth = bitmapWallpaper.getWidth();
                int srcHeight = bitmapWallpaper.getHeight();

                int screenSize = mContext.getResources().getConfiguration().screenLayout
                        & Configuration.SCREENLAYOUT_SIZE_MASK;
                boolean isScreenLarge = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE
                        || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
                DisplayMetrics displayMetrics = new DisplayMetrics();
                mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
                int maxDim = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
                int minDim = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                float WALLPAPER_SCREENS_SPAN = 2f;
                int w, h;
                if (isScreenLarge) {
                    w = (int) (maxDim * wallpaperTravelToScreenWidthRatio(maxDim, minDim));
                    h = maxDim;
                } else {
                    w = Math.max((int) (minDim * WALLPAPER_SCREENS_SPAN), maxDim);
                    h = maxDim;
                }

                if (w < srcWidth && h < srcHeight) {
                    Matrix matrix = new Matrix();
                    float widthScale = w / (float) srcWidth;
                    float heightScale = h / (float) srcHeight;
                    matrix.postScale(widthScale, heightScale);
                    Bitmap resizedWallpaper = Bitmap.createBitmap(bitmapWallpaper, 0, 0, srcWidth,
                            srcHeight, matrix, true);
                    mWallpaperManager.setBitmap(resizedWallpaper);
                } else {
                    mWallpaperManager.setBitmap(bitmapWallpaper);
                }
            } catch (IOException e) {
            }
        }
    }

    // borrowed from "com/android/launcher2/Workspace.java"
    private float wallpaperTravelToScreenWidthRatio(int width, int height) {

        float aspectRatio = width / (float) height;

        final float ASPECT_RATIO_LANDSCAPE = 16 / 10f;
        final float ASPECT_RATIO_PORTRAIT = 10 / 16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        final float x =
                (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT)
                        / (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }
}
