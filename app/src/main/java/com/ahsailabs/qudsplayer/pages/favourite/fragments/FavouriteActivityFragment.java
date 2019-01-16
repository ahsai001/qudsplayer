package com.ahsailabs.qudsplayer.pages.favourite.fragments;

import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.events.PlayThisEvent;
import com.ahsailabs.qudsplayer.events.PlayThisListEvent;
import com.ahsailabs.qudsplayer.pages.favourite.adapters.FavouriteAdapter;
import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.labo.kaji.fragmentanimations.MoveAnimation;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.utils.CommonUtils;
import com.zaitunlabs.zlcore.utils.SwipeRefreshLayoutUtils;
import com.zaitunlabs.zlcore.utils.ViewBindingUtils;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavouriteActivityFragment extends BaseFragment {
    ViewBindingUtils viewBindingUtils;
    SwipeRefreshLayoutUtils swipeRefreshLayoutUtils;
    FavouriteAdapter favouriteAdapter;
    List<FavouriteModel> favouriteModelList;
    String playlistName;
    public FavouriteActivityFragment() {
    }


    public FavouriteActivityFragment setPlayListName(String playListName){
        getBundle().putString("playlist", playListName);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        favouriteModelList = new ArrayList<>();
        favouriteAdapter = new FavouriteAdapter(favouriteModelList);


        ((BaseActivity)getActivity()).getSupportActionBar().setTitle("Fav List");
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
        playlistName = CommonUtils.getStringFragmentArgument(getArguments(),"playlist", "");
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return MoveAnimation.create(enter?MoveAnimation.LEFT:MoveAnimation.RIGHT,enter,500);
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
                EventBus.getDefault().post(new PlayThisEvent(selectedItem));
                getActivity().finish();
            }
        });

        swipeRefreshLayoutUtils.refreshNow();
    }

    private void loadDB(){
        List<FavouriteModel> dataList = FavouriteModel.getAllFromPlayList(playlistName);
        if(dataList != null && dataList.size() > 0) {
            favouriteModelList.clear();
            favouriteModelList.addAll(dataList);
            favouriteAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayoutUtils.refreshDone();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favourite,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_play_this_list){
            EventBus.getDefault().post(new PlayThisListEvent(favouriteModelList));
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
