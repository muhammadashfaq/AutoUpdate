package com.techndevs.autoupdate;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class ShowNote extends Activity {
    private BroadcastReceiver receiver;
    private long enqueue;
    private DownloadManager dm;
    boolean isDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Resulted", "Into ShowNote");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("AutoUpdate");
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setMessage("Latest Version is Available. Click on OK to update");
        builder.getContext().setTheme(R.style.AppTheme);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "App Downloading...Please Wait", Toast.LENGTH_LONG).show();
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                File file = new File("/storage/emulated/0/Download/AppUpdater.apk");
                if (file.exists()) {
                    Log.d("Resulted", "File Already exists.");
                    isDeleted = file.delete();
                    deleteAndInstall();
                } else {
                    Log.d("Resulted", "File is downloading first time.");
                    firstTimeInstall();
                }
            }
        });
        builder.setNegativeButton("Remind Me Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShowNote.this.finish();
            }
        });
        builder.show();
    }

    private void firstTimeInstall() {
        Log.d("May be 1st Update:", "OR deleted from folder");
        downloadAndInstall();
    }

    private void deleteAndInstall() {
        if (isDeleted) {
            Log.d("Deleted Existence file:", String.valueOf(isDeleted));
            downloadAndInstall();

        } else {
            Log.d("NOT DELETED:", String.valueOf(isDeleted));
            Toast.makeText(getApplicationContext(), "Error in Updating...Please try Later", Toast.LENGTH_LONG).show();
        }
    }

    private void downloadAndInstall() {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse("http://techndevs.us/clients/ashfaq/AutoUpdater/AppUpdater.apk"));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AppUpdater.apk");

        enqueue = dm.enqueue(request);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_LONG).show();

                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                            Log.d("ainfo", uriString);

                            if (downloadId == c.getInt(0)) {
                                Log.d("DOWNLOAD PATH:", c.getString(c.getColumnIndex("local_uri")));

                                Log.d("isRooted:", String.valueOf(isRooted()));
                                if (!isRooted()) {

                                    File apkFile = new File("/storage/emulated/0/Download/AppUpdater.apk");
                                    Intent apkIntent;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Uri apkUri = FileProvider.getUriForFile(ShowNote.this, BuildConfig.APPLICATION_ID + ".fileprovider", apkFile);
                                        apkIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                        apkIntent.setData(apkUri);
//                                        apkIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                        apkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    } else {
                                        Uri apkUri = Uri.fromFile(apkFile);
                                        apkIntent = new Intent(Intent.ACTION_VIEW);
                                        apkIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    }
                                    startActivity(apkIntent);

                                    Toast.makeText(getApplicationContext(), "App Installing", Toast.LENGTH_LONG).show();
                                } else {
                                    //if your device is rooted then you can install or update app in background directly
                                    Toast.makeText(getApplicationContext(), "App Installing...Please Wait", Toast.LENGTH_LONG).show();
                                    File file = new File("/storage/emulated/0/Download/AppUpdater.apk");
                                    Log.d("IN INSTALLER:", "/storage/emulated/0/Download/AppUpdater.apk");
                                    if (file.exists()) {
                                        try {
                                            String command;
                                            Log.d("IN File exists:", "/storage/emulated/0/Download/AppUpdater.apk");

                                            command = "pm install -r " + "/storage/emulated/0/Download/AppUpdater.apk";
                                            Log.d("COMMAND:", command);
                                            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                                            proc.waitFor();
                                            Toast.makeText(getApplicationContext(), "App Installed Successfully", Toast.LENGTH_LONG).show();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    c.close();
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private static boolean isRooted() {
        return findBinary("su");
    }

    public static boolean findBinary(String binaryName) {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if (new File(where + binaryName).exists()) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
