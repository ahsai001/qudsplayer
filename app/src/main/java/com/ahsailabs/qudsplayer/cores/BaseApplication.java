package com.ahsailabs.qudsplayer.cores;

import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;

public class BaseApplication extends com.zaitunlabs.zlcore.core.BaseApplication {
    @Override
    public void onCreate() {
        addDBModelClass(FavouriteModel.class);
        super.onCreate();
    }
}
