package com.ahsailabs.qudsplayer.cores;

import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.models.AppListDataModel;
import com.zaitunlabs.zlcore.models.AppListModel;
import com.zaitunlabs.zlcore.models.AppListPagingModel;
import com.zaitunlabs.zlcore.models.InformationModel;
import com.zaitunlabs.zlcore.models.StoreDataModel;
import com.zaitunlabs.zlcore.models.StoreModel;
import com.zaitunlabs.zlcore.models.StorePagingModel;

public class BaseApplication extends com.zaitunlabs.zlcore.core.BaseApplication {
    @Override
    public void onCreate() {
        addDBModelClass(FavouriteModel.class);

        addDBModelClass(InformationModel.class);
        addDBModelClass(AppListModel.class);
        addDBModelClass(AppListDataModel.class);
        addDBModelClass(AppListPagingModel.class);
        addDBModelClass(StoreModel.class);
        addDBModelClass(StoreDataModel.class);
        addDBModelClass(StorePagingModel.class);


        APIConstant.setApiAppid("4");
        APIConstant.setApiKey("1321ffgfsdfsdfsgweegfdsgdf");
        APIConstant.setApiVersion("v1");
        super.onCreate();
    }
}
