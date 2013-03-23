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
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.DialogInterface;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;

public class SoftwareUpdate extends Fragment {
    private static final String TAG = "SoftwareUpdate";
    private static final int KEYGUARD_REQUEST = 55;
    private View mContentView;
    private Button mInitiateButton;
    static String DOWNLOAD_FILE_URL = null;
    static final String FILE_LOCAL_PATH = "/cache/jcrom.zip";

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
            AsyncDownloadTask task = new AsyncDownloadTask ();
            task.execute (0);
        }
    };

    private void establishInitialState() {
        mInitiateButton = (Button)mContentView.findViewById(R.id.initiate_software_update);
        mInitiateButton.setOnClickListener(mInitiateListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.software_update, null);
        establishInitialState();
        return mContentView;
    }

    private class AsyncDownloadTask extends AsyncTask <Integer, Integer, Integer> {
        @Override
        public void onPreExecute() {
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
        
        @Override
        public Integer doInBackground(Integer...ARGS) {
            File temporaryFile = new File (FILE_LOCAL_PATH + ".tmp");
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
            DOWNLOAD_FILE_URL = res.getString(R.string.software_update_url);

            boolean downloadComplete = false;
            boolean downloadCancel = false;
            try {
                {
                    URL url = new URL(DOWNLOAD_FILE_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("HEAD");
                    httpURLConnection.connect();
                    if(httpURLConnection.getResponseCode() == 200) {
                        this.downloadFileSize = httpURLConnection.getContentLength();
                    }
                }
                {
                    URL url = new URL(DOWNLOAD_FILE_URL);
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
                if(true == downloadComplete) {
                    if(false == downloadCancel) {
                        temporaryFile.renameTo(new File(FILE_LOCAL_PATH));
                        showFinalConfirmation();
                    }
                }
            }
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
        }

        ProgressDialog progressDialog = null;
        Integer downloadFileSize = 0;
        Integer downloadFileSizeCount = 0;
    }
}

