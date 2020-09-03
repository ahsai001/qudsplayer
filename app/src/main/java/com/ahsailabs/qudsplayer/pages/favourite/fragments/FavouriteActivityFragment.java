package com.ahsailabs.qudsplayer.pages.favourite.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.configs.AppConfig;
import com.ahsailabs.qudsplayer.events.FavFABEvent;
import com.ahsailabs.qudsplayer.events.PlayThisEvent;
import com.ahsailabs.qudsplayer.events.PlayThisListEvent;
import com.ahsailabs.qudsplayer.pages.favourite.FavouriteActivity;
import com.ahsailabs.qudsplayer.pages.favourite.adapters.FavouriteAdapter;
import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.ahsailabs.sqlitewrapper.Lookup;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.labo.kaji.fragmentanimations.MoveAnimation;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.utils.CommonUtil;
import com.zaitunlabs.zlcore.utils.EventsUtil;
import com.zaitunlabs.zlcore.utils.SwipeRefreshLayoutUtil;
import com.zaitunlabs.zlcore.utils.ViewBindingUtil;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavouriteActivityFragment extends BaseFragment {
    ViewBindingUtil viewBindingUtil;
    SwipeRefreshLayoutUtil swipeRefreshLayoutUtil;
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

        EventsUtil.register(this);

    }

    @Override
    public void onDestroy() {
        EventsUtil.unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(FavFABEvent event){
        EventBus.getDefault().post(new PlayThisListEvent(favouriteModelList));
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity)getActivity()).getSupportActionBar().setTitle("Favourite List");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBindingUtil = viewBindingUtil.initWithParentView(view);
        playlistName = CommonUtil.getStringFragmentArgument(getArguments(),"playlist", "");
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return MoveAnimation.create(enter?MoveAnimation.LEFT:MoveAnimation.RIGHT,enter,500);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayoutUtil = swipeRefreshLayoutUtil.init(viewBindingUtil.getSwipeRefreshLayout(R.id.favourite_refreshLayout), new Runnable() {
            @Override
            public void run() {
                loadDB();
            }
        });

        CustomRecylerView recylerView = viewBindingUtil.getCustomRecylerView(R.id.favourite_recylerView);
        recylerView.init();
        recylerView.setEmptyView(viewBindingUtil.getViewWithId(R.id.favourite_empty_view));
        recylerView.setAdapter(favouriteAdapter);

        favouriteAdapter.addOnChildViewClickListener(new BaseRecyclerViewAdapter.OnChildViewClickListener() {
            @Override
            public void onClick(View view, Object o, int i) {
                FavouriteModel selectedItem = (FavouriteModel)o;
                EventBus.getDefault().post(new PlayThisEvent(selectedItem));
                getActivity().finish();
            }

            @Override
            public void onLongClick(View view, Object dataModel, int position) {

            }
        });

        swipeRefreshLayoutUtil.refreshNow();
        ((FavouriteActivity)getActivity()).fab.setVisibility(View.VISIBLE);
    }

    private void loadQudsQidsIndexList(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlistName).collection("list").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<FavouriteModel> qudsQidsIndexList = new ArrayList<>();
                    for (QueryDocumentSnapshot snapshot : task.getResult()){
                        Map<String, Object> data = snapshot.getData();
                        FavouriteModel item = new FavouriteModel();
                        item.setName((String) data.get("name"));
                        item.setPlaylist(playlistName);
                        item.setNumber((String) data.get("number"));
                        qudsQidsIndexList.add(item);
                    }

                    favouriteModelList.clear();
                    favouriteModelList.addAll(qudsQidsIndexList);
                    favouriteAdapter.notifyDataSetChanged();
                }

                swipeRefreshLayoutUtil.refreshDone();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                swipeRefreshLayoutUtil.refreshDone();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                swipeRefreshLayoutUtil.refreshDone();
            }
        });
    }

    private void loadDB(){
        List<FavouriteModel> dataList = FavouriteModel.getAllFromPlayList(playlistName);
        if (dataList != null && dataList.size() > 0) {
            favouriteModelList.clear();
            favouriteModelList.addAll(dataList);
            favouriteAdapter.notifyDataSetChanged();
            swipeRefreshLayoutUtil.refreshDone();
        } else {
            loadQudsQidsIndexList();
        }

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
