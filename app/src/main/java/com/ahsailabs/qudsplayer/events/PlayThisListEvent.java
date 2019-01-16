package com.ahsailabs.qudsplayer.events;

import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;

import java.util.List;

public class PlayThisListEvent {
    List<FavouriteModel> dataList;

    public List<FavouriteModel> getDataList() {
        return dataList;
    }

    public void setDataList(List<FavouriteModel> dataList) {
        this.dataList = dataList;
    }

    public PlayThisListEvent(List<FavouriteModel> favouriteModelList) {
        this.dataList = favouriteModelList;
    }
}
