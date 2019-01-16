package com.ahsailabs.qudsplayer.pages.favourite.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.events.PlayThisEvent;
import com.ahsailabs.qudsplayer.events.PlayThisListEvent;
import com.ahsailabs.qudsplayer.pages.favourite.adapters.FavouriteAdapter;
import com.ahsailabs.qudsplayer.pages.favourite.adapters.FavouritePlayListAdapter;
import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.utils.SwipeRefreshLayoutUtils;
import com.zaitunlabs.zlcore.utils.ViewBindingUtils;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavouritePlayListActivityFragment extends BaseFragment {
    ViewBindingUtils viewBindingUtils;
    SwipeRefreshLayoutUtils swipeRefreshLayoutUtils;
    FavouritePlayListAdapter favouriteAdapter;
    List<FavouriteModel> favouriteModelList;
    public FavouritePlayListActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        favouriteModelList = new ArrayList<>();
        favouriteAdapter = new FavouritePlayListAdapter(favouriteModelList);

        ((BaseActivity)getActivity()).getSupportActionBar().setTitle("Play List");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBindingUtils = ViewBindingUtils.initWithParentView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayoutUtils = SwipeRefreshLayoutUtils.init(viewBindingUtils.getSwipeRefreshLayout(R.id.favourite_refreshLayout), new Runnable() {
            @Override
            public void run() {
                loadDB();
            }
        });

        CustomRecylerView recylerView = viewBindingUtils.getCustomRecylerView(R.id.favourite_recylerView);
        recylerView.init();
        recylerView.setEmptyView(viewBindingUtils.getViewWithId(R.id.favourite_empty_view));
        recylerView.setAdapter(favouriteAdapter);

        favouriteAdapter.setOnChildViewClickListener(new BaseRecyclerViewAdapter.OnChildViewClickListener() {
            @Override
            public void onClick(View view, Object o, int i) {
                FavouriteModel selectedItem = (FavouriteModel)o;
                FavouriteActivityFragment nextFragment = new FavouriteActivityFragment();
                nextFragment.setPlayListName(selectedItem.getPlaylist()).saveAsArgument();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment,nextFragment,"favlist").addToBackStack("showlist").commit();
            }
        });

        swipeRefreshLayoutUtils.refreshNow();
    }

    private void loadDB(){
        List<FavouriteModel> dataList = FavouriteModel.getPlayList();
        if(dataList != null && dataList.size() > 0) {
            favouriteModelList.clear();
            favouriteModelList.addAll(dataList);
            favouriteAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayoutUtils.refreshDone();
    }
}
