/*
 * Copyright (C) 2013 JCROM Project
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.pm.ActivityInfo;
import android.content.DialogInterface;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SoftwareUpdate extends Fragment {
    private static final String TAG = "SoftwareUpdate";
    private static final int KEYGUARD_REQUEST = 55;
    private static final String FILE_LOCAL_PATH = "/cache/jcrom.zip";
    private static final String FILE_LOCAL_TMP_PATH = "/cache/jcrom.zip.tmp";
    private static final String FILE_LOCAL_MD5_PATH = "/cache/jcrom.zip.md5sum";
    private static final String FILE_LOCAL_MD5_TMP_PATH = "/cache/jcrom.zip.md5sum.tmp";
    private static final String FILE_LOCAL_GAPPS_PATH = "/cache/gapps.zip";
    private static final String FILE_LOCAL_GAPPS_TMP_PATH = "/cache/gapps.zip.tmp";
    private static final String KEY_JCROM_VERSION = "ro.jcrom.version";
    private static final String URL_JCROM_UPDATE = "http://jcrom.net/release/aosp/jcrom_update_lolli.xml";
    private static final int INDEX_VERSION = 0;
    private static final int INDEX_LINK = 1;
    private static final int INDEX_MD5 = 2;

    private View mContentView;
    private Button mInitiateButton;
    private Button mCheckButton;
    private String jcrom_version = "";
    private String jcrom_link = "";
    private String md5_link = "";
    private String gapps_link = "";
    private PowerManager pm = null;
    private WakeLock lock = null;
    private boolean mWakeLockFlag = false;

    String mUpdateInfo[][] = {{"gn_version",  "gn_link",  "gn_md5" },
                              {"n7_version",  "n7_link",  "n7_md5" },
                              {"n4_version",  "n4_link",  "n4_md5" },
                              {"n10_version", "n10_link", "n10_md5"},
                              {"ns_version",  "ns_link",  "ns_md5" },
                              {"n72_version", "n72_link", "n72_md5"},
                              {"n5_version",  "n5_link",  "n5_md5" },
                              {"n6_version",  "n6_link",  "n6_md5" },
                              {"n9_version",  "n9_link",  "n9_md5" }};
    List<String> mList = 
        new ArrayList<String>(Arrays.asList("yakju", "nakasi", "occam", 
                                            "mantaray", "soju", "razor", 
                                            "hammerhead", "shamu", "volantis"));

    private String getModelInfo(int info) {
        String model = Build.PRODUCT;
        String data = null;
        int index = mList.indexOf(model);

        if (index != -1) {
            data = mUpdateInfo[index][info];
        }

        return data;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }
        establishInitialState();
    }

    private void showFinalConfirmation() {
        getActivity().sendBroadcast(new Intent("android.intent.action.SOFTWARE_UPDATE"));
    }

    private final Button.OnClickListener mInitiateListener = new Button.OnClickListener() {
        public void onClick(View v) {
            clearDownloadData();
            AsyncDownloadTask task = new AsyncDownloadTask ();
            task.execute (0);
        }
    };

    private final Button.OnClickListener mCheckListener = new Button.OnClickListener() {
        public void onClick(View v) {
            AsyncDownloadTask2 task = new AsyncDownloadTask2 ();
            task.execute (0);
        }
    };

    private void establishInitialState() {
        TextView textView1 = (TextView)mContentView.findViewById(R.id.software_update_text1);
        TextView textView2 = (TextView)mContentView.findViewById(R.id.software_update_text2);
        String str = SystemProperties.get(KEY_JCROM_VERSION);
        Resources res = getResources();
        textView1.setText(res.getString(R.string.jcrom_current) + " " +  str);
        textView2.setText(res.getString(R.string.jcrom_latest) + " " + jcrom_version);
        mInitiateButton = (Button)mContentView.findViewById(R.id.initiate_software_update);
        mInitiateButton.setOnClickListener(mInitiateListener);
        mCheckButton = (Button)mContentView.findViewById(R.id.check_software_update);
        mCheckButton.setOnClickListener(mCheckListener);
    }

    private void updateState() {
        TextView textView = (TextView)mContentView.findViewById(R.id.software_update_text2);
        Resources res = getResources();
        textView.setText(res.getString(R.string.jcrom_latest) + " " + jcrom_version);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.software_update, null);
        establishInitialState();
        String model = Build.MODEL;
        if(model.equals("Nexus 10")) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        lock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "JCROM");
        return mContentView;
    }

    private class AsyncDownloadTask extends AsyncTask <Integer, Integer, Integer> {
        @Override
        public void onPreExecute() {
            lock.acquire();
            mWakeLockFlag = true;
            this.progressDialog = new ProgressDialog(getActivity());
            this.progressDialog.setMessage("Downloading");
            this.progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener () {
                    public void onClick(DialogInterface dialog, int which) {  
                        AsyncDownloadTask.this.cancel(false);
                    }
                }  
            ); 
            this.progressDialog.setIndeterminate(false);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setMax(0);
            this.progressDialog.setProgress(0);
            this.progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
        
        public void downloadFile(String downloadfile, String downloadlink, boolean reboot) {
            File temporaryFile = new File (downloadfile + ".tmp");
            if(temporaryFile.exists()) {
                this.downloadFileSize = 0;
                this.downloadFileSizeCount = (int)temporaryFile.length();
            } else {
                this.downloadFileSize = 0;
                this.downloadFileSizeCount = 0;
                try { 
                    temporaryFile.createNewFile();
                } catch(IOException exception) {
                    exception.printStackTrace();
                }
            }

            Resources res = getResources();
            Log.d(TAG, downloadlink + ": " + downloadfile);
            boolean downloadComplete = false;
            boolean downloadCancel = false;

            try {
                {
                    URL url = new URL(downloadlink);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("HEAD");
                    httpURLConnection.connect();
                    if(httpURLConnection.getResponseCode() == 200) {
                        this.downloadFileSize = httpURLConnection.getContentLength();
                    }
                }
                {
                    URL url = new URL(downloadlink);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setRequestProperty("Range",
                        String.format("bytes=%d-%d", this.downloadFileSizeCount, this.downloadFileSize)
                    );
                    httpURLConnection.connect();
                    if((httpURLConnection.getResponseCode() == 200) || (httpURLConnection.getResponseCode() == 206)) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        FileOutputStream fileOutputStream = (new FileOutputStream(temporaryFile, true));
                        byte[] buffReadBytes = new byte[4096];
                        for(int sizeReadBytes = inputStream.read(buffReadBytes); sizeReadBytes != -1; sizeReadBytes = inputStream.read(buffReadBytes)) {
                            fileOutputStream.write(buffReadBytes, 0, sizeReadBytes);
                            this.downloadFileSizeCount += sizeReadBytes;
                            this.publishProgress(this.downloadFileSizeCount);
                            if(this.isCancelled()) {
                                downloadCancel = true;
                                if(mWakeLockFlag) {
                                    lock.release();
                                    mWakeLockFlag = false;
                                }
                                break;
                            }
                        }
                        downloadComplete = true;
                    }
                }
            } catch(MalformedURLException exception) { 
                exception.printStackTrace();
            } catch(ProtocolException exception) { 
                exception.printStackTrace();
            } catch(IOException exception) {
                exception.printStackTrace();
            } finally {
                if((true == downloadComplete) && (false == downloadCancel)) {
                    temporaryFile.renameTo(new File(downloadfile));
                    if (true == reboot) {
                        if(mWakeLockFlag) {
                            lock.release();
                            mWakeLockFlag = false;
                        }
                        showFinalConfirmation();
                    }
                }
            }
        }

        @Override
        public Integer doInBackground(Integer...ARGS) {
            downloadFile(FILE_LOCAL_PATH, jcrom_link, false);
            downloadFile(FILE_LOCAL_MD5_PATH, md5_link, false);
            downloadFile(FILE_LOCAL_GAPPS_PATH, gapps_link, true);
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            this.progressDialog.setMax(this.downloadFileSize);
            this.progressDialog.setProgress (this.downloadFileSizeCount);
        }

        @Override
        public void onPostExecute(Integer result) {
            if(null != this.progressDialog) {
                this.progressDialog.dismiss();
                this.progressDialog = null;
            }
            if(mWakeLockFlag) {
                lock.release();
                mWakeLockFlag = false;
            }
        }

        ProgressDialog progressDialog = null;
        Integer downloadFileSize = 0;
        Integer downloadFileSizeCount = 0;
    }

    private class AsyncDownloadTask2 extends AsyncTask <Integer, Integer, Integer> {
        @Override
        public Integer doInBackground(Integer...ARGS) {
            getUpdateInfo();
            return 0;
        }

        @Override
        public void onPostExecute(Integer result) {
            updateState();
        }
    }

    private void getUpdateInfo() {
        String uri = URL_JCROM_UPDATE;
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet();
        try {
            get.setURI(new URI(uri));
            HttpResponse res = client.execute(get);
            InputStream in = res.getEntity().getContent();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, "UTF-8");
            int eventType = parser.getEventType();
            jcrom_version = "";
            jcrom_link = "";
            md5_link = "";
            gapps_link = "";
            String version_name = getModelInfo(INDEX_VERSION);
            String link_name = getModelInfo(INDEX_LINK);
            String md5_name = getModelInfo(INDEX_MD5);
            String gapps_name = getGappsName();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tag = parser.getName();
                    if (version_name.equals(tag)) {
                        jcrom_version = parser.nextText();
                    } else if (link_name.equals(tag)) {
                    	jcrom_link = parser.nextText();
                    } else if (md5_name.equals(tag)) {
                        md5_link = parser.nextText();
                    } else if (gapps_name.equals(tag)) {
                        gapps_link = parser.nextText();
                    }
                    break;
                }
                if ( ! ("".equals(jcrom_version) || "".equals(jcrom_link) || "".equals(md5_link) || "".equals(gapps_link))) {
                    break;
                }
                eventType = parser.next();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private String getGappsName() {
        return "gapps_link";
    }

    private void clearDownloadData() {
        clearFile(FILE_LOCAL_PATH);
        clearFile(FILE_LOCAL_TMP_PATH);
        clearFile(FILE_LOCAL_MD5_PATH);
        clearFile(FILE_LOCAL_MD5_TMP_PATH);
        clearFile(FILE_LOCAL_GAPPS_PATH);
        clearFile(FILE_LOCAL_GAPPS_TMP_PATH);
    }

    private void clearFile(String filename) {
        File deleteFile;
        deleteFile = new File(filename);
        deleteFile.delete();
    }
}

