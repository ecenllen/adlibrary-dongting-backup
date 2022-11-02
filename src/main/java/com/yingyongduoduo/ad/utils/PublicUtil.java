package com.yingyongduoduo.ad.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Created by shanlin on 2017/10/12.
 */

public class PublicUtil {

    public static String metadata(Context context, String key) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            String value = appInfo.metaData.getString(key);
            if(TextUtils.isEmpty(value)) {
                value = appInfo.metaData.getInt(key, 0) + "";
            }
            return Objects.requireNonNull(value).replace("QQ", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        return getAppInfo(context) == null ? 1 : getAppInfo(context).versionCode;
    }

    public static String getVersionName(Context context) {
        return getAppInfo(context) == null ? "1.0" : getAppInfo(context).versionName;
    }

    public static boolean isImageFile(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        if (options.outWidth == -1) {
            return false;
        }
        return true;
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getAppName(Context context) {
        PackageManager packageManagers = context.getPackageManager();
        try {
            String appName = (String) packageManagers.getApplicationLabel(packageManagers.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA));
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAppPackage(Context context) {
        return context.getPackageName();
    }


    public static String readAssets(Context context, String filePath) {
        StringBuilder buf = new StringBuilder();
        try {
            InputStream json = context.getAssets().open(filePath);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return buf.toString();
    }

    public static PackageInfo getAppInfo(Context context) {
        try {
            PackageManager packageManagers = context.getPackageManager();
            return packageManagers.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setDoze(Context ctx) {
        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = ctx.getPackageName();
            boolean isIgnoring = ((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    ctx.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
