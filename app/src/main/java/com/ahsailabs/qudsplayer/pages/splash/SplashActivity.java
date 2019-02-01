package com.ahsailabs.qudsplayer.pages.splash;

import android.os.Bundle;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.pages.home.MainActivity;
import com.zaitunlabs.zlcore.activities.BaseSplashActivity;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.utils.CommonUtils;

public class SplashActivity extends BaseSplashActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImageIcon(R.drawable.splash_logo);
        setBackgroundPaneColor(R.color.colorPrimary);
        setTitleTextView("Quds Player\n"+getString(R.string.nav_header_subtitle), android.R.color.white);
        setBottomTextView("Quds Player v"+CommonUtils.getVersionName(SplashActivity.this), android.R.color.white);
    }

    @Override
    protected String getCheckVersionUrl() {
        return APIConstant.API_CHECK_VERSION;
    }

    @Override
    protected void doNextAction() {
        MainActivity.start(SplashActivity.this);
    }

    @Override
    protected int getMinimumSplashTimeInMS() {
        return 3000;
    }
}
