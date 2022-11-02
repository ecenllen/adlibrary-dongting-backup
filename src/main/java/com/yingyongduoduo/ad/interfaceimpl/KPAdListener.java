package com.yingyongduoduo.ad.interfaceimpl;

public interface KPAdListener {
    void onAdPresent();

    void onAdDismissed();

    void onAdFailed(String var1);

    void onAdClick();

    void onAdTick(long millisUntilFinished);
}
