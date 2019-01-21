package com.ahsailabs.qudsplayer.pages.splash;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.pages.home.MainActivity;
import com.zaitunlabs.zlcore.activities.BaseSplashActivity;
import com.zaitunlabs.zlcore.api.APIConstant;

public class SplashActivity extends BaseSplashActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImageIcon(R.mipmap.ic_launcher);
        setBackgroundPaneColor(R.color.colorPrimary);
        setTitleTextView("Quds Player", android.R.color.white);
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
