package com.ahsailabs.qudsplayer.pages.favourite.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.pages.favourite.FavouriteActivity;
import com.ahsailabs.qudsplayer.pages.favourite.adapters.FavouritePlayListAdapter;
import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.core.BaseFragment;
import com.zaitunlabs.zlcore.core.BaseRecyclerViewAdapter;
import com.zaitunlabs.zlcore.utils.SwipeRefreshLayoutUtil;
import com.zaitunlabs.zlcore.utils.ViewBindingUtil;
import com.zaitunlabs.zlcore.views.CustomRecylerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class FavouritePlayListActivityFragment extends BaseFragment {
    ViewBindingUtil viewBindingUtil;
    SwipeRefreshLayoutUtil swipeRefreshLayoutUtil;
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
    }

    @Override
    public void onResume() {
        super.onResume();

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
        viewBindingUtil = ViewBindingUtil.initWithParentView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        swipeRefreshLayoutUtil = SwipeRefreshLayoutUtil.init(viewBindingUtil.getSwipeRefreshLayout(R.id.favourite_refreshLayout), new Runnable() {
            @Override
            public void run() {
                loadDB();
            }
        });

        CustomRecylerView recylerView = viewBindingUtil.getCustomRecylerView(R.id.favourite_recylerView);
        recylerView.init();
        recylerView.setEmptyView(viewBindingUtil.getViewWithId(R.id.favourite_empty_view));
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

            @Override
            public void onLongClick(View view, Object dataModel, int position) {

            }
        });

        swipeRefreshLayoutUtil.refreshNow();
        ((FavouriteActivity)getActivity()).fab.setVisibility(View.GONE);
    }

    private void loadCloudListAndDB(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<FavouriteModel> cloudList = new ArrayList<>();
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot snapshot : task.getResult()){
                        Map<String, Object> data = snapshot.getData();
                        FavouriteModel item = new FavouriteModel();
                    }
                }

                loadDB();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                loadDB();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadDB();
            }
        });
    }
    
    private void loadDB(){
        List<FavouriteModel> dataList = FavouriteModel.getPlayList();
        if(dataList != null && dataList.size() > 0) {
            favouriteModelList.clear();
            favouriteModelList.addAll(dataList);
            favouriteAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayoutUtil.refreshDone();
    }
}
