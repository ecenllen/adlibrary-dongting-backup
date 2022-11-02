package com.yingyongduoduo.ad;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.CSJAdError;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.yingyongduoduo.ad.bean.ADBean;
import com.yingyongduoduo.ad.config.AppConfig;
import com.yingyongduoduo.ad.dialog.GDTMuBanTuiPingDialog;
import com.yingyongduoduo.ad.dialog.SelfCPDialog;
import com.yingyongduoduo.ad.dialog.SelfTuiPingDialog;
import com.yingyongduoduo.ad.dialog.UpdateDialog;
import com.yingyongduoduo.ad.interfaceimpl.KPAdListener;
import com.yingyongduoduo.ad.interfaceimpl.SelfBannerAdListener;
import com.yingyongduoduo.ad.interfaceimpl.SelfBannerView;
import com.yingyongduoduo.ad.interfaceimpl.SelfKPAdListener;
import com.yingyongduoduo.ad.interfaceimpl.SelfKPView;
import com.yingyongduoduo.ad.utils.ScreenUtils;

import java.util.HashMap;
import java.util.List;


public class ADControl {

    private static int score = 0;

    public static long lastshowadTime = 0;

    public static Boolean isonshow = false;
    public static boolean ISGiveHaoping = false;
    private static HashMap<String, String> giveHaoping = new HashMap<String, String>();

    //展示5分好评广告，首次进来不展示，和插屏广告戳开，隔间10秒
    private static long divideTime = 8L * 1000L;
    private static long showadTimeDuration = 120 * 1000;
    private long lastshowHaopingTime = System.currentTimeMillis();

    public static String oldADVersition = "";

    public void ChangeTVAddrVersion(Context context, String newVersion) {
        SharedPreferences mSettings = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        Editor editor = mSettings.edit();
        editor.putString("addrversion", newVersion);
        editor.apply();
        ADControl.oldADVersition = newVersion;
    }

    private void ShowCSJKP(final Activity context, final RelativeLayout adsParent, View skip_view, final KPAdListener kpAdListener, String appid, String adplaceid) {
        //创建TTAdNative对象，createAdNative(Context context) context需要传入Activity对象
        TTAdNative mTTAdNative = TTAdManagerHolder.get().createAdNative(context);

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(adplaceid)
                .setImageAcceptedSize(ScreenUtils.getScreenWidth(context), ScreenUtils.getScreenHeight(context) - ScreenUtils.getVirtualBarHeigh(context))
                .setAdLoadType(PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                .build();

        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.CSJSplashAdListener() {
            @Override
            public void onSplashLoadSuccess() {

            }

            @Override
            public void onSplashLoadFail(CSJAdError csjAdError) {
                //开发者处理跳转到APP主页面逻辑
                kpAdListener.onAdFailed(csjAdError.getMsg() + "");
            }

            @Override
            public void onSplashRenderSuccess(CSJSplashAd csjSplashAd) {
                if (csjSplashAd == null) {
                    kpAdListener.onAdDismissed();
                    return;
                }
                //获取SplashView
                View view = csjSplashAd.getSplashView();
                if (view != null && adsParent != null && !context.isFinishing()) {
                    adsParent.removeAllViews();
                    //把SplashView 添加到ViewGroup中,注意开屏广告view：width =屏幕宽；height >=75%屏幕高
                    adsParent.addView(view);
                    //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
                    //ad.setNotAllowSdkCountdown();
                } else {
                    //开发者处理跳转到APP主页面逻辑
                    kpAdListener.onAdDismissed();
                }

                csjSplashAd.setSplashAdListener(new CSJSplashAd.SplashAdListener() {
                    @Override
                    public void onSplashAdShow(CSJSplashAd csjSplashAd) {
                        kpAdListener.onAdPresent();
                    }

                    @Override
                    public void onSplashAdClick(CSJSplashAd csjSplashAd) {
                        kpAdListener.onAdClick();
                    }

                    @Override
                    public void onSplashAdClose(CSJSplashAd csjSplashAd, int i) {
                        //开发者处理跳转到APP主页面逻辑
                        kpAdListener.onAdDismissed();
                    }
                });
            }

            @Override
            public void onSplashRenderFail(CSJSplashAd csjSplashAd, CSJAdError csjAdError) {
                //开发者处理跳转到APP主页面逻辑
                kpAdListener.onAdFailed(csjAdError.getMsg() + "");
            }
        }, 4000);

//        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
//            //请求广告失败
//            @Override
//            @MainThread
//            public void onError(int code, String message) {
//                //开发者处理跳转到APP主页面逻辑
//                kpAdListener.onAdFailed(message + "");
//            }
//
//            //请求广告超时
//            @Override
//            @MainThread
//            public void onTimeout() {
//                //开发者处理跳转到APP主页面逻辑
//                kpAdListener.onAdFailed("请求广告超时");
//            }
//
//            //请求广告成功
//            @Override
//            @MainThread
//            public void onSplashAdLoad(TTSplashAd ad) {
//                if (ad == null) {
//                    kpAdListener.onAdDismissed();
//                    return;
//                }
//                //获取SplashView
//                View view = ad.getSplashView();
//                if (view != null && adsParent != null && !context.isFinishing()) {
//                    adsParent.removeAllViews();
//                    //把SplashView 添加到ViewGroup中,注意开屏广告view：width =屏幕宽；height >=75%屏幕高
//                    adsParent.addView(view);
//                    //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
//                    //ad.setNotAllowSdkCountdown();
//                } else {
//                    //开发者处理跳转到APP主页面逻辑
//                    kpAdListener.onAdDismissed();
//                }
//
//                //设置SplashView的交互监听器
//                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
//                    @Override
//                    public void onAdClicked(View view, int type) {
//                        kpAdListener.onAdClick();
//                    }
//
//                    @Override
//                    public void onAdShow(View view, int type) {
//                        kpAdListener.onAdPresent();
//                    }
//
//                    @Override
//                    public void onAdSkip() {
//                        //开发者处理跳转到APP主页面逻辑
//                        kpAdListener.onAdDismissed();
//                    }
//
//                    @Override
//                    public void onAdTimeOver() {
//                        //开发者处理跳转到APP主页面逻辑
//                        kpAdListener.onAdDismissed();
//                    }
//                });
//            }
//        });
    }

    private void ShowGDTKP(final Activity context, final RelativeLayout adsParent, View skip_view, final KPAdListener kpAdListener, String appid, String adplaceid) {
        SplashADListener listener = new SplashADListener() {

            @Override
            public void onADDismissed() {
                kpAdListener.onAdDismissed();
            }

            @Override
            public void onNoAD(AdError adError) {

                kpAdListener.onAdFailed(adError != null ? adError.getErrorMsg() : "");
            }

            @Override
            public void onADPresent() {
                kpAdListener.onAdPresent();
            }

            @Override
            public void onADClicked() {
                kpAdListener.onAdClick();
            }

            @Override
            public void onADTick(long l) {
                kpAdListener.onAdTick(l);
            }

            @Override
            public void onADExposure() {

            }

            @Override
            public void onADLoaded(long l) {

            }
        };
        SplashAD splashAD = new SplashAD((Activity) context, adplaceid, listener, 0);
        splashAD.fetchAndShowIn(adsParent);

    }

    private void ShowSelfKP(final Context context, RelativeLayout adsParent, final KPAdListener kpAdListener) {

        SelfKPAdListener listener = new SelfKPAdListener() {
            @Override
            public void onAdDismissed(ADBean bean) {//广告展示完毕
                kpAdListener.onAdDismissed();
            }

            @Override
            public void onAdFailed(ADBean bean) {//广告获取失败
                kpAdListener.onAdFailed("");
            }

            @Override
            public void onAdPresent(ADBean bean) {//广告开始展示
                kpAdListener.onAdPresent();
//                if (bean != null && !TextUtils.isEmpty(bean.getAd_name())) {
//                    Map<String, String> map_ekv = new HashMap<String, String>();
//                    map_ekv.put("show", bean.getAd_name());
//                    MobclickAgent.onEvent(context, "kp_count", map_ekv);
//                }
            }

            @Override
            public void onAdClick(ADBean bean) {//广告被点击
                kpAdListener.onAdClick();
//                if (bean != null && !TextUtils.isEmpty(bean.getAd_name())) {
//                    Map<String, String> map_ekv = new HashMap<String, String>();
//                    map_ekv.put("click", bean.getAd_name());
//                    MobclickAgent.onEvent(context, "kp_count", map_ekv);
//                }
            }
        };
        SelfKPView selfKPView = new SelfKPView(context);
        selfKPView.setADListener(listener);
        adsParent.removeAllViews();
        adsParent.addView(selfKPView);
    }

    //初始化广点通退屏广告
    public static Boolean InitGDTMuBanTP(Context context) {
        if (AppConfig.isShowTP())//展示退屏广告
        {
            String kpType = AppConfig.getTPType();//获取开屏广告类型，baidu，gdt，google
            String kp_String = AppConfig.configBean.ad_tp_idMap.get(kpType);
            if (!TextUtils.isEmpty(kp_String)) {
                String[] a = kp_String.split(",");
                if (a.length == 2) {
                    String appid = a[0];
                    String adplaceid = a[1];
                    if ("gdtmb".equals(kpType)) {
                        GDTMuBanTuiPingDialog.Init(context, appid, adplaceid);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else//不展示开屏广告
        {
            return false;
        }

    }

    //初始化广点通退屏广告
    public Boolean ShowTPAD(Context context) {
        if (AppConfig.isShowTP())//展示开屏广告
        {
            String kpType = AppConfig.getTPType();//获取开屏广告类型，baidu，gdt，google
            String kp_String = AppConfig.configBean.ad_tp_idMap.get(kpType);
            if (!TextUtils.isEmpty(kpType) && "self".equals(kpType)) {//退屏类型为自家的
                SelfTuiPingDialog sfCP = new SelfTuiPingDialog(context);
                sfCP.show();
                return false;
            } else if (!TextUtils.isEmpty(kp_String)) {//并非自家的，来自广点通，百度等，目前只有广点通
                String[] a = kp_String.split(",");
                if (a.length == 2) {
                    String appid = a[0];
                    String adplaceid = a[1];
                    if ("gdtmb".equals(kpType)) {
                        GDTMuBanTuiPingDialog sfCP = new GDTMuBanTuiPingDialog(context);
                        sfCP.show();
                        return true;
                    } else {//有两个id，但是又不是广点通
                        SelfTuiPingDialog sfCP = new SelfTuiPingDialog(context, null);
                        sfCP.show();
                        return false;
                    }
                } else {//id没有两个，则暂时表示配置有问题，如果以后某个平台id只有一个，则重新写该方法
                    SelfTuiPingDialog sfCP = new SelfTuiPingDialog(context, null);
                    sfCP.show();
                    return false;
                }
            } else {//如果返回的id为空，又不展示自家广告，这种情况可能是后台配置错误，则不展示广告
                SelfTuiPingDialog sfCP = new SelfTuiPingDialog(context, null);
                sfCP.show();
                return true;
            }
        } else//不展示退屏广告
        {
            SelfTuiPingDialog sfCP = new SelfTuiPingDialog(context, null);
            sfCP.show();
            return false;
        }

    }

    public void ShowKp(Activity context, RelativeLayout adsParent, View skipView, final KPAdListener kpAdListener) {
        if (AppConfig.isShowKP())//展示开屏广告
        {
            String kpType = AppConfig.getKPType();//获取开屏广告类型，baidu，gdt，google
            String kp_String = AppConfig.configBean.ad_kp_idMap.get(kpType);

            if (!TextUtils.isEmpty(kp_String)) {
                String[] a = kp_String.split(",");
                if (a.length == 2) {
                    String appid = a[0];
                    String adplaceid = a[1];
                    if ("baidu".equals(kpType)) {
                        ShowSelfKP(context, adsParent, kpAdListener);
                    } else if ("csj".equals(kpType)) {
                        ShowCSJKP(context, adsParent, skipView, kpAdListener, appid, adplaceid);
                    } else if ("gdt".equals(kpType)) {
                        ShowGDTKP(context, adsParent, skipView, kpAdListener, appid, adplaceid);
                    } else {
                        kpAdListener.onAdFailed("其他不支持广告类型" + kp_String);
                    }
                } else {
                    kpAdListener.onAdFailed("后台获取开屏广告的id为" + kp_String);
                }
            } else {
                Log.i("hehe100", "else");
                ShowSelfKP(context, adsParent, kpAdListener);
            }
        } else//不展示开屏广告
        {
            kpAdListener.onAdFailed("后台不展示开屏广告");
        }

    }

    private UnifiedInterstitialAD interAd;

    private void ShowCSJCP2(final Activity context, final String appid, final String adplaceid) {
        if (context == null || context.isFinishing()) return;
        TTAdNative mTTAdNative = TTAdManagerHolder.get().createAdNative(context);

        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(adplaceid)
                //此次加载广告的用途是实时加载，当用来作为缓存时，请使用：TTAdLoadType.PRELOAD
                .setAdLoadType(TTAdLoadType.LOAD)
                .build();

        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                if (context == null || context.isFinishing() || context.isDestroyed()) return;
                ShowSelfCP(context);
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                if (context == null || context.isFinishing() || context.isDestroyed()) return;
                if (ad != null)
                    ad.showFullScreenVideoAd(context);
            }

            @Override

            public void onFullScreenVideoCached() {

            }

            @Override

            public void onFullScreenVideoCached(TTFullScreenVideoAd ad) {
                Log.e("ADControl", "Callback --> onFullScreenVideoCached");
//                ad.showFullScreenVideoAd(FullScreenVideoActivity.this, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
            }
        });


    }

    public void showShiPingAD(final Activity activity, KPAdListener kpAdListener) {
        boolean isShow = false;
        if (AppConfig.isShowShiping()) {//展示开屏广告
            if (activity != null) {
                String spType = AppConfig.getShipingType();
                String banner_String = AppConfig.configBean.ad_shiping_idMap.get(spType);
                if (!TextUtils.isEmpty(banner_String)) {
                    String[] a = banner_String.split(",");
                    if (a.length == 2) {
                        String appid = a[0];
                        String adplaceid = a[1];
                        isShow = true;
                        if ("csj".equals(spType)) {
                            showCSJFullVideoAd(activity, appid, adplaceid, kpAdListener);
                        } else {
                            if (kpAdListener != null)
                                kpAdListener.onAdFailed("没有匹配的广告");
                        }
                    }
                }

            }
        }

        if (!isShow) {
            if (kpAdListener != null)
                kpAdListener.onAdFailed("没有匹配的广告");
        }

    }

    private TTFullScreenVideoAd mTTFullVideoAd;
    private boolean mHasShowDownloadActive = false;

    private void showCSJFullVideoAd(final Activity activity, String appid, String adplaceid, final KPAdListener kpAdListener) {

        final TTAdNative mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(adplaceid)
                //模板广告需要设置期望个性化模板广告的大小,单位dp,激励视频场景，只要设置的值大于0即可
                //且仅是模板渲染的代码位ID使用，非模板渲染代码位切勿使用
                .setExpressViewAcceptedSize(500, 500)
                .setSupportDeepLink(true)
                .setOrientation(TTAdConstant.VERTICAL)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .setAdLoadType(PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
//                .setDownloadType(TTAdConstant.DOWNLOAD_TYPE_POPUP)
                .build();

        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            //请求广告失败
            @Override
            public void onError(int code, String message) {
                kpAdListener.onAdFailed(message + "");
            }

            //广告物料加载完成的回调
            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                if (kpAdListener != null)
                    kpAdListener.onAdPresent();

                mTTFullVideoAd = ad;
                mTTFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                    }

                    @Override
                    public void onAdVideoBarClick() {
                    }

                    @Override
                    public void onAdClose() {
                        if (kpAdListener != null)
                            kpAdListener.onAdDismissed();
                    }

                    @Override
                    public void onVideoComplete() {
                    }

                    @Override
                    public void onSkippedVideo() {
                    }

                });

                ad.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);

                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            Toast.makeText(activity, "下载中，点击下载区域暂停", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        Toast.makeText(activity, "下载暂停，点击下载区域继续", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        Toast.makeText(activity, "下载失败，点击下载区域重新下载", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                        Toast.makeText(activity, "下载完成，点击下载区域重新下载", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                        Toast.makeText(activity, "安装完成，点击下载区域打开", Toast.LENGTH_SHORT).show();
                    }
                });
                //展示广告，并传入广告展示的场景
                mTTFullVideoAd.showFullScreenVideoAd(activity, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
                mTTFullVideoAd = null;
            }

            //广告视频本地加载完成的回调，接入方可以在这个回调后直接播放本地视频
            @Override
            public void onFullScreenVideoCached() {

            }

            @Override
            public void onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd) {
            }
        });

    }


    private void ShowGDTCP2(final Activity context, final String appid, final String adplaceid) {

        if (context == null || context.isFinishing()) return;
        interAd = getIAD(context, appid, adplaceid, new UnifiedInterstitialADListener() {
            @Override
            public void onADReceive() {
                if (context == null || context.isFinishing() || interAd == null || !interAd.isValid())
                    return;
                interAd.show();
            }

            @Override
            public void onVideoCached() {

            }

            @Override
            public void onNoAD(AdError adError) {
                lastshowadTime = 0;
                ShowSelfCP(context);
            }


            @Override
            public void onADOpened() {

            }

            @Override
            public void onADExposure() {

            }

            @Override
            public void onADClicked() {

            }

            @Override
            public void onADLeftApplication() {

            }

            @Override
            public void onADClosed() {

            }

            @Override
            public void onRenderSuccess() {

            }

            @Override
            public void onRenderFail() {

            }
        });

        interAd.loadAD();
    }

    private UnifiedInterstitialAD getIAD(Activity context, String appid, String posId, UnifiedInterstitialADListener listener) {
        if (interAd != null && interAd.isValid()) {
            interAd.close();
            interAd.destroy();
            interAd = null;
        }
        interAd = new UnifiedInterstitialAD(context, posId, listener);
        return interAd;
    }

    private void ShowSelfCP(final Context context) {

        SelfCPDialog sfCP = new SelfCPDialog(context);
        sfCP.setADListener(new SelfBannerAdListener() {
            @Override
            public void onAdClick(ADBean adBean) {
            }

            @Override
            public void onAdFailed() {

            }

            @Override
            public void onADReceiv(ADBean adBean) {
            }
        });
        sfCP.show();

    }

    public void ShowCp(Activity context) {
        ShowCp(context, false);
    }

    public void ShowCp(Activity context, boolean isMustShow) {
        if (AppConfig.isShowCP())//展示开屏广告
        {
            if (!isMustShow) {
                if (System.currentTimeMillis() - lastshowadTime < showadTimeDuration) {
                    System.out.println("广告时间没到" + (System.currentTimeMillis() - lastshowadTime));
                    return;
                }
            }
            lastshowadTime = System.currentTimeMillis();
            String cpType = AppConfig.getCPType();//获取开屏广告类型，baidu，gdt，google
            String kp_String = AppConfig.configBean.ad_cp_idMap.get(cpType);

            if (!TextUtils.isEmpty(kp_String)) {
                String[] a = kp_String.split(",");
                if (a.length == 2) {
                    String appid = a[0];
                    String adplaceid = a[1];
                    if ("baidu".equals(cpType)) {
                        ShowSelfCP(context);
                    } else if ("csj2".equals(cpType)) {
                        ShowCSJCP2(context, appid, adplaceid);
                    } else if ("gdt2".equals(cpType)) {
                        ShowGDTCP2(context, appid, adplaceid);
                    } else if ("self".equals(cpType)) {
                        ShowSelfCP(context);
                    } else {
                        // kpAdListener.onAdFailed("其他不支持广告类型" + kp_String);
                    }
                } else {
                    // kpAdListener.onAdFailed("后台获取开屏广告的id为" + kp_String);
                }
            } else {
                ShowSelfCP(context);
            }
        } else//不展示开屏广告
        {
            //  kpAdListener.onAdFailed("后台不展示开屏广告");
        }

    }

    private void addCSJBanner(final LinearLayout lyt, final Activity context, final String appid, final String adplaceid) {
        if (mTTAd != null) {
            if (lyt != null)
                lyt.removeAllViews();
            mTTAd.destroy();
            mTTAd = null;
        }
        if (context == null || context.isFinishing()) return;
        try {

            TTAdNative mTTAdNative = TTAdManagerHolder.get().createAdNative(context);

            //step4:创建广告请求参数AdSlot,具体参数含义参考文档
            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(adplaceid) //广告位id
                    .setAdCount(1) //请求广告数量为1到3条
                    .setExpressViewAcceptedSize(ScreenUtils.getScreenWidth(context), 60) //期望模板广告view的size,单位dp
//                    .setDownloadType(TTAdConstant.DOWNLOAD_TYPE_POPUP) // 应用每次下载都需要触发弹窗披露应用信息（不含跳转商店的场景），该配置优先级高于下载网络弹窗配置；
                    .build();
            //step5:请求广告，对请求回调的广告作渲染处理
            mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
                @Override
                public void onError(int code, String message) {
                    if (lyt != null) {
                        lyt.removeAllViews();
                        addSelfBanner(lyt, context);
                    }
                }

                @Override
                public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                    if (ads == null || ads.size() == 0) {
                        return;
                    }
                    mTTAd = ads.get(0);
                    if (mTTAd == null) {
                        return;
                    }
                    mTTAd.setSlideIntervalTime(30 * 1000);
                    mTTAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                        @Override
                        public void onAdClicked(View view, int type) {
                        }

                        @Override
                        public void onAdShow(View view, int type) {
                        }

                        @Override
                        public void onRenderFail(View view, String msg, int code) {
                        }

                        @Override
                        public void onRenderSuccess(View view, float width, float height) {
                            //返回view的宽高 单位 dp
                            if (lyt != null) {
                                lyt.removeAllViews();
                                lyt.addView(view);
                            }
                        }
                    });
                    mTTAd.render();


                    //使用默认模板中默认dislike弹出样式
                    mTTAd.setDislikeCallback(context, new TTAdDislike.DislikeInteractionCallback() {
                        @Override
                        public void onShow() {

                        }

                        @Override
                        public void onSelected(int position, String value, boolean enforce) {
                            AppConfig.isShowBanner = false;
                            if (lyt != null)
                                lyt.removeAllViews();
                            //用户选择不喜欢原因后，移除广告展示
                            if (enforce) {
//                                TToast.show(mContext, "模版Banner 穿山甲sdk强制将view关闭了");
                            }
                        }

                        @Override
                        public void onCancel() {
                        }

                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addGDTBanner2(final LinearLayout lyt, final Activity context, final String appid, final String adplaceid) {

        if (unifiedBannerView != null && unifiedBannerView.isValid()) {
            Log.e("ADControl", "banner广告还有效");
            return;
        }

        if (unifiedBannerView != null) {
            if (lyt != null)
                lyt.removeAllViews();
            unifiedBannerView.destroy();
            unifiedBannerView = null;
        }

        if (context == null || context.isFinishing()) return;
        try {
            unifiedBannerView = new UnifiedBannerView(context, adplaceid, new UnifiedBannerADListener() {
                @Override
                public void onNoAD(AdError adError) {
                    addSelfBanner(lyt, context);
                }

                @Override
                public void onADReceive() {

                }

                @Override
                public void onADExposure() {

                }

                @Override
                public void onADClosed() {
                    AppConfig.isShowBanner = false;
                    if (lyt != null) {
                        lyt.removeAllViews();
                    }
                }

                @Override
                public void onADClicked() {
                    System.out.println("广点通广告被点击");
                }

                @Override
                public void onADLeftApplication() {

                }

            });
            if (lyt != null)
                lyt.addView(unifiedBannerView, getUnifiedBannerLayoutParams(context));
            // 注意：如果开发者的banner不是始终展示在屏幕中的话，请关闭自动刷新，否则将导致曝光率过低。
            unifiedBannerView.loadAD();

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    /**
     * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
     *
     * @return
     */
    private LinearLayout.LayoutParams getUnifiedBannerLayoutParams(Activity context) {
        Point screenSize = new Point();
        context.getWindowManager().getDefaultDisplay().getSize(screenSize);
        return new LinearLayout.LayoutParams(screenSize.x, Math.round(screenSize.x / 6.4F));
    }

    public void addGoogleBanner(final LinearLayout lyt, final Activity context, String appid, String adplaceid) {
        lyt.removeAllViews();
    }

    public void addSelfBanner(LinearLayout lyt, final Activity context) {
        if (lyt == null)
            return;

        lyt.removeAllViews();
        if (context == null || context.isFinishing()) return;
        try {
            SelfBannerView bv = new SelfBannerView(context);
            bv.setADListener(new SelfBannerAdListener() {
                @Override
                public void onAdClick(final ADBean adBean) {
//                    if(context != null && !context.isFinishing()) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
//                                .setMessage(adBean.getAd_name() + "\n" + adBean.getAd_description())
//                                .setTitle("下载")
//                                .setPositiveButton("下载", new OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        AppConfig.openAD(context, adBean, "banner_count");
//                                    }
//                                })
//                                .setNegativeButton("取消", null);
//                        AlertDialog alertDialog = builder.create();
//                        alertDialog.setCanceledOnTouchOutside(false);
//                        alertDialog.show();
//                    }

                    AppConfig.openAD(context, adBean, "banner_count");

                }

                @Override
                public void onAdFailed() {

                }

                @Override
                public void onADReceiv(ADBean adBean) {
//                    if (adBean != null && !TextUtils.isEmpty(adBean.getAd_name())) {
//                        Map<String, String> map_ekv = new HashMap<String, String>();
//                        map_ekv.put("show", adBean.getAd_name());
//                        MobclickAgent.onEvent(context, "banner_count", map_ekv);
//                        System.out.println("广告被展示:"+adBean.getAd_name());
//                    }
                }
            });
            lyt.addView(bv);

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    private UnifiedBannerView unifiedBannerView;
    private TTNativeExpressAd mTTAd;

    public void addAd(LinearLayout lyt, Activity activity) {
        addBannerAd(lyt, activity);
    }

    public void addBannerAd(LinearLayout lyt, Activity context) {
        ShowCp(context);
        homeGet5Score(context);
        if (AppConfig.isShowBanner() && lyt != null)//展示广告条广告
        {
            String bannerType = AppConfig.getBannerType();//获取开屏广告类型，baidu，gdt，google
            String banner_String = AppConfig.configBean.ad_banner_idMap.get(bannerType);

            if (!TextUtils.isEmpty(banner_String)) {
                String[] a = banner_String.split(",");
                if (a.length == 2) {
                    String appid = a[0];
                    String adplaceid = a[1];
                    if ("baidu".equals(bannerType)) {
                        addSelfBanner(lyt, context);
                    } else if ("csj".equals(bannerType)) {
                        addCSJBanner(lyt, context, appid, adplaceid);
                    } else if ("gdt2".equals(bannerType)) {
                        addGDTBanner2(lyt, context, appid, adplaceid);
                    } else if ("google".equals(bannerType)) {
                        addGoogleBanner(lyt, context, appid, adplaceid);
                    } else if ("self".equals(bannerType)) {
                        addSelfBanner(lyt, context);
                    } else {
//                        kpAdListener.onAdFailed("其他不支持广告类型" + kp_String);
                    }
                } else {

//                    kpAdListener.onAdFailed("后台获取开屏广告的id为" + kp_String);
                }
            } else {
                addSelfBanner(lyt, context);
            }
        } else//不展示banner
        {
//            kpAdListener.onAdFailed("后台不展示开屏广告");
        }

    }

    public void setISGiveHaoping(Context context, Boolean isgivehaoping) {
        ISGiveHaoping = isgivehaoping;
        SharedPreferences mSettings = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE); //
        Editor editor = mSettings.edit();
        editor.putBoolean("ISGiveHaoping", true);
        editor.apply();
    }

    public boolean getIsGiveHaoping(Context context) {
        SharedPreferences mSettings = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        ISGiveHaoping = mSettings.getBoolean("ISGiveHaoping", false);
        return ISGiveHaoping;
    }

    public void setScore(Context context, int score) {
        ADControl.score = score;
        SharedPreferences mSettings = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        Editor editor = mSettings.edit();
        editor.putInt("score", ADControl.score);
        editor.apply();
    }

    public static int getScore(Context context) {
        SharedPreferences mSettings = context.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        return mSettings.getInt("score", -1);

    }

    public void homeGet5Score(final Activity context) {

        if (getIsGiveHaoping(context)) {
            return;
        }
        if (!AppConfig.isShowHaoPing()) {
            return;
        }

        if (isonshow)
            return;

        if (System.currentTimeMillis() - lastshowHaopingTime < showadTimeDuration) {
            Log.i("广告时间没到", (System.currentTimeMillis() - lastshowHaopingTime) + "");
            return;
        }

        lastshowHaopingTime = System.currentTimeMillis();
        isonshow = true;

        new AlertDialog.Builder(context).setTitle("意见或建议")
                .setMessage("\t\t大家对本软件有意见或建议，欢迎通过评论区给我们留言，我们会根据你的要求进行改进，谢谢！")
                .setPositiveButton("给个好评", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setISGiveHaoping(context, true);
                        goodPinglun(context);
                        isonshow = false;
                    }
                }).setNegativeButton("以后再说", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isonshow = false;
            }
        }).setCancelable(false).show();
    }

    public boolean update(Context context) {
        if (AppConfig.isShowUpdate()) {
            int currentVersion = 0;
            try {
                // ---get the package info---
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                currentVersion = pi.versionCode;
                if (currentVersion < AppConfig.configBean.updatemsg.versioncode) {
                    UpdateDialog dg = new UpdateDialog(context);
                    dg.show();
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.e("VersionInfo", "Exception", e);
            }
            return false;

        } else {
            return false;
        }
    }

    /*
     * 加入QQ群的代码
     * */
    public boolean joinQQGroup(Context context) {
        try {
            String key = AppConfig.publicConfigBean.qqKey;
            if (key == null || "".equals(key)) {
                Toast.makeText(context, "请手工添加QQ群:286239217", Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent();
            intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
            // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "请手工添加QQ群:286239217", Toast.LENGTH_SHORT).show();
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    public void goodPinglun(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "异常", Toast.LENGTH_SHORT).show();
        }

    }

    public void destroyView() {
        if (interAd != null && interAd.isValid()) {
            interAd.close();
            interAd.destroy();
            interAd = null;
        }
        if (unifiedBannerView != null) {
            unifiedBannerView.destroy();
            unifiedBannerView = null;
        }
        if (mTTAd != null) {
            mTTAd.destroy();
            mTTAd = null;
        }
    }


}
