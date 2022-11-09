package com.yingyongduoduo.ad.config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qq.e.comm.managers.GDTAdSdk;
import com.umeng.analytics.MobclickAgent;
import com.yingyongduoduo.ad.TTAdManagerHolder;
import com.yingyongduoduo.ad.bean.ADBean;
import com.yingyongduoduo.ad.bean.ConfigBean;
import com.yingyongduoduo.ad.bean.MainListBean;
import com.yingyongduoduo.ad.bean.PublicConfigBean;
import com.yingyongduoduo.ad.bean.SearchListBean;
import com.yingyongduoduo.ad.bean.ShowListBean;
import com.yingyongduoduo.ad.bean.VideoBean;
import com.yingyongduoduo.ad.bean.WXGZHBean;
import com.yingyongduoduo.ad.bean.ZiXunItemBean;
import com.yingyongduoduo.ad.bean.ZiXunListItemBean;
import com.yingyongduoduo.ad.utils.DownLoaderAPK;
import com.yingyongduoduo.ad.utils.HttpUtil;
import com.yingyongduoduo.ad.utils.IData;
import com.yingyongduoduo.ad.utils.PackageUtil;
import com.yingyongduoduo.ad.utils.PublicUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * 保存软件的配置信息
 * Created by yuminer on 2017/3/16.
 */
public class AppConfig {

    public static final String KEY_PUBLIC = "publicConfigJson";
    public static final String KEY_CONFIG = "ConfigJson";
    public static final String KEY_SELF = "SelfadJson";

    /**
     * index.html主页路径，从后台下载到手机本地的index.html 路径
     */
    public static String INDEX_HTML_LOCAL_PATH;
    /**
     * index.html本地路径：前缀加了file://，直接提供给webView使用
     */
    public static String URL_INDEX_HTML;
    /**
     * start.html本地路径
     */
    public static String START_HTML_LOCAL_PATH;
    /**
     * start.html本地路径：前缀加了file://，直接提供给webView使用
     */
    public static String URL_START_HTML;

    public static boolean isShowBanner = true;

    private static String play_qingxidu = "1";//清晰度 0,1,2 对应标清，高清，超清
    private static String download_qingxidu = "1";
    public static String youkulibPath;
    public static boolean isshowHDPicture = true;
    public static String GZHPath;
    public static float decimal = 0.05f;//广告内容百分比
    public static String[] qingxiduArray = new String[]{"标清", "高清", "超清", "1008P"};

    public static String getDownload_qingxidu(Context context) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        AppConfig.download_qingxidu = mSettings.getString("download_qingxidu", "2");
        return AppConfig.download_qingxidu;
    }

    public static String getDownload_qingxidu() {
        return AppConfig.download_qingxidu;
    }

    public static void setDownload_qingxidu(Context context, String qingxidu) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("download_qingxidu", qingxidu);
        editor.commit();
        AppConfig.download_qingxidu = qingxidu;
    }

    public static String getPlay_Qingxidu(Context context) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        AppConfig.play_qingxidu = mSettings.getString("play_qingxidu", "2");
        return AppConfig.play_qingxidu;
    }

    public static String getPlay_Qingxidu() {
        return AppConfig.play_qingxidu;
    }

    public static void setPlay_Qingxidu(Context context, String qingxidu) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("play_qingxidu", qingxidu);
        editor.commit();
        AppConfig.play_qingxidu = qingxidu;
    }

    //    public static List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();
    //    public static String ConfigJson = "";
//    public static String VideoJson = "";
//    public static String SelfadJson = "";
//    public static String wxgzhJson = "";
    public static String versioncode = "";
    public static String Channel = "";
    public static String APPKEY = "";


    private final static String baseURL1 = "http://120.25.224.76/appstore/";
    public final static String baseURL2 = "http://videodata.gz.bcebos.com/appstore/";
    public final static String baseURL3 = "http://www.yingyongduoduo.com/appstore/";
    private final static String configbaseURL1 = baseURL1 + "%s/";
    private final static String configbaseURL2 = baseURL2 + "%s/";
    private final static String configbaseURL3 = baseURL3 + "%s/";

    public static ConfigBean configBean;
    public static PublicConfigBean publicConfigBean;
    public static List<VideoBean> videoBeans = new ArrayList<VideoBean>();
    public static List<ADBean> selfadBeans = new ArrayList<ADBean>();
    public static List<ZiXunItemBean> ziXunBeans = new ArrayList<>();
    public static List<WXGZHBean> wxgzhBeans = new ArrayList<WXGZHBean>();

    /**
     * 联网初始化广告配置
     * 在启动页进行初始化
     *
     * @param context
     */
    public static void Init(Context context) {
        ///storage/emulated/0/Android/data/cn.uproxy.ox/files/testJson.html
//        INDEX_HTML_LOCAL_PATH = context.getExternalFilesDir(null).getAbsolutePath() + "/index.html";
        initConfigJson(context);
        initPublicConfigJson(context);
//        initVideoJson(context);
        initselfadJson(context);
        initzixunJson(context);
//        initwxgzhJson(context);

//        initvideosourceVersion(context);
        initADManager(context);

    }

    private static void initADManager(Context context) {
        String kp_String = "";
        boolean isHasAppId = false;
        if (AppConfig.configBean != null && AppConfig.configBean.ad_kp_idMap != null) {
//            String kpType = AppConfig.getKPType();//获取开屏广告类型，baidu，gdt，google
//            kp_String = AppConfig.configBean.ad_kp_idMap.get(kpType);
            Set<Map.Entry<String, String>> entries = AppConfig.configBean.ad_kp_idMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String adType = entry.getKey();
                if (!TextUtils.isEmpty(adType)) {
                    kp_String = entry.getValue();
                    if (!TextUtils.isEmpty(kp_String)) {
                        String[] a = kp_String.split(",");
                        if (a.length == 2) {
                            String appid = a[0];
                            if (!TextUtils.isEmpty(appid)) {
                                if ("csj".equals(adType)) {
                                    TTAdManagerHolder.init(context.getApplicationContext(), appid);
                                } else if ("gdt".equals(adType)) {
                                    GDTAdSdk.init(context, appid);
                                }
                                isHasAppId = true;
                            }
                        }
                    }
                }
            }

        }

        if (!isHasAppId) {
            Log.e("AppConfig", "获取广告APP_ID 为Null，请检查广告相关配置");
//            throw new NullPointerException("获取广告APP_ID 为Null，请检查广告相关配置，如无需要广告，则注释掉该异常即可。");
        }
    }

    /**
     * 初始化本地参数配置
     * 在Application 进行初始化
     *
     * @param context
     */
    public static void initLocalConfig(Context context) {
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            AppConfig.versioncode = GetVersionCode(context);
            AppConfig.APPKEY = appInfo.metaData.getString("UMENG_APPKEY");
            AppConfig.Channel = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        AppConfig.INDEX_HTML_LOCAL_PATH = context.getCacheDir() + File.separator + "index.html";// 浏览器主页HTML存放位置
        AppConfig.START_HTML_LOCAL_PATH = context.getCacheDir() + File.separator + "start.html";// 浏览器主页HTML存放位置
        // 浏览器主页HTML存放位置 测试
//        AppConfig.INDEX_HTML_LOCAL_PATH = Environment.getExternalStorageDirectory() +  "/cn.uproxy.ox/files/index.html";
//        AppConfig.START_HTML_LOCAL_PATH = Environment.getExternalStorageDirectory() +  "/cn.uproxy.ox/files/start.html";
        AppConfig.URL_INDEX_HTML = String.format("%s" + AppConfig.INDEX_HTML_LOCAL_PATH, "file://");
        AppConfig.URL_START_HTML = String.format("%s" + AppConfig.START_HTML_LOCAL_PATH, "file://");
        AppConfig.youkulibPath = context.getCacheDir() + File.separator + "videoparse.jar";// 初始化引擎存放位置
        AppConfig.GZHPath = IData.DEFAULT_GZH_CACHE;// 公众号的目录不能用缓存目录

        InitLocal(context);
    }

    private static String GetVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return String.valueOf(info.versionCode); //获取版本cood
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void InitLocal(Context context) {
        isShowBanner = true;
        initConfigBean(context, getPreferencesJson(context, KEY_CONFIG));
        initPublicConfigBean(context, getPreferencesJson(context, KEY_PUBLIC));
//        initVideoBean(context);
        initselfadBeans(context, getPreferencesJson(context, KEY_SELF));
        initZixunBeans(context);
        initwxgzhBeans(context);
        //initvideosourceVersion(context);这个没有必要初始化

        initADManager(context);

    }

    public static ConfigBean getConfigBean(String configJson) {
        ConfigBean bean = new ConfigBean();

        try {
            final JSONObject jo = new JSONObject(configJson);
            if (haveKey(jo, "updatemsg")) {
                JSONObject jo_ad_banner_id = jo.getJSONObject("updatemsg");
                bean.updatemsg.msg = jo_ad_banner_id.optString("msg");
                bean.updatemsg.versioncode = jo_ad_banner_id.optInt("versioncode");
                bean.updatemsg.url = jo_ad_banner_id.optString("url");
                bean.updatemsg.packageName = jo_ad_banner_id.optString("packageName");
            }
            if (haveKey(jo, "ad_banner_id")) {
                JSONObject jo_ad_banner_id = jo.getJSONObject("ad_banner_id");
                Iterator<String> keys = jo_ad_banner_id.keys();
                while (keys.hasNext()) { // 只要一个
                    String key = keys.next();
                    bean.ad_banner_idMap.put(key, jo_ad_banner_id.getString(key));
                }
            }
            if (haveKey(jo, "ad_kp_id")) {
                JSONObject jo_ad_kp_id = jo.getJSONObject("ad_kp_id");
                Iterator<String> keys = jo_ad_kp_id.keys();
                while (keys.hasNext()) { // 只要一个
                    String key = keys.next();
                    bean.ad_kp_idMap.put(key, jo_ad_kp_id.getString(key));
                }
            }
            if (haveKey(jo, "ad_cp_id")) {
                JSONObject jo_ad_banner_id = jo.getJSONObject("ad_cp_id");
                Iterator<String> keys = jo_ad_banner_id.keys();
                while (keys.hasNext()) { // 只要一个
                    String key = keys.next();
                    bean.ad_cp_idMap.put(key, jo_ad_banner_id.getString(key));
                }
            }
            if (haveKey(jo, "ad_tp_id")) {
                JSONObject jo_ad_banner_id = jo.getJSONObject("ad_tp_id");
                Iterator<String> keys = jo_ad_banner_id.keys();
                while (keys.hasNext()) { // 只要一个
                    String key = keys.next();
                    bean.ad_tp_idMap.put(key, jo_ad_banner_id.getString(key));
                }
            }
            if (haveKey(jo, "ad_shiping_id")) {
                JSONObject jo_ad_banner_id = jo.getJSONObject("ad_shiping_id");
                Iterator<String> keys = jo_ad_banner_id.keys();
                while (keys.hasNext()) { // 只要一个
                    String key = keys.next();
                    bean.ad_shiping_idMap.put(key, jo_ad_banner_id.getString(key));
                }
            }
            if (haveKey(jo, "cpuidorurl")) {
                bean.cpuidorurl = jo.getString("cpuidorurl");
            }

            if (haveKey(jo, "channel")) {
                JSONObject jo_channel = jo.getJSONObject("channel");
                if (haveKey(jo_channel, Channel)) {
                    JSONObject jo_channelInfo = jo_channel.getJSONObject(Channel);

                    if (haveKey(jo_channelInfo, "nomeinvchannel")) {
                        bean.nomeinvchannel = jo_channelInfo.getString("nomeinvchannel");
                    }
                    if (haveKey(jo_channelInfo, "nocpuadchannel")) {
                        bean.nocpuadchannel = jo_channelInfo.getString("nocpuadchannel");
                    }
                    if (haveKey(jo_channelInfo, "nofenxiang")) {
                        bean.nofenxiang = jo_channelInfo.getString("nofenxiang");
                    }
                    if (haveKey(jo_channelInfo, "nozhikouling")) {
                        bean.nozhikouling = jo_channelInfo.getString("nozhikouling");
                    }
                    if (haveKey(jo_channelInfo, "nosearch")) {
                        bean.nosearch = jo_channelInfo.getString("nosearch");
                    }
                    if (haveKey(jo_channelInfo, "nohaoping")) {
                        bean.nohaoping = jo_channelInfo.getString("nohaoping");
                    }
                    if (haveKey(jo_channelInfo, "nomapnochannel")) {
                        bean.nomapnochannel = jo_channelInfo.getString("nomapnochannel");
                    }
                    if (haveKey(jo_channelInfo, "noadbannerchannel")) {
                        bean.noadbannerchannel = jo_channelInfo.getString("noadbannerchannel");
                    }
                    if (haveKey(jo_channelInfo, "noshipingadchannel")) {
                        bean.noshipingadchannel = jo_channelInfo.getString("noshipingadchannel");
                    }
                    if (haveKey(jo_channelInfo, "noadkpchannel")) {
                        bean.noadkpchannel = jo_channelInfo.getString("noadkpchannel");
                    }
                    if (haveKey(jo_channelInfo, "noadtpchannel")) {
                        bean.noadtpchannel = jo_channelInfo.getString("noadtpchannel");
                    }
                    if (haveKey(jo_channelInfo, "noadcpchannel")) {
                        bean.noadcpchannel = jo_channelInfo.getString("noadcpchannel");
                    }
                    if (haveKey(jo_channelInfo, "nopaychannel")) {
                        bean.nopaychannel = jo_channelInfo.getString("nopaychannel");
                    }
                    if (haveKey(jo_channelInfo, "isfirstfreeusechannel")) {
                        bean.isfirstfreeusechannel = jo_channelInfo.getString("isfirstfreeusechannel");
                    }
                    if (haveKey(jo_channelInfo, "showselflogochannel")) {
                        bean.showselflogochannel = jo_channelInfo.getString("showselflogochannel");
                    }
                    if (haveKey(jo_channelInfo, "noshowdschannel")) {
                        bean.noshowdschannel = jo_channelInfo.getString("noshowdschannel");
                    }
                    if (haveKey(jo_channelInfo, "noupdatechannel")) {
                        bean.noupdatechannel = jo_channelInfo.getString("noupdatechannel");
                    }
                    if (haveKey(jo_channelInfo, "noselfadchannel")) {
                        bean.noselfadchannel = jo_channelInfo.getString("noselfadchannel");
                    }
                    if (haveKey(jo_channelInfo, "norootpaychannel")) {
                        bean.norootpaychannel = jo_channelInfo.getString("norootpaychannel");
                    }
                    if (haveKey(jo_channelInfo, "noaddvideochannel")) {
                        bean.noaddvideochannel = jo_channelInfo.getString("noaddvideochannel");
                    }
                    if (haveKey(jo_channelInfo, "noadVideowebchannel")) {
                        bean.noadVideowebchannel = jo_channelInfo.getString("noadVideowebchannel");
                    }
                    bean.playonwebchannel = jo_channelInfo.optString("playonwebchannel");

                    if (haveKey(jo_channelInfo, "nogzhchannel")) {
                        bean.nogzhchannel = jo_channelInfo.getString("nogzhchannel");
                    }
                    if (haveKey(jo_channelInfo, "bannertype")) {
                        bean.bannertype = jo_channelInfo.getString("bannertype");
                    }
                    if (haveKey(jo_channelInfo, "mapno")) {
                        bean.mapno = jo_channelInfo.getString("mapno");
                    }
                    if (haveKey(jo_channelInfo, "cptype")) {
                        bean.cptype = jo_channelInfo.getString("cptype");
                    }
                    if (haveKey(jo_channelInfo, "kptype")) {
                        bean.kptype = jo_channelInfo.getString("kptype");
                    }
                    if (haveKey(jo_channelInfo, "tptype")) {
                        bean.tptype = jo_channelInfo.getString("tptype");
                    }
                    if (haveKey(jo_channelInfo, "shipingtype")) {
                        bean.shipingtype = jo_channelInfo.getString("shipingtype");
                    }
                    if (haveKey(jo_channelInfo, "navigationid")) {
                        bean.navigationid = jo_channelInfo.getString("navigationid");
                    }

                } else {
                    bean = null;//连channel都没有，这可能是服务器异常
                }
            }
        } catch (Exception e) {
            bean = null;
        }
        return bean;
    }

    public static PublicConfigBean getpublicConfigBean(String configJson) {
        PublicConfigBean bean = new PublicConfigBean();

        try {
            final JSONObject jo = new JSONObject(configJson);

            if (haveKey(jo, "videosourceVersion")) {
                bean.videosourceVersion = jo.getString("videosourceVersion");
            }
            if (haveKey(jo, "selfadVersion")) {
                bean.selfadVersion = jo.getString("selfadVersion");
            }
            if (haveKey(jo, "zixunVersion")) {
                bean.zixunVersion = jo.getString("zixunVersion");
            }
            if (haveKey(jo, "dashangContent")) {
                bean.dashangContent = jo.getString("dashangContent");
            }
            if (haveKey(jo, "wxgzhversion")) {
                bean.wxgzhversion = jo.getString("wxgzhversion");
            }
            if (haveKey(jo, "goodPinglunVersion")) {
                bean.goodPinglunVersion = jo.getString("goodPinglunVersion");
            }
            if (haveKey(jo, "rootContent")) {
                bean.rootContent = jo.getString("rootContent");
            }
            if (haveKey(jo, "onlineVideoParseVersion")) {
                bean.onlineVideoParseVersion = jo.getString("onlineVideoParseVersion");
            }
            if (haveKey(jo, "baiduCpuId")) {
                bean.baiduCpuId = jo.getString("baiduCpuId");
            }
            if (haveKey(jo, "qqKey")) {
                bean.qqKey = jo.getString("qqKey");
            }
            if (haveKey(jo, "Information")) {
                bean.Information = jo.getString("Information");
            }
            if (haveKey(jo, "fenxiangInfo")) {
                bean.fenxiangInfo = jo.getString("fenxiangInfo");
            }
            if (haveKey(jo, "zhikouling")) {
                bean.zhikouling = jo.getString("zhikouling");
            }
            if (haveKey(jo, "searchbaidudomestic")) {
                bean.searchbaidudomestic = jo.getString("searchbaidudomestic");
            }
        } catch (Exception e) {
            bean = null;
        }
        return bean;
    }

    public static List<VideoBean> getVideoBean(String videoJson) {
        List<VideoBean> beans = new ArrayList<VideoBean>();

        try {
            final JSONArray ja = new JSONArray(videoJson);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                VideoBean bean = new VideoBean();
                if (haveKey(jo, "platform") && haveKey(jo, "name") && haveKey(jo, "playonbroswer")) {
                    bean.platform = jo.getString("platform");
                    bean.name = jo.getString("name");
                    bean.playonbroswer = jo.getString("playonbroswer");
                    bean.noadVideowebBaseUrl = jo.getString("noadVideowebBaseUrl");
                    bean.imgUrl = jo.getString("imgUrl");
                    beans.add(bean);
                }
            }

        } catch (Exception e) {
        }
        return beans;
    }

    private static List<ADBean> getSelfAdBeans(String selfadJson) {
        List<ADBean> beans = new ArrayList<ADBean>();

        try {
            final JSONArray ja = new JSONArray(selfadJson);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                ADBean bean = new ADBean();
                bean.setAd_name(jo.optString("name"));
                bean.setAd_description(jo.optString("description"));
                bean.setAd_iconurl(jo.optString("iconurl"));
                bean.setAd_iconscal((float) jo.optDouble("iconscal", bean.getAd_iconscal()));
                bean.setAd_thumbnail(jo.optString("thumbnail"));
                bean.setAd_thumbnailscal((float) jo.optDouble("thumbnailscal", bean.getAd_thumbnailscal()));
                bean.setAd_banner(jo.optString("banner"));
                bean.setAd_kp(jo.optString("kp"));
                bean.setAd_apkurl(jo.optString("apkurl"));
                bean.setAd_packagename(jo.optString("packagename"));
                bean.setAd_isConfirm(jo.optBoolean("isConfirm"));
                bean.setAd_type(jo.optInt("type"));
                bean.setAd_versioncode(jo.optInt("versioncode"));
                bean.setAd_platform("ad");
                beans.add(bean);
//                if (haveKey(jo, "displayName") && haveKey(jo, "secondConfirm") && haveKey(jo, "adtype") && haveKey(jo, "scal") && haveKey(jo, "iconthunb1") && haveKey(jo, "url") && haveKey(jo, "packageName")) {
//                    bean.displayName = jo.getString("displayName");
//                    bean.secondConfirm = jo.getString("secondConfirm");
//                    bean.adtype = jo.getString("adtype");
//                    bean.scal = (float) jo.getDouble("scal");
//                    bean.iconthunb1 = jo.getString("iconthunb1");
//                    bean.url = jo.getString("url");
//                    bean.packageName = jo.getString("packageName");
//                    beans.add(bean);
//                }
            }

        } catch (Exception e) {
        }
        return beans;
    }

    private static List<ZiXunItemBean> getZiXunBeans(String zixunJson) {
        List<ZiXunItemBean> beans = new ArrayList<>();

        try {
            final JSONArray ja = new JSONArray(zixunJson);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                JSONObject bottomTab = jo.optJSONObject("bottomTab");
                ZiXunItemBean ziXunItemBean = new ZiXunItemBean();
                ziXunItemBean.setTabName(bottomTab.optString("tabName"));
                ziXunItemBean.setIcon(bottomTab.optString("icon"));
                ziXunItemBean.setSelIcon(bottomTab.optString("selIcon"));
                JSONArray list = bottomTab.optJSONArray("list");
                if (list != null) {
                    List<ZiXunListItemBean> ziXunListItemBeans = new ArrayList<>();
                    for (int l = 0; l < list.length(); l++) {
                        JSONObject jsonObject = list.getJSONObject(l);
                        ZiXunListItemBean ziXunListItemBean = new ZiXunListItemBean();
                        ziXunListItemBean.setName(jsonObject.optString("name"));
                        ziXunListItemBean.setUrl(jsonObject.optString("url"));
                        ziXunListItemBeans.add(ziXunListItemBean);
                    }
                    ziXunItemBean.setList(ziXunListItemBeans);
                }

                beans.add(ziXunItemBean);
            }

        } catch (Exception e) {
        }
        return beans;
    }

    public static List<WXGZHBean> getWXGZHBeans(String wxgzhJson) {
        List<WXGZHBean> beans = new ArrayList<WXGZHBean>();

        try {
            final JSONArray ja = new JSONArray(wxgzhJson);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                WXGZHBean bean = new WXGZHBean();
                if (haveKey(jo, "displayName") && haveKey(jo, "introduction") && haveKey(jo, "url") && haveKey(jo, "id") && haveKey(jo, "thumb") && haveKey(jo, "type")) {
                    bean.displayName = jo.getString("displayName");
                    bean.id = jo.getString("id");
                    bean.type = jo.getString("type");
                    bean.introduction = jo.getString("introduction");
                    bean.thumb = jo.getString("thumb");
                    bean.url = jo.getString("url");
                    if (new File(GZHPath + bean.id + ".jpg").exists()) {
                        bean.isPicExist = true;
                    }
                    beans.add(bean);
                }
            }

        } catch (Exception e) {
        }
        return beans;
    }

    private static String getConfigJson(String url) {
        String ConfigJson = "";
        try {
            ConfigJson = HttpUtil.getJson(url);
            ConfigBean bean1 = getConfigBean(ConfigJson);
            if (bean1 == null) {
                ConfigJson = "";

            }
        } catch (Exception ex) {
            ConfigJson = "";
        }
        return ConfigJson;
    }

    private static String getPubConfigJson(String url) {
        String getpubConfigJson = "";
        try {
            getpubConfigJson =
                    HttpUtil.getJson(url);
            PublicConfigBean bean1 = getpublicConfigBean(getpubConfigJson);
            if (bean1 == null) {
                getpubConfigJson = "";

            }
        } catch (Exception ex) {
            getpubConfigJson = "";
        }
        return getpubConfigJson;
    }

    private final static String dongtingBaseURL1 = "https://api.csdtkj.cn/xly/webcloud/";
    private final static String configAPI = dongtingBaseURL1 + "jsonadconfig/getadconfig";
    private final static String publicAPI = dongtingBaseURL1 + "jsonadconfig/getpublic";
    private final static String videoAPI = dongtingBaseURL1 + "jsonadconfig/getvideo";
    private final static String selfadAPI = dongtingBaseURL1 + "jsonadconfig/getselfad";
    private final static String zixunAPI = dongtingBaseURL1 + "jsonadconfig/getzixun";

    private static String getParameters(Context context) {
        return "?application=" + "ROOT"
                + "&apppackage=" + PublicUtil.getAppPackage(context)
                + "&appversion=" + PublicUtil.getVersionCode(context)
                + "&appmarket=" + PublicUtil.metadata(context, "UMENG_CHANNEL")
                + "&agencychannel=" + PublicUtil.metadata(context, "AGENCY_CHANNEL");
    }

    public static void initConfigJson(Context context) {

//        new OkHttpUtil().startDownloadJson(INDEX_HTML_LOCAL_PATH);

        String ConfigJson = "";
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        ConfigJson = getConfigJson(configAPI + getParameters(context));
        if (TextUtils.isEmpty(ConfigJson))
            ConfigJson = getConfigJson(String.format(configbaseURL1, APPKEY) + "config.json");
        if (TextUtils.isEmpty(ConfigJson)) {
            ConfigJson = getConfigJson(String.format(configbaseURL2, APPKEY) + "config.json");
        }
        if (TextUtils.isEmpty(ConfigJson)) {
            ConfigJson = getConfigJson(String.format(configbaseURL3, APPKEY) + "config.json");
        }
        if (!TextUtils.isEmpty(ConfigJson)) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString("ConfigJson", ConfigJson);
            editor.apply();
            initConfigBean(context, ConfigJson);
        } else {
            initConfigBean(context, getPreferencesJson(context, KEY_CONFIG));
        }

    }

    public static void initPublicConfigJson(Context context) {
        String ConfigJson = "";
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        ConfigJson = getPubConfigJson(publicAPI + getParameters(context));
        if (TextUtils.isEmpty(ConfigJson)) {
            ConfigJson = getPubConfigJson(baseURL1 + "publicconfig.json");
        }
        if (TextUtils.isEmpty(ConfigJson)) {
            ConfigJson = getPubConfigJson(baseURL2 + "publicconfig.json");
        }
        if (TextUtils.isEmpty(ConfigJson)) {
            ConfigJson = getPubConfigJson(baseURL3 + "publicconfig.json");
        }
        if (!ConfigJson.isEmpty()) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(KEY_PUBLIC, ConfigJson);
            editor.apply();
            initPublicConfigBean(context, ConfigJson);
        } else {
            ConfigJson = getPreferencesJson(context, KEY_PUBLIC);
            initPublicConfigBean(context, ConfigJson);
        }
    }

    public static void initConfigBean(Context context, String ConfigJson) {
        if (context == null) return;
        try {
            ConfigBean bean1 = getConfigBean(ConfigJson);
            configBean = bean1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPreferencesJson(Context context, String key) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        return mSettings.getString(key, "");
    }

    public static void initPublicConfigBean(Context context, String ConfigJson) {
        if (context == null) return;
        try {
            PublicConfigBean bean1 = getpublicConfigBean(ConfigJson);
            publicConfigBean = bean1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getVideoJson(String url) {

        String VideoJson = "";
        try {
            VideoJson = new HttpUtil().getJson(url);
            List<VideoBean> currentVideoBeans = getVideoBean(VideoJson);
            if (currentVideoBeans.size() == 0) {
                VideoJson = "";
            }
        } catch (Exception e) {

            VideoJson = "";
        }
        return VideoJson;
    }

    public static void initVideoJson(Context context) {
        if (context == null) return;
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        if (publicConfigBean != null && !"".equals(publicConfigBean.onlineVideoParseVersion) && !publicConfigBean.onlineVideoParseVersion.equals(mSettings.getString("onlineVideoParseVersion", ""))) {//需要更新videosourceVersion
            String VideoJson = getVideoJson(baseURL1 + "video/video.json");
            if (VideoJson.isEmpty()) {
                VideoJson = getVideoJson(baseURL2 + "video/video.json");
            }
            if (VideoJson.isEmpty()) {
                VideoJson = getVideoJson(baseURL3 + "video/video.json");
            }

            if (!VideoJson.isEmpty()) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("VideoJson", VideoJson);
                editor.putString("onlineVideoParseVersion", publicConfigBean.onlineVideoParseVersion);
                editor.apply();
            }

        }
        initVideoBean(context);
    }

    public static void initVideoBean(Context context) {
        if (context == null) return;
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);

        String VideoJson = mSettings.getString("VideoJson", "");
        try {
            List<VideoBean> currentVideoBeans = getVideoBean(VideoJson);
            if (currentVideoBeans.size() == 0) {
                VideoJson = "";

            }
            videoBeans = currentVideoBeans;
        } catch (Exception e) {

        }
    }

    private static String getSelfadJson(String url) {
        String SelfadJson = "";
        try {
            SelfadJson = HttpUtil.getJson(url);
            List<ADBean> currentSelfAdBeans = getSelfAdBeans(SelfadJson);
            if (currentSelfAdBeans.size() == 0) {
                SelfadJson = "";
            }
        } catch (IOException e) {
            SelfadJson = "";
        }
        return SelfadJson;
    }

    /**
     * 读取asset里的文件
     *
     * @param context  上下文
     * @param filename 文件名
     * @return
     */
    public static String getZixunJsonFromAssets(Context context, String filename) {
        String string = "";
        try {
            InputStream in = context.getResources().getAssets().open(filename);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            string = new String(buffer);

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    private static String getZixunJson(String url) {
        String SelfadJson = "";
        try {
            SelfadJson = HttpUtil.getJson(url);
            List<ZiXunItemBean> currentSelfAdBeans = getZiXunBeans(SelfadJson);
            if (currentSelfAdBeans.size() == 0) {
                SelfadJson = "";
            }
        } catch (IOException e) {
            SelfadJson = "";
        }
        return SelfadJson;
    }

    public static void initselfadJson(Context context) {
        if (context == null) return;
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);

        if (publicConfigBean != null && !"".equals(publicConfigBean.selfadVersion) && !publicConfigBean.selfadVersion.equals(mSettings.getString("selfadVersion", ""))) {//需要更新
            String SelfadJson = getSelfadJson(selfadAPI + getParameters(context));

            if (TextUtils.isEmpty(SelfadJson))
                SelfadJson = getSelfadJson(baseURL1 + "selfad/selfad.json");

            if (SelfadJson.isEmpty()) {
                SelfadJson = getSelfadJson(baseURL2 + "selfad/selfad.json");
            }
            if (SelfadJson.isEmpty()) {
                SelfadJson = getSelfadJson(baseURL3 + "selfad/selfad.json");
            }


            if (!SelfadJson.isEmpty()) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("SelfadJson", SelfadJson);
                editor.putString("selfadVersion", publicConfigBean.selfadVersion);
                editor.apply();
                initselfadBeans(context, SelfadJson);
            } else {
                initselfadBeans(context, getPreferencesJson(context, KEY_SELF));
            }
        }

    }

    private static void initselfadBeans(Context context, String SelfadJson) {
        if (context == null) return;
        try {
            List<ADBean> currentSelfAdBeans = getSelfAdBeans(SelfadJson);
            if (currentSelfAdBeans.size() == 0) {
                SelfadJson = "";
            }
            selfadBeans = currentSelfAdBeans;
        } catch (Exception e) {

        }
    }

    private static void initzixunJson(Context context) {
        if (context == null) return;
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        String SelfadJson = "";
        if (publicConfigBean != null && !TextUtils.isEmpty(publicConfigBean.zixunVersion) && !publicConfigBean.zixunVersion.equals(mSettings.getString("zixunVersion", ""))) {//需要更新
            SelfadJson = getZixunJson(zixunAPI + getParameters(context));
            if (TextUtils.isEmpty(SelfadJson)) {
                SelfadJson = getZixunJson(baseURL1 + "zixun/zixun.json");
            }
            if (TextUtils.isEmpty(SelfadJson)) {
                SelfadJson = getZixunJson(baseURL2 + "zixun/zixun.json");
            }
            if (TextUtils.isEmpty(SelfadJson)) {
                SelfadJson = getZixunJson(baseURL3 + "zixun/zixun.json");
            }
            if (!TextUtils.isEmpty(SelfadJson)) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("zixunJson", SelfadJson);
                editor.putString("zixunVersion", publicConfigBean.zixunVersion);
                editor.apply();
            }
        }
        if (TextUtils.isEmpty(SelfadJson)) {
            SelfadJson = getZixunJsonFromAssets(context, "zixun.json");
            if (!TextUtils.isEmpty(SelfadJson)) {
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("zixunJson", SelfadJson);
                editor.putString("zixunVersion", "");
                editor.apply();
            }
        }

        initZixunBeans(context);
    }

    private static void initZixunBeans(Context context) {
        if (context == null) return;
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        String ZixunJson = mSettings.getString("zixunJson", "");
        try {
            List<ZiXunItemBean> currentSelfAdBeans = getZiXunBeans(ZixunJson);
            if (currentSelfAdBeans.size() == 0) {
                ZixunJson = "";
            }
            ziXunBeans = currentSelfAdBeans;
        } catch (Exception e) {

        }
    }

    private static String getWXGZHJson(String url) {
        String wxgzhJson = "";
        try {
            wxgzhJson = HttpUtil.getJson(url);
            List<WXGZHBean> currentSelfAdBeans = getWXGZHBeans(wxgzhJson);
            if (currentSelfAdBeans.size() == 0) {
                wxgzhJson = "";
            }
        } catch (IOException e) {
            wxgzhJson = "";
        }
        return wxgzhJson;
    }

    public static void initwxgzhJson(Context context) {
        if (context == null) return;
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        if (publicConfigBean != null && !"".equals(publicConfigBean.wxgzhversion) && !publicConfigBean.wxgzhversion.equals(mSettings.getString("wxgzhversion", ""))) {//需要更新
            String wxgzhJson = getWXGZHJson(baseURL1 + "wxgzh/wxgzh.json");
            if (wxgzhJson.isEmpty()) {
                wxgzhJson = getWXGZHJson(baseURL2 + "wxgzh/wxgzh.json");
            }
            if (wxgzhJson.isEmpty()) {
                wxgzhJson = getWXGZHJson(baseURL3 + "wxgzh/wxgzh.json");
            }

            if (!wxgzhJson.isEmpty()) {
                List<WXGZHBean> currentSelfAdBeans = getWXGZHBeans(wxgzhJson);
                for (WXGZHBean bean : currentSelfAdBeans) {
                    initGZHPic(bean);
                }
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("wxgzhJson", wxgzhJson);
                editor.putString("wxgzhversion", publicConfigBean.wxgzhversion);
                editor.apply();
            }
        }
        String wxgzhJson = mSettings.getString("wxgzhJson", "");
        List<WXGZHBean> currentSelfAdBeans = getWXGZHBeans(wxgzhJson);
        for (WXGZHBean bean : currentSelfAdBeans) {
            // Boolean isSuccess = true;//成功与否不重要，不成功的不用就是
            if (!new File(GZHPath + bean.id + ".jpg").exists()) {//如果文件不存在
                initGZHPic(bean);
            }
        }
        initwxgzhBeans(context);//初始化之后需要判断图片是否存在
    }

    private static void initGZHPic(WXGZHBean bean) {

        try {
            downloadgzhjpg(bean, bean.thumb);
        } catch (Exception ethumb) {//这一步则表示下载失败
            deleteFile(GZHPath + bean.id + ".jpg");

            try {
                downloadgzhjpg(bean, baseURL1 + "wxgzh/" + bean.id + ".jpg");
            } catch (Exception e1) {
                deleteFile(GZHPath + bean.id + ".jpg");
                try {
                    downloadgzhjpg(bean, baseURL2 + "wxgzh/" + bean.id + ".jpg");
                } catch (Exception e2) {
                    deleteFile(GZHPath + bean.id + ".jpg");
                    try {
                        downloadgzhjpg(bean, baseURL3 + "wxgzh/" + bean.id + ".jpg");
                    } catch (Exception e3) {//这一步则表示下载失败
                        // isSuccess = false;
                        deleteFile(GZHPath + bean.id + ".jpg");
                    }
                }
            }
        }
    }

    public static void downloadgzhjpg(WXGZHBean bean, String jpgurl) throws Exception {
        deleteFile(GZHPath + bean.id + ".jpg");// 如果存在就先删除
        URL url = new URL(jpgurl);
        URLConnection con = url.openConnection();
        int contentLength = con.getContentLength();
        InputStream is = con.getInputStream();
        byte[] bs = new byte[1024];
        int len;
        OutputStream os = new FileOutputStream(GZHPath + bean.id + ".jpg");
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        os.close();
        is.close();

    }

    public static void initwxgzhBeans(Context context) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        String wxgzhJson = mSettings.getString("wxgzhJson", "");
        try {
            List<WXGZHBean> currentSelfAdBeans = getWXGZHBeans(wxgzhJson);
            if (currentSelfAdBeans.size() == 0) {
                wxgzhJson = "";

            }
            wxgzhBeans = currentSelfAdBeans;
        } catch (Exception e) {

        }
    }

    public static void initvideosourceVersion(Context context) {
        SharedPreferences mSettings = context.getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        Boolean isneedUpdate = publicConfigBean != null && !"".equals(publicConfigBean.videosourceVersion) && !publicConfigBean.videosourceVersion.equals(mSettings.getString("videosourceVersion", ""));
        if (isneedUpdate || (!(new File(youkulibPath).exists()) && publicConfigBean != null && !"".equals(publicConfigBean.videosourceVersion))) {//需要更新videosourceVersion 或者没有在目录下找到该jar,但是获取
            Boolean isSuccess = true;
            try {
                downloadjar(baseURL1 + "video/videoparse.jar", youkulibPath);
            } catch (Exception e1) {
                try {
                    downloadjar(baseURL2 + "video/videoparse.jar", youkulibPath);
                } catch (Exception e2) {
                    try {
                        downloadjar(baseURL3 + "video/videoparse.jar", youkulibPath);
                    } catch (Exception e3) {//这一步则表示下载失败
                        isSuccess = false;
                    }
                }
            }
            if (isSuccess) {

                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("videosourceVersion", publicConfigBean.videosourceVersion);
                editor.apply();
            } else {
                deleteFile(youkulibPath);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("videosourceVersion", "");
                editor.apply();
            }
        }
    }

    public static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ex) {
        }
    }

    public static void downloadjar(String jarUrl, String savePath) throws Exception {
        deleteFile(savePath);// 如果存在就先删除
        boolean mkdirs = new File(savePath).mkdirs();
        Log.e("downloadjar", mkdirs ? "创建下载目录成功" : "创建下载目录失败");
        URL url = new URL(jarUrl);
        URLConnection con = url.openConnection();
        int contentLength = con.getContentLength();
        InputStream is = con.getInputStream();
        byte[] bs = new byte[1024];
        int len;
        OutputStream os = new FileOutputStream(savePath);
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        os.flush();
        os.close();
        is.close();

    }

    private static boolean haveKey(JSONObject jo, String key) {
        return jo.has(key) && !jo.isNull(key);
    }


    /*
     *
     * 是否展示在线视频，针对底部的channel
     * */
    public static boolean isShowOnlineVideo() {
        if (configBean == null) {
            return false;
        }
        for (String version :
                configBean.noaddvideochannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }

        }
        return true;
    }

    public static boolean isShowWXGZH() {
        if (configBean == null) {
            return false;
        }

        for (String version :
                configBean.nogzhchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowMeinv() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.nomeinvchannel))
            return false;
        for (String version :
                configBean.nomeinvchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否展示资讯
     *
     * @return
     */
    public static boolean isShowCpuWeb() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.nocpuadchannel))
            return false;
        for (String version :
                configBean.nocpuadchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否显示支付宝支付
     *
     * @return
     */
    public static boolean isShowAliPay() {
        if (configBean == null) {
            return true;
        }
        for (String version :
                configBean.noalipaychannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRootPay() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.norootpaychannel))
            return false;
        for (String version : configBean.norootpaychannel.split(",")) {
            if (version.equals(versioncode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否显示微信支付
     *
     * @return
     */
    public static boolean isShowWeixinPay() {
        if (configBean == null) {
            return true;
        }
        for (String version :
                configBean.noweixinpaychannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFengxiang() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.nofenxiang))
            return false;
        for (String version :
                configBean.nofenxiang.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowHaoPing() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.nohaoping))
            return false;
        for (String version : configBean.nohaoping.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowMapNO() {
        if (configBean == null) {
            return true;
        }
        for (String version : configBean.nomapnochannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否展示视频广告
     *
     * @return
     */
    public static boolean isShowShipng() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.noshipingadchannel))
            return false;
        for (String version : configBean.noshipingadchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowBanner() {
        if (!isShowBanner)
            return false;
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.noadbannerchannel))
            return false;
        for (String version : configBean.noadbannerchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowKP() {
        if (configBean == null) {//如果configbean都没有获取到
            return false;
        }
        if (TextUtils.isEmpty(configBean.noadkpchannel))
            return false;
        for (String version : configBean.noadkpchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowTP() {
        if (configBean == null) {//如果configbean都没有获取到
            return false;
        }
        if (TextUtils.isEmpty(configBean.noadtpchannel))
            return false;
        for (String version : configBean.noadtpchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowCP() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.noadcpchannel))
            return false;
        for (String version : configBean.noadcpchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNeedPay() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.nopaychannel))
            return false;
        for (String version : configBean.nopaychannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFirstFreeUse() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.isfirstfreeusechannel))
            return false;
        for (String version : configBean.isfirstfreeusechannel.split(",")) {
            if (version.equals(versioncode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isShowSelfLogo() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.showselflogochannel))
            return false;
        for (String version : configBean.showselflogochannel.split(",")) {
            if (version.equals(versioncode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isShowDS() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.noshowdschannel))
            return false;
        for (String version : configBean.noshowdschannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isShowUpdate() {
        if (configBean == null) {
            return false;
        }
        for (String version : configBean.noupdatechannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isShowSelfAD() {
        if (configBean == null) {
            return false;
        }
        if (TextUtils.isEmpty(configBean.noselfadchannel))
            return false;
        for (String version : configBean.noselfadchannel.split(",")) {
            if (version.equals(versioncode)) {
                return false;
            }
        }
        return true;
    }

    //是否使用第三方链接对当前播放地址进行处理，针对vip等情况
    public static boolean isNoadVideoweb(String platform) {
        if (configBean == null || TextUtils.isEmpty(platform)) {
            return false;
        }
        for (String str : configBean.noadVideowebchannel.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String platformItem = a[0];
                String versionItem = a[1];
                if (platform.equals(platformItem) && versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return false;
                }
            }
        }
        return true;
    }

    //获取第三方去广告的基地址
    public static String getnoadVideowebBaseUrl(String platform, String url) {
        if (videoBeans == null || videoBeans.size() == 0 || TextUtils.isEmpty(platform)) {
            return url;
        }
        if (!isNoadVideoweb(platform)) {
            return url;
        }
        for (VideoBean bean : AppConfig.videoBeans) {
            if (bean.platform.equals(platform)) {
                if (bean.noadVideowebBaseUrl.contains("%s")) {
                    return String.format(bean.noadVideowebBaseUrl, url);
                } else {
                    return url;
                }
            }
        }
        return url;
    }

    //是否通过浏览器进行播放，这里涉及两个开关，一个在videojson里面（总开关），一个在config里面（渠道开关）
    public static boolean isPlayOnweb(String platform) {
        System.out.println("当前频道:" + platform);
        if (videoBeans == null || videoBeans.size() == 0 || TextUtils.isEmpty(platform)) {
            return false;
        }
        for (VideoBean bean : AppConfig.videoBeans) {
            if (bean.platform.equals(platform)) {
                if ("1".equals(bean.playonbroswer)) //为1则表示用web播放,为0表示总开关没有设置一定要用web播放
                {
                    return true;
                } else {
                    break;
                }
            }
        }
        if (configBean == null) {
            return false;
        }
        for (String str : configBean.playonwebchannel.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String platformItem = a[0];
                String versionItem = a[1];
                if (platform.equals(platformItem) && versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return true;
                }
            }
        }
        return false;
    }

    public static String getKPType() {
        if (configBean == null) {
            return "csj";
        }
        for (String str : configBean.kptype.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return adNameItem;
                }

            }
        }
        return "csj";

    }

    public static String getTPType() {
        if (configBean == null) {
            return "gdtmb";
        }
        for (String str : configBean.tptype.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return adNameItem;
                }

            }
        }
        return "gdtmb";

    }

    public static String getCPType() {
        if (configBean == null) {
            return "csj2";
        }
        for (String str : configBean.cptype.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return adNameItem;
                }

            }
        }
        return "csj2";

    }

    public static int getNavigationButtonId() {
        if (configBean == null) {
            return 2147479817;
        }
        for (String str : configBean.navigationid.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return Integer.parseInt(adNameItem);
                }
            }
        }
        return 2147479817;

    }

    public static String getBannerType() {
        if (configBean == null) {
            return "csj";
        }
        for (String str : configBean.bannertype.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return adNameItem;
                }

            }
        }
        return "csj";

    }


    public static String getShipingType() {
        if (configBean == null) {
            return "";
        }
        for (String str : configBean.shipingtype.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return adNameItem;
                }

            }
        }
        return "csj";

    }

    public static String getBaiduMapNO() {
        if (configBean == null) {
            return "©2022 高德软件有限公司 GS(2021)6375号 - 甲测资字11111093";
//            return "北京百度网讯科技有限公司\n©2022 Baidu - GS(2021)6026号 - 甲测资字11111342";
        }
        for (String str : configBean.mapno.split(",")) {
            String[] a = str.split(":");
            if (a.length == 2) {
                String versionItem = a[0];
                String adNameItem = a[1];
                if (versioncode.equals(versionItem)) {//平台与版本对应了，因为渠道已经选定了
                    return adNameItem;
                }

            }
        }
        return "©2022 高德软件有限公司 GS(2021)6375号 - 甲测资字11111093";
//        return "北京百度网讯科技有限公司\n©2022 Baidu - GS(2021)6026号 - 甲测资字11111342";

    }

    private static List<Integer> GetRandomList(int size, int max) {
        Random r = new Random();
        List<Integer> list = new ArrayList<Integer>();
        int i;
        while (list.size() < size) {
            i = r.nextInt(max);
            if (!list.contains(i)) {
                list.add(i);
            }
        }
        Collections.sort(list);
        return list;
    }

    public static Boolean ISInistallSelfAD(String packageName) {
        for (ADBean selfad : selfadBeans) {//过滤掉已经安装了的app
            if (TextUtils.equals(packageName, selfad.getAd_packagename())) {
                return true;
            }

        }
        return false;

    }

    /*
     * 随机取广告，size表示取的数量
     * */
    public static List<ADBean> GetSelfADByCount(Context context, int size, String event_id) {
        List<ADBean> selfADs = new ArrayList<ADBean>();
        List<ADBean> ok_selfadBeans = new ArrayList<ADBean>();
        for (ADBean selfad : selfadBeans) {//过滤掉已经安装了的app
            if (!PackageUtil.isInstallApp(context, selfad.getAd_packagename())) {
                ok_selfadBeans.add(selfad);
            }

        }
        if (size >= ok_selfadBeans.size()) {
            selfADs.addAll(ok_selfadBeans);
        } else {
            //建立一个size大的0-selfadBeans.size()之间不重复的list
            List<Integer> positionList = GetRandomList(size, ok_selfadBeans.size());
            for (int i : positionList) {
                selfADs.add(ok_selfadBeans.get(i));
            }
        }
        for (ADBean bean : selfADs) {
            Map<String, String> map_ekv = new HashMap<String, String>();
            map_ekv.put("show", bean.getAd_name());
            MobclickAgent.onEvent(context, event_id, map_ekv);
        }
        return selfADs;
    }

//    public static DownloadInfo adToDownloadInfo(ADBean adbean) {
//        DownloadInfo downloadinfo = new DownloadInfo();
//        downloadinfo.setAd_name(adbean.getAd_name());
//        downloadinfo.setAd_description(adbean.getAd_description());
//        downloadinfo.setAd_iconurl(adbean.getAd_iconurl());
//        downloadinfo.setAd_thumbnail(adbean.getAd_thumbnail());
//        downloadinfo.setAd_iconscal(adbean.getAd_iconscal());
//        downloadinfo.setAd_thumbnailscal(adbean.getAd_thumbnailscal());
//        downloadinfo.setAd_thumbnail(adbean.getAd_thumbnail());
//        downloadinfo.setAd_apkurl(adbean.getAd_apkurl());
//        downloadinfo.setAd_packagename(adbean.getAd_packagename());
//        downloadinfo.setAd_isConfirm(adbean.isAd_isConfirm());
//        downloadinfo.setAd_type(adbean.getAd_type());
//        downloadinfo.setAd_versioncode(adbean.getAd_versioncode());
//        downloadinfo.setAd_platform(adbean.getAd_platform());
//        return downloadinfo;
//    }

//    public static HistoryBean adToHistoryBean(ADBean adbean) {
//        HistoryBean downloadinfo = new HistoryBean();
//        downloadinfo.setAd_name(adbean.getAd_name());
//        downloadinfo.setAd_description(adbean.getAd_description());
//        downloadinfo.setAd_iconurl(adbean.getAd_iconurl());
//        downloadinfo.setAd_thumbnail(adbean.getAd_thumbnail());
//        downloadinfo.setAd_iconscal(adbean.getAd_iconscal());
//        downloadinfo.setAd_thumbnailscal(adbean.getAd_thumbnailscal());
//        downloadinfo.setAd_thumbnail(adbean.getAd_thumbnail());
//        downloadinfo.setAd_apkurl(adbean.getAd_apkurl());
//        downloadinfo.setAd_packagename(adbean.getAd_packagename());
//        downloadinfo.setAd_isConfirm(adbean.isAd_isConfirm());
//        downloadinfo.setAd_type(adbean.getAd_type());
//        downloadinfo.setAd_versioncode(adbean.getAd_versioncode());
//        downloadinfo.setAd_platform(adbean.getAd_platform());
//        return downloadinfo;
//    }

    //    public static FavoriteBean adToFavoriteBean(ADBean adbean) {
//        FavoriteBean downloadinfo = new FavoriteBean();
//        downloadinfo.setAd_name(adbean.getAd_name());
//        downloadinfo.setAd_description(adbean.getAd_description());
//        downloadinfo.setAd_iconurl(adbean.getAd_iconurl());
//        downloadinfo.setAd_thumbnail(adbean.getAd_thumbnail());
//        downloadinfo.setAd_iconscal(adbean.getAd_iconscal());
//        downloadinfo.setAd_thumbnailscal(adbean.getAd_thumbnailscal());
//        downloadinfo.setAd_thumbnail(adbean.getAd_thumbnail());
//        downloadinfo.setAd_apkurl(adbean.getAd_apkurl());
//        downloadinfo.setAd_packagename(adbean.getAd_packagename());
//        downloadinfo.setAd_isConfirm(adbean.isAd_isConfirm());
//        downloadinfo.setAd_type(adbean.getAd_type());
//        downloadinfo.setAd_versioncode(adbean.getAd_versioncode());
//        downloadinfo.setAd_platform(adbean.getAd_platform());
//        return downloadinfo;
//    }
//
    public static ShowListBean adToShowListBean(ADBean adbean) {
        ShowListBean videoDataBean = new ShowListBean();
        if (adbean == null) return videoDataBean;
        videoDataBean.setAd_name(adbean.getAd_name());
        videoDataBean.setAd_description(adbean.getAd_description());
        videoDataBean.setAd_iconurl(adbean.getAd_iconurl());
        videoDataBean.setAd_thumbnail(adbean.getAd_thumbnail());
        videoDataBean.setAd_iconscal(adbean.getAd_iconscal());
        videoDataBean.setAd_thumbnailscal(adbean.getAd_thumbnailscal());
        videoDataBean.setAd_apkurl(adbean.getAd_apkurl());
        videoDataBean.setAd_packagename(adbean.getAd_packagename());
        videoDataBean.setAd_isConfirm(adbean.isAd_isConfirm());
        videoDataBean.setAd_type(adbean.getAd_type());
        videoDataBean.setAd_versioncode(adbean.getAd_versioncode());
        videoDataBean.setAd_platform(adbean.getAd_platform());
        return videoDataBean;
    }

    public static MainListBean adToShowListBean2(ADBean adbean) {
        MainListBean videoDataBean = new MainListBean();
        if (adbean == null) return videoDataBean;
        videoDataBean.setAd_name(adbean.getAd_name());
        videoDataBean.setAd_description(adbean.getAd_description());
        videoDataBean.setAd_iconurl(adbean.getAd_iconurl());
        videoDataBean.setAd_thumbnail(adbean.getAd_thumbnail());
        videoDataBean.setAd_iconscal(adbean.getAd_iconscal());
        videoDataBean.setAd_thumbnailscal(adbean.getAd_thumbnailscal());
        videoDataBean.setAd_apkurl(adbean.getAd_apkurl());
        videoDataBean.setAd_packagename(adbean.getAd_packagename());
        videoDataBean.setAd_isConfirm(adbean.isAd_isConfirm());
        videoDataBean.setAd_type(adbean.getAd_type());
        videoDataBean.setAd_versioncode(adbean.getAd_versioncode());
        videoDataBean.setAd_platform(adbean.getAd_platform());
        return videoDataBean;
    }

    public static SearchListBean adToSearchListBean(ADBean adbean) {
        SearchListBean videoDataBean = new SearchListBean();
        if (adbean == null) return videoDataBean;
        videoDataBean.setAd_name(adbean.getAd_name());
        videoDataBean.setAd_description(adbean.getAd_description());
        videoDataBean.setAd_iconurl(adbean.getAd_iconurl());
        videoDataBean.setAd_thumbnail(adbean.getAd_thumbnail());
        videoDataBean.setAd_iconscal(adbean.getAd_iconscal());
        videoDataBean.setAd_thumbnailscal(adbean.getAd_thumbnailscal());
        videoDataBean.setAd_apkurl(adbean.getAd_apkurl());
        videoDataBean.setAd_packagename(adbean.getAd_packagename());
        videoDataBean.setAd_isConfirm(adbean.isAd_isConfirm());
        videoDataBean.setAd_type(adbean.getAd_type());
        videoDataBean.setAd_versioncode(adbean.getAd_versioncode());
        videoDataBean.setAd_platform(adbean.getAd_platform());
        return videoDataBean;
    }

    /*
     * 将自定义广告随机插入到showbeans里面
     * */
    public static List<SearchListBean> getMixSearchListBeans(Context context, List<SearchListBean> historyBeans) {
        List<SearchListBean> retshowBeans = new ArrayList<SearchListBean>();
        if (!isShowSelfAD()) {
            return historyBeans;
        }
        int adCount = (int) (historyBeans.size() * AppConfig.decimal);
        if (adCount == 0 && historyBeans.size() != 0) {
            adCount = 1;//如果获取到了节目数据至少保证一条
        }
        List<ADBean> selfADs = GetSelfADByCount(context, adCount, "yuansheng_count");//随机取adcount条广告

        List<Integer> adIntegerList = GetRandomList(selfADs.size(), historyBeans.size() + selfADs.size());
        int showBeanIndex = 0, selfADIndex = 0;
        for (int i = 0; i < historyBeans.size() + selfADs.size(); i++) {
            if (adIntegerList.contains(i)) {

                retshowBeans.add(AppConfig.adToSearchListBean(selfADs.get(selfADIndex++)));
            } else {

                retshowBeans.add(historyBeans.get(showBeanIndex++));
            }


        }
        return retshowBeans;
    }

    /*
     * 将自定义广告随机插入到集合
     * */
    public static List<MainListBean> getAdMIXShowListBeans(Context context, List<MainListBean> historyBeans) {
        List<MainListBean> retshowBeans = new ArrayList<>();
        if (!isShowSelfAD()) {
            return historyBeans;
        }
        int adCount = (int) (historyBeans.size() * AppConfig.decimal);
        if (adCount == 0 && historyBeans.size() != 0) {
            adCount = 1;//如果获取到了节目数据至少保证一条
        }
        List<ADBean> selfADs = GetSelfADByCount(context, adCount, "yuansheng_count");//随机取adcount条广告

        List<Integer> adIntegerList = GetRandomList(selfADs.size(), historyBeans.size() + selfADs.size());
        int showBeanIndex = 0, selfADIndex = 0;
        for (int i = 0; i < historyBeans.size() + selfADs.size(); i++) {
            if (adIntegerList.contains(i)) {

                retshowBeans.add(AppConfig.adToShowListBean2(selfADs.get(selfADIndex++)));
            } else {

                retshowBeans.add(historyBeans.get(showBeanIndex++));
            }


        }
        return retshowBeans;
    }

    public static List<ShowListBean> getMIXShowListBeans(Context context, List<ShowListBean> historyBeans) {
        List<ShowListBean> retshowBeans = new ArrayList<ShowListBean>();
        if (!isShowSelfAD()) {
            return historyBeans;
        }
        int adCount = (int) (historyBeans.size() * AppConfig.decimal);
        if (adCount == 0 && historyBeans.size() != 0) {
            adCount = 1;//如果获取到了节目数据至少保证一条
        }
        List<ADBean> selfADs = GetSelfADByCount(context, adCount, "yuansheng_count");//随机取adcount条广告

        List<Integer> adIntegerList = GetRandomList(selfADs.size(), historyBeans.size() + selfADs.size());
        int showBeanIndex = 0, selfADIndex = 0;
        for (int i = 0; i < historyBeans.size() + selfADs.size(); i++) {
            if (adIntegerList.contains(i)) {

                retshowBeans.add(AppConfig.adToShowListBean(selfADs.get(selfADIndex++)));
            } else {

                retshowBeans.add(historyBeans.get(showBeanIndex++));
            }


        }
        return retshowBeans;
    }
//
//    /*
//         * 将自定义广告随机插入到showbeans里面
//         * */
//    public static List<HistoryBean> getMIXHistoryBeans(Context context, List<HistoryBean> historyBeans) {
//        if (!isShowSelfAD())
//            return historyBeans;
//        int adCount = (int) (historyBeans.size() * AppConfig.decimal);
//        if (adCount == 0 && historyBeans.size() != 0)
//            adCount = 1;//如果获取到了节目数据至少保证一条
//        List<ADBean> selfADs = GetSelfADByCount(context, adCount, "yuansheng_count");//随机取adcount条广告
//        List<HistoryBean> retshowBeans = new ArrayList<HistoryBean>();
//        List<Integer> adIntegerList = GetRandomList(selfADs.size(), historyBeans.size() + selfADs.size());
//        int showBeanIndex = 0, selfADIndex = 0;
//        for (int i = 0; i < historyBeans.size() + selfADs.size(); i++) {
//            if (adIntegerList.contains(i)) {
//
//                retshowBeans.add(adToHistoryBean(selfADs.get(selfADIndex++)));
//            } else {
//
//                retshowBeans.add(historyBeans.get(showBeanIndex++));
//            }
//
//
//        }
//        return retshowBeans;
//    }
//
//    /*
//      * 将自定义广告随机插入到showbeans里面
//      * */
//    public static List<DownloadInfo> getMIXDownloadListBeans(Context context, List<DownloadInfo> downloadInfos) {
//        if (!isShowSelfAD())
//            return downloadInfos;
//        int adCount = (int) (downloadInfos.size() * AppConfig.decimal);
//        if (adCount == 0 && downloadInfos.size() != 0)
//            adCount = 1;//如果获取到了节目数据至少保证一条
//        List<ADBean> selfADs = GetSelfADByCount(context, adCount, "yuansheng_count");//随机取adcount条广告
//        List<DownloadInfo> retshowBeans = new ArrayList<DownloadInfo>();
//        List<Integer> adIntegerList = GetRandomList(selfADs.size(), downloadInfos.size() + selfADs.size());
//        int showBeanIndex = 0, selfADIndex = 0;
//        for (int i = 0; i < downloadInfos.size() + selfADs.size(); i++) {
//            if (adIntegerList.contains(i)) {
//
//                retshowBeans.add(adToDownloadInfo(selfADs.get(selfADIndex++)));
//            } else {
//
//                retshowBeans.add(downloadInfos.get(showBeanIndex++));
//            }
//
//
//        }
//        return retshowBeans;
//    }
//
//    /*
//    * 将自定义广告随机插入到showbeans里面
//    * */
//    public static List<VideoDataListBean> getMIXVideoDataListBeans(Context context, List<VideoDataListBean> showBeans) {
//        if (!isShowSelfAD())
//            return showBeans;
//        int adCount = (int) (showBeans.size() * AppConfig.decimal);
//        if (adCount == 0 && showBeans.size() != 0)
//            adCount = 1;//如果获取到了节目数据至少保证一条
//        List<ADBean> selfADs = GetSelfADByCount(context, adCount, "yuansheng_count");//随机取adcount条广告
//        List<VideoDataListBean> retshowBeans = new ArrayList<VideoDataListBean>();
//        List<Integer> adIntegerList = GetRandomList(selfADs.size(), showBeans.size() + selfADs.size());
//        int showBeanIndex = 0, selfADIndex = 0;
//        for (int i = 0; i < showBeans.size() + selfADs.size(); i++) {
//            if (adIntegerList.contains(i)) {
//
//                retshowBeans.add(adToVideoDataListBean(selfADs.get(selfADIndex++)));
//            } else {
//
//                retshowBeans.add(showBeans.get(showBeanIndex++));
//            }
//
//
//        }
//        return retshowBeans;
//    }

//    public static ADBean toADBean(VideoDataListBean videoDataListBean) {
//
//        ADBean bean = new ADBean();
//        bean.setAd_name(videoDataListBean.getName());
//        bean.setAd_versioncode(videoDataListBean.getVersioncode());
//        bean.setAd_apkurl(videoDataListBean.getApkurl());
//        bean.setAd_packagename(videoDataListBean.getPackagename());
//        return bean;
//    }

    public static void openAD(final Context context, final ADBean adbean, String tag) {//如果本条是广告
        if (adbean == null) return;
        Map<String, String> map_ekv = new HashMap<String, String>();
        map_ekv.put("click", adbean.getAd_name());
        MobclickAgent.onEvent(context, tag, map_ekv);


        int type = adbean.getAd_type();
        if (type == 1)//下载
        {
            if (PackageUtil.isInstallApp(context, adbean.getAd_packagename()))//已经安装直接打开
            {
                PackageUtil.startApp(context, adbean.getAd_packagename());
                return;
            }
            if (adbean.isAd_isConfirm()) {
                new AlertDialog.Builder(context).setTitle("确定下载：" + adbean.getAd_name() + "?")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("下载", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                if (DownLoaderAPK.getInstance(context).addDownload(adbean)) {
                                    Toast.makeText(context, "开始下载:" + adbean.getAd_name(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, adbean.getAd_name() + " 已经在下载了:", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();
            } else {

                if (DownLoaderAPK.getInstance(context).addDownload(adbean)) {
                    Toast.makeText(context, "开始下载:" + adbean.getAd_name(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, adbean.getAd_name() + " 已经在下载了:", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (type == 2)//打开网页
        {
            if (adbean.getAd_apkurl().contains(".taobao.com"))//是淘宝链接
            {
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW"); //
                    String url = "";
                    if (adbean.getAd_apkurl().startsWith("http://")) {
                        url = adbean.getAd_apkurl().replaceFirst("http://", "taobao://");
                    } else {
                        url = adbean.getAd_apkurl().replaceFirst("https://", "taobao://");
                    }
                    Uri uri = Uri.parse(url);
                    intent.setData(uri);
                    context.startActivity(intent);
                } catch (Exception ex) {
                    PackageUtil.qidongLiulanqi((Activity) context, adbean.getAd_apkurl());
                }
            } else if (adbean.getAd_apkurl().contains("item.jd.com/"))//是淘宝链接
            {
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW"); //
                    int begin = adbean.getAd_apkurl().indexOf("item.jd.com/") + "item.jd.com/".length();
                    int end = adbean.getAd_apkurl().indexOf(".html");
                    String id = adbean.getAd_apkurl().substring(begin, end);
                    String url = "openapp.jdmobile://virtual?params=%7B%22sourceValue%22:%220_productDetail_97%22,%22des%22:%22productDetail%22,%22skuId%22:%22" + id + "%22,%22category%22:%22jump%22,%22sourceType%22:%22PCUBE_CHANNEL%22%7D";

                    Uri uri = Uri.parse(url);
                    intent.setData(uri);
                    context.startActivity(intent);
                } catch (Exception ex) {
                    PackageUtil.qidongLiulanqi((Activity) context, adbean.getAd_apkurl());
                }
            } else {

                PackageUtil.qidongLiulanqi((Activity) context, adbean.getAd_apkurl());
            }
        } else if (type == 3)//打开公众号
        {
            WXGZHBean wxgzhbean = new WXGZHBean();
            if (AppConfig.wxgzhBeans != null && AppConfig.wxgzhBeans.size() > 0) {
                wxgzhbean.type = AppConfig.wxgzhBeans.get(0).type;
            } else {
                wxgzhbean.type = "pengyouquan,pengyou,putong";
            }

            wxgzhbean.thumb = adbean.getAd_thumbnail();
            wxgzhbean.displayName = adbean.getAd_name();
            wxgzhbean.id = adbean.getAd_packagename();
            wxgzhbean.url = adbean.getAd_apkurl();
            wxgzhbean.introduction = adbean.getAd_description();
//            Intent intent = new Intent(context, GZHAddActivity.class);
//            intent.putExtra("wxgzhbean", wxgzhbean);
//            context.startActivity(intent);
        } else {
            PackageUtil.qidongLiulanqi((Activity) context, adbean.getAd_apkurl());
        }

    }

    public static boolean isCopyZhikouling() {
        if (configBean == null)
            return false;
        for (String version :
                configBean.nozhikouling.split(",")) {
            if (version.equals(versioncode))
                return false;
        }
        return true;
    }
}
