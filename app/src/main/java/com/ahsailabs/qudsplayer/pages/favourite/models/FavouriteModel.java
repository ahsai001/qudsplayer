package com.ahsailabs.qudsplayer.pages.favourite.models;

import com.ahsailabs.qudsplayer.cores.BaseApplication;
import com.zaitunlabs.zlcore.utils.SQLiteWrapper;

import java.util.List;

public class FavouriteModel extends SQLiteWrapper.TableClass {
    private String name;
    private String number;
    private String filename;
    private String pathname;
    private String playlist;

    public FavouriteModel(){
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    @Override
    protected String getDatabaseName() {
        return BaseApplication.DATABASE_NAME;
    }

    @Override
    protected void getObjectData(List<Object> dataList) {
        dataList.add(name);
        dataList.add(number);
        dataList.add(filename);
        dataList.add(pathname);
        dataList.add(playlist);
    }

    @Override
    protected void setObjectData(List<Object> dataList) {
        name = (String) dataList.get(0);
        number = (String) dataList.get(1);
        filename = (String) dataList.get(2);
        pathname = (String) dataList.get(3);
        playlist = (String) dataList.get(4);
    }


    public static List<FavouriteModel> getAll(){
        return SQLiteWrapper.of(BaseApplication.DATABASE_NAME).findAll(null, FavouriteModel.class);
    }

    public static List<FavouriteModel> getPlayList(){
        return SQLiteWrapper.of(BaseApplication.DATABASE_NAME).selectQuery(true,null, FavouriteModel.class,
                null, null, null,"playlist", null, null, null);
    }

    public static void deleteWithPlayList(String playListName){
        SQLiteWrapper.of(BaseApplication.DATABASE_NAME).delete(null, FavouriteModel.class,
                "playlist=?", new String[]{playListName});
    }

    public static List<FavouriteModel> getAllFromPlayList(String playListName){
        return SQLiteWrapper.of(BaseApplication.DATABASE_NAME).findAllWithCriteria(null, FavouriteModel.class,
                "playlist=?", new String[]{playListName});
    }
}
