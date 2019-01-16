package com.ahsailabs.qudsplayer.pages.favourite.models;

import android.provider.BaseColumns;
import android.view.Display;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.ahsailabs.qudsplayer.pages.favourite.adapters.FavouritePlayListAdapter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Table(name = "FavouriteModel", id = BaseColumns._ID)
public class FavouriteModel extends Model implements Serializable {
    @Column(name = "name")
    private String name;

    @Column(name = "number")
    private String number;

    @Column(name = "filename")
    private String filename;

    @Column(name = "pathname")
    private String pathname;

    @Column(name = "playlist")
    private String playlist;

    public FavouriteModel(){
        super();
    }

    public Date timestamp;
    public void saveWithTimeStamp() {
        this.timestamp = Calendar.getInstance().getTime();
        this.save();
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

    public static List<FavouriteModel> getAll(){
        return new Select().from(FavouriteModel.class).execute();
    }

    public static List<FavouriteModel> getPlayList(){
        return new Select().distinct().from(FavouriteModel.class).groupBy("playlist").execute();
    }

    public static void deleteWithPlayList(String playListName){
        new Delete().from(FavouriteModel.class).where("playlist = ?",playListName).execute();
    }

    public static List<FavouriteModel> getAllFromPlayList(String playListName){
        return new Select().from(FavouriteModel.class).where("playlist = ?",playListName).execute();
    }
}
