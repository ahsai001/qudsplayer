package com.ahsailabs.qudsplayer.cores;

import android.content.Context;

import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.ahsailabs.sqlitewrapper.Lookup;
import com.ahsailabs.sqlitewrapper.SQLiteWrapper;
import com.zaitunlabs.zlcore.api.APIConstant ;

public class BaseApplication extends com.zaitunlabs.zlcore.core.BaseApplication {
    public static final String DATABASE_NAME = "qudsplayer.db";
    @Override
    public void onCreate() {
        APIConstant.setApiAppid("4");
        APIConstant.setApiKey("1321ffgfsdfsdfsgweegfdsgdf");
        APIConstant.setApiVersion("v1");
        super.onCreate();

        SQLiteWrapper.addDatabase(new SQLiteWrapper.Database() {
            @Override
            public Context getContext() {
                return BaseApplication.this;
            }

            @Override
            public String getDatabaseName() {
                return DATABASE_NAME;
            }

            @Override
            public int getDatabaseVersion() {
                return 1;
            }

            @Override
            public void configure(SQLiteWrapper sqLiteWrapper) {
                sqLiteWrapper.addTable(new SQLiteWrapper.Table(FavouriteModel.class)
                        .addStringField("name")
                        .addStringField("number")
                        .addStringField("filename")
                        .addStringField("pathname")
                        .addStringField("playlist")
                        .enableRecordLog()
                        .addIndex("playlist"));
            }
        });

        Lookup.init(this, true);
    }
}
