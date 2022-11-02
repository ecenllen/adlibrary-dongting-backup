package com.yingyongduoduo.ad.utils;

import android.os.Environment;

/**
 * 存放数据的地方
 * 
 * @author duoduoapp
 */
public class IData {

	public static String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	/**
	 * 保存apk的
	 */
	public static String DEFAULT_APK_CACHE = SDCARD + "/mapvr/street/";

	/**
	 * 保存公众号等的图片等信息
	 */
	public static String DEFAULT_GZH_CACHE = SDCARD + "/mapvr/street/gzh/";

	public static String getDefaultApkCache() {
		return FileUtils.getOutputDirectory() + "/apk/";
	}

	public static String getDefaultGzhCache() {
		return FileUtils.getOutputDirectory() + "/gzh/";
	}
}
