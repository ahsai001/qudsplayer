package com.ahsailabs.qudsplayer.pages.favourite.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.utils.CommonUtils;

import java.util.List;

public class FavouritePlayListAdapter extends BaseRecyclerViewAdapter<FavouriteModel,FavouritePlayListAdapter.FavouriteViewHolder> {
    public FavouritePlayListAdapter(List<FavouriteModel> modelList) {
        super(modelList);
    }

    @Override
    protected int getLayout() {
        return R.layout.favourite_item_view;
    }

    @Override
    protected FavouriteViewHolder getViewHolder(View view) {
        return new FavouriteViewHolder(view);
    }

    @Override
    protected void doSettingViewWithModel(FavouriteViewHolder favouriteViewHolder, FavouriteModel favouriteModel, int position) {
        String playListName = favouriteModel.getPlaylist();
        favouriteViewHolder.getNameView().setText(TextUtils.isEmpty(playListName)?"No Name":playListName);
        favouriteViewHolder.getFileNameView().setText("");
        setViewClickable(favouriteViewHolder,favouriteViewHolder.itemView);
    }

    public static class FavouriteViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private TextView fileNameView;
        public FavouriteViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.favourite_item_name_textview);
            fileNameView = itemView.findViewById(R.id.favourite_item_filename_textview);
        }

        public TextView getNameView() {
            return nameView;
        }

        public TextView getFileNameView() {
            return fileNameView;
        }
    }

    @Override
    public int swipeLeftColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public String swipeLeftTextString() {
        return "Delete";
    }

    @Override
    public int swipeLeftTextColor() {
        return Color.BLACK;
    }

    @Override
    public int swipeRightColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public String swipeRightTextString() {
        return "Delete";
    }

    @Override
    public int swipeRightTextColor() {
        return Color.BLACK;
    }

    @Override
    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        CommonUtils.showDialog2Option(viewHolder.itemView.getContext(), "Delete Confirmation", "are you sure want to delete it?", "delete", new Runnable() {
            public void run() {
                FavouriteModel.deleteWithPlayList(modelList.get(position).getPlaylist());
                modelList.remove(position);
                notifyItemRemoved(position);
            }
        }, "cancel", new Runnable() {
            public void run() {
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
}
