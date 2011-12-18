package net.madroom.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * @author mamor
 *
 */
public class CommonUtil {

    /**
     * Left padding a String with Zeros.
     * 
     * @param length
     * @param str
     * @return Left padded zero string.
     */
    public static String zeroPadding(int length, String str) {
        StringBuffer sb = new StringBuffer();
        String zero = "0";

        for(int count=0; count<length; count++){
            sb.append(zero);
        }

        DecimalFormat df = new DecimalFormat(sb.toString());
        return df.format(Integer.parseInt(str));
    }

    /**
     * Put all column names in Cursor.
     * 
     * @param c
     */
    public static void logColumnNames(Cursor c) {
        Log.v(CommonConf.DBG_TAG, "ColumnNames : " + Arrays.toString(c.getColumnNames()));
        Log.v(CommonConf.DBG_TAG, "CursorCount : " + c.getCount());
    }

    /**
     * @param context
     * @param alarmIntent
     * @param action
     * @param triggerAtTime
     */
    public static void setAlarm(Context context, Intent alarmIntent, String action, long triggerAtTime) {
        alarmIntent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, triggerAtTime, pendingIntent);
    }

    /**
     * @param context
     * @return DisplayMetrics.
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     * @param url
     * @return Shorten YouTube URL.
     */
    public static String getShortYoutubeUrl(String url) {
        if(url.matches(".+youtube\\.com.+") && (url.matches(".+\\?v=.*") || url.matches(".+&v=.*"))) {
            String[] array = url.split("\\?v=");
            if(array.length<2) {
                array = url.split("&v=");
            }
            if(2<=array.length) {
                String[] array2 = array[1].split("&");
                return "http://youtu.be/"+ array2[0];
            }
        }
        return "";
    }

    /**
     * @param login
     * @param apiKey
     * @param longUrl
     * @return Bit.ly URL.
     */
    public static String getShortUrlByBitlyV3(String login, String apiKey, String longUrl) {
        String ret = "";
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.path("http://api.bit.ly/v3/shorten");
        uriBuilder.appendQueryParameter("longUrl", Uri.encode(longUrl));
        uriBuilder.appendQueryParameter("login", login);
        uriBuilder.appendQueryParameter("apiKey", apiKey);
        String uri = Uri.decode(uriBuilder.build().toString());

        try {
            // HTTP request
            HttpUriRequest httpGet = new HttpGet(uri);
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            HttpResponse httpResponse = defaultHttpClient.execute(httpGet);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // HTTP response
                String entity = EntityUtils.toString(httpResponse.getEntity());

                // JSON decode
                JSONObject jsonEntity = new JSONObject(entity);
                if (jsonEntity != null) {
                    if(jsonEntity.optInt("status_code")==200) {
                        JSONObject jsonData = jsonEntity.optJSONObject("data");
                        if (jsonData != null) {
                            ret = jsonData.optString("url");
                        }
                    }
                }
            }
            return ret;
        } catch (IOException e) {
            return ret;
        } catch (JSONException e) {
            return ret;
        }
    }

    /**
     * @param context
     * @param packageName
     * @return boolean.
     */
    public static boolean isInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo(packageName, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     * @param str
     * @return String.
     */
    public static String multiSpace2singleSpace(String str) {
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile("[\\s]+");
        matcher = pattern.matcher(str);
        return matcher.replaceAll(" ");
    }

    /**
     * @param context
     * @return Current version code.
     */
    public static int getVersionCode(Context context){
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    /**
     * @param context
     * @return Current version name.
     */
    public static String getVersionName(Context context){
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return info.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    private static ProgressDialog mDialog;
    /**
     * @param context
     * @param message
     */
    public static void showProgressDialog(Context context, String message) {
        mDialog = new ProgressDialog(context);
        mDialog.setMessage(message);
        mDialog.show();
    }

    /**
     * 
     */
    public static void dismissProgressDialog() {
        mDialog.dismiss();
    }

    /**
     * @param context
     * @param text
     */
    public static void shareTextPlain(Context context, String text) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * @param src
     * @return Bitmap
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(is);

            is.close();
            conn.disconnect();
            return myBitmap;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }}
