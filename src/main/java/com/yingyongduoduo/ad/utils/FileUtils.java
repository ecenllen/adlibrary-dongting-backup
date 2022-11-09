package com.yingyongduoduo.ad.utils;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by AA on 2017/3/24.
 */
public class FileUtils {


    public static boolean isFile(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }


    public static String getRootPath() {
        ArrayList<StorageDirectoryParcelable> storageDirectories = FileUtils.getStorageDirectories();
        String rootPath = "";
        if (storageDirectories != null && storageDirectories.size() > 0) {
            rootPath = storageDirectories.get(0).path;
        }
        return rootPath;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static File getVolumeDirectory(StorageVolume volume) {
        try {
            Field f = StorageVolume.class.getDeclaredField("mPath");
            f.setAccessible(true);
            return (File) f.get(volume);
        } catch (Exception e) {
            // This shouldn't fail, as mPath has been there in every version
            throw new RuntimeException(e);
        }
    }

    /**
     * @return paths to all available volumes in the system (include emulated)
     */
    public static synchronized ArrayList<StorageDirectoryParcelable> getStorageDirectories() {
        ArrayList<StorageDirectoryParcelable> volumes;
        if (SDK_INT >= Build.VERSION_CODES.N) {
            volumes = getStorageDirectoriesNew();
        } else {
            volumes = getStorageDirectoriesLegacy();
        }
        return volumes;
    }

    public static final Pattern DIR_SEPARATOR = Pattern.compile("/");
    private static final String DEFAULT_FALLBACK_STORAGE_PATH = "/storage/sdcard0";
    public static final String PREFIX_OTG = "otg:/";

    /**
     * Returns all available SD-Cards in the system (include emulated)
     *
     * <p>Warning: Hack! Based on Android source code of version 4.3 (API 18) Because there was no
     * standard way to get it before android N
     *
     * @return All available SD-Cards in the system (include emulated)
     */
    public static synchronized ArrayList<StorageDirectoryParcelable> getStorageDirectoriesLegacy() {
        List<String> rv = new ArrayList<>();

        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                // Check for actual existence of the directory before adding to list
                if (new File(DEFAULT_FALLBACK_STORAGE_PATH).exists()) {
                    rv.add(DEFAULT_FALLBACK_STORAGE_PATH);
                } else {
                    // We know nothing else, use Environment's fallback
                    rv.add(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (SDK_INT >= Build.VERSION_CODES.M && checkStoragePermission()) rv.clear();
        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String strings[] = getExtSdCardPathsForActivity(DownLoaderAPK.weakContext.get());
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && canListFiles(f)) rv.add(s);
            }
        }

        // Assign a label and icon to each directory
        ArrayList<StorageDirectoryParcelable> volumes = new ArrayList<>();
        for (String file : rv) {
            File f = new File(file);

//            String name = StorageNaming.getDeviceDescriptionLegacy(this, f);
            volumes.add(new StorageDirectoryParcelable(file, ""));
        }

        return volumes;
    }

    public static boolean canListFiles(File f) {
        return f.canRead() && f.isDirectory();
    }

    public static boolean checkStoragePermission() {
        // Verify that all required contact permissions have been granted.
        return ActivityCompat.checkSelfPermission(DownLoaderAPK.weakContext.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
//                    Log.w(LOG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    /**
     * @return All available storage volumes (including internal storage, SD-Cards and USB devices)
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static synchronized ArrayList<StorageDirectoryParcelable> getStorageDirectoriesNew() {
        // Final set of paths
        ArrayList<StorageDirectoryParcelable> volumes = new ArrayList<>();
        StorageManager sm = DownLoaderAPK.weakContext.get().getSystemService(StorageManager.class);
        for (StorageVolume volume : sm.getStorageVolumes()) {
            if (!volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                    && !volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                continue;
            }
            File path = getVolumeDirectory(volume);
            String name = volume.getDescription(DownLoaderAPK.weakContext.get());
            int icon;
            if (!volume.isRemovable()) {

            } else {
                // HACK: There is no reliable way to distinguish USB and SD external storage
                // However it is often enough to check for "USB" String
                if (name.toUpperCase().contains("USB") || path.getPath().toUpperCase().contains("USB")) {
                } else {
                }
            }
            volumes.add(new StorageDirectoryParcelable(path.getPath(), name));
        }
        return volumes;
    }

    /**
     * 根据文件路径获取文件名称
     *
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

    /**
     * 生成本地文件路径
     *
     * @return
     */
//    public static File gerateLocalFile(String filePath, String fileName) {
////        String fileName = getFileName(filePath);
//        File dirFile = new File(getRootPath() + File.separator + PublicUtil.getAppName(DownLoaderAPK.weakContext.get()));
//        if (!dirFile.exists()) {
//            dirFile.mkdirs();
//        }
//        return new File(dirFile, fileName);
//    }

    public static File getOutputDirectory() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (Build.VERSION.SDK_INT >= 29) {
//                mediaStorageDir = DownLoaderAPK.weakContext.get().getExternalFilesDir(Environment.DIRECTORY_DCIM);
                String SDCARD_DIR_NEW = ContextCompat.getExternalFilesDirs(
                        DownLoaderAPK.weakContext.get(), null)[0].getAbsolutePath();
                mediaStorageDir = new File(SDCARD_DIR_NEW);
            } else {
                mediaStorageDir = new File(IData.DEFAULT_APK_CACHE);
            }
        } else {
            mediaStorageDir = getDiskCacheFile(DownLoaderAPK.weakContext.get());
        }

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (mediaStorageDir == null) {
            mediaStorageDir = new File(IData.DEFAULT_APK_CACHE);
        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                mediaStorageDir = getDiskCacheFile(DownLoaderAPK.weakContext.get());
            }
        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("abcdefg", "getOutputMediaFile 文件创建失败了！！！！！！！！！！！");
            }
        }
        Log.e("abcdefg", "mediaStorageDir ======== " + mediaStorageDir.getAbsolutePath());
        return mediaStorageDir;
    }

    private static File getDiskCacheFile(Context context) {
        File cachePath = null;
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
//                || !Environment.isExternalStorageRemovable()) {
//            cachePath = DownLoaderAPK.weakContext.get().getExternalFilesDir("");
//        } else {
//            cachePath = DownLoaderAPK.weakContext.get().getExternalCacheDir();
//        }
        if (Build.VERSION.SDK_INT >= 29) {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        } else {
            cachePath = context.getExternalFilesDir("");
        }
        if (cachePath == null || !cachePath.exists()) {
            cachePath = context.getExternalCacheDir();
        }
        if (cachePath == null || !cachePath.exists()) {
            cachePath = context.getFilesDir();
        }

        if (cachePath == null || !cachePath.exists()) {
            cachePath = context.getCacheDir();
        }
        if (cachePath != null)
            Log.e("abcdef", "getDiskCacheFile path ===========" + cachePath.getAbsolutePath());
        return cachePath;
    }


}
