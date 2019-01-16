package com.ahsailabs.qudsplayer.events;

import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;

public class PlayThisEvent {
    private FavouriteModel data;

    public FavouriteModel getData() {
        return data;
    }

    public void setData(FavouriteModel data) {
        this.data = data;
    }

    public PlayThisEvent(FavouriteModel data) {
        this.data = data;
    }
}
