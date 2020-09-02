package com.ahsailabs.qudsplayer.pages.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahsailabs.qudsplayer.R;
import com.ahsailabs.qudsplayer.configs.AppConfig;
import com.ahsailabs.qudsplayer.events.PlayThisEvent;
import com.ahsailabs.qudsplayer.events.PlayThisListEvent;
import com.ahsailabs.qudsplayer.pages.favourite.FavouriteActivity;
import com.ahsailabs.qudsplayer.pages.favourite.models.FavouriteModel;
import com.ahsailabs.qudsplayer.views.TextInputAutoCompleteTextView;
import com.google.android.material.snackbar.Snackbar;
import com.zaitunlabs.zlcore.activities.AppListActivity;
import com.zaitunlabs.zlcore.activities.MessageListActivity;
import com.zaitunlabs.zlcore.activities.StoreActivity;
import com.zaitunlabs.zlcore.api.APIConstant;
import com.zaitunlabs.zlcore.core.BaseActivity;
import com.zaitunlabs.zlcore.core.WebViewActivity;
import com.zaitunlabs.zlcore.events.InfoCounterEvent;
import com.zaitunlabs.zlcore.modules.about.AboutUs;
import com.zaitunlabs.zlcore.services.FCMIntentService;
import com.zaitunlabs.zlcore.tables.InformationModel;
import com.zaitunlabs.zlcore.utils.CommonUtil;
import com.zaitunlabs.zlcore.utils.EventsUtil;
import com.zaitunlabs.zlcore.utils.PermissionUtil;
import com.zaitunlabs.zlcore.utils.ViewBindingUtil;
import com.zaitunlabs.zlcore.utils.ViewUtil;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    List<String> fileNameList = new ArrayList<>();
    List<String> filePathList = new ArrayList<>();
    HashMap<String, String> fileMap = new HashMap<>();

    TextView statusTextView;
    TextView numberTextView;
    Button playButton;

    MediaPlayer mediaPlayer;
    SeekBar playingSeekBar;
    ViewBindingUtil viewBindingUtil;
    PermissionUtil permissionUtil;

    final String NO_REPEAT = "no repeat";
    final String REPEAT_ONE = "repeat one";
    final String REPEAT_ALL = "repeat all";
    String repeatState = NO_REPEAT;
    int playingNumber = 0;

    final String NO_PLAY = "";
    final String PLAY = "played";
    final String PAUSE = "paused";
    final String STOP = "stopped";
    String playState = NO_PLAY;


    boolean isPlaylistMode = false;
    int playListIndex = 0;
    List<FavouriteModel> favPlayList = new ArrayList<>();


    private TextView messageItemView;
    private boolean isScanning = false;

    private Snackbar scanLoading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FavouriteActivity.start(MainActivity.this);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        viewBindingUtil = viewBindingUtil.initWithParentView(findViewById(android.R.id.content));

        statusTextView = findViewById(R.id.status_textview);
        numberTextView = findViewById(R.id.number_textview);
        playButton = findViewById(R.id.play_button);

        playButton.setBackground(ViewUtil.getSelectableItemBackgroundWithColor(MainActivity.this, ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)));

        permissionUtil = PermissionUtil.checkPermissionAndGo(MainActivity.this, 1053, new Runnable() {
            @Override
            public void run() {
                addAsync(new AsyncTask<Void, Void, Void>() {
                    public void walkDir(File dir) {

                        File[] listFile = dir.listFiles();

                        if (listFile != null) {
                            for (int i = 0; i < listFile.length; i++) {
                                if (listFile[i].isDirectory()) {
                                    walkDir(listFile[i]);
                                } else {
                                    String fileName = listFile[i].getName();
                                    String pathName = listFile[i].getPath();
                                    if (fileName.endsWith(".mp3") || fileName.endsWith(".MP3") || fileName.endsWith(".wma")) {
                                        //remove extension
                                        fileName = fileName.replace(".mp3","").replace(".MP3","").replace(".wma","");

                                        fileMap.put(fileName, pathName);

                                        fileNameList.add(fileName);
                                        filePathList.add(pathName);

                                        publishProgress();

                                        Log.e("audio OK fileName",fileName);
                                        Log.e("audio OK pathName",pathName);
                                    } else {
                                        Log.e("audio NOK fileName",fileName);
                                        Log.e("audio NOK pathName",pathName);
                                    }
                                }
                            }
                        }

                        publishProgress();
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        scanLoading = CommonUtil.showLoadingSnackBar(MainActivity.this, "Sedang membaca micro SD, harap bersabar");
                        isScanning = true;
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        String[] sdCards = getStorageDirectories(MainActivity.this);
                        if (sdCards.length > 0) {
                            walkDir(new File(sdCards[0]));
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Void... values) {
                        updateInfo();
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        isScanning = false;
                        scanLoading.dismiss();
                    }
                }.execute());
            }
        }, new Runnable() {
            @Override
            public void run() {
                CommonUtil.showSnackBar(MainActivity.this,"Aplikasi tidak dapat bekerja normal tanpa storage permission and phone state permission");
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE);




        messageItemView = (TextView) navigationView.getMenu().
                findItem(R.id.nav_message).getActionView();

        reCountMessage();



        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = numberTextView.getText().toString();
                if(!TextUtils.isEmpty(number)){
                    //do playing
                    int numberInt = Integer.parseInt(number);

                    if(0 >= filePathList.size()){
                        CommonUtil.showSnackBar(MainActivity.this, "Maaf, tidak ada audio yang terbaca");
                        return;
                    }

                    if(numberInt-1 > filePathList.size()){
                        CommonUtil.showSnackBar(MainActivity.this, "Maaf, tidak ada audio pada nomor ini");
                        return;
                    }

                    String fileName = fileNameList.get(numberInt-1);
                    String filePath = filePathList.get(numberInt-1);
                    if(filePath != null) {
                        playingNumber = numberInt;

                        releaseMediaPlayer();
                        try {
                            mediaPlayer = MediaPlayer.create(MainActivity.this, Uri.parse(filePath));
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    if (repeatState.equals(REPEAT_ALL)) {
                                        String nextNumber = String.valueOf(playingNumber + 1);
                                        if(isPlaylistMode){
                                            playListIndex = playListIndex+1;
                                            if(playListIndex >= favPlayList.size()){
                                                playListIndex = 0;
                                            }
                                            nextNumber = favPlayList.get(playListIndex).getNumber();
                                        }
                                        numberTextView.setText(nextNumber);
                                        playButton.callOnClick();
                                    } else {
                                        playState = STOP;
                                        updateInfo();
                                    }
                                }
                            });


                            mediaPlayer.start();


                            playingSeekBar.setMax(mediaPlayer.getDuration() / 1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mediaPlayer != null) {
                                        int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                                        playingSeekBar.setProgress(mCurrentPosition);
                                    }
                                    mSeekBarHandler.postDelayed(this, 1000);
                                }
                            });

                            if (repeatState.equals(REPEAT_ONE)) {
                                mediaPlayer.setLooping(true);
                            } else {
                                mediaPlayer.setLooping(false);
                            }
                            playState = PLAY;

                        } catch (Exception e){
                            CommonUtil.showSnackBar(MainActivity.this, "Maaf, tidak bisa menjalankan audio ini");
                            e.printStackTrace();
                        }


                    } else {
                        if(repeatState.equals(REPEAT_ALL)){
                            numberTextView.setText("1");
                            playButton.callOnClick();
                        }
                    }
                    updateInfo();
                }
            }
        });


        viewBindingUtil.getTextView(R.id.number_1_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("1");
            }
        });
        viewBindingUtil.getTextView(R.id.number_2_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("2");
            }
        });
        viewBindingUtil.getTextView(R.id.number_3_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("3");
            }
        });
        viewBindingUtil.getTextView(R.id.number_4_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("4");
            }
        });
        viewBindingUtil.getTextView(R.id.number_5_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("5");
            }
        });
        viewBindingUtil.getTextView(R.id.number_6_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("6");
            }
        });
        viewBindingUtil.getTextView(R.id.number_7_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("7");
            }
        });
        viewBindingUtil.getTextView(R.id.number_8_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("8");
            }
        });
        viewBindingUtil.getTextView(R.id.number_9_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("9");
            }
        });
        viewBindingUtil.getTextView(R.id.number_0_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appendNumber("0");
            }
        });
        viewBindingUtil.getViewWithId(R.id.number_repeat_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeatState.equals(NO_REPEAT)){
                    repeatState = REPEAT_ONE;
                    if(mediaPlayer != null){
                        mediaPlayer.setLooping(true);
                    }
                } else if(repeatState.equals(REPEAT_ONE)){
                    repeatState = REPEAT_ALL;
                    if(mediaPlayer != null){
                        mediaPlayer.setLooping(false);
                    }
                } else if(repeatState.equals(REPEAT_ALL)){
                    repeatState = NO_REPEAT;
                    if(mediaPlayer != null){
                        mediaPlayer.setLooping(false);
                    }
                }

                updateInfo();
            }
        });
        viewBindingUtil.getViewWithId(R.id.number_pause_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playState.equals(PLAY)){
                    playState = PAUSE;
                    if(mediaPlayer != null){
                        mediaPlayer.pause();
                    }
                } else if(playState.equals(PAUSE)){
                    playState = PLAY;
                    if(mediaPlayer != null){
                        mediaPlayer.start();
                    }
                }

                updateInfo();
            }
        });

        viewBindingUtil.getViewWithId(R.id.number_prev_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prevNumber = String.valueOf(playingNumber-1);
                if(isPlaylistMode){
                    playListIndex = playListIndex-1;
                    if(playListIndex < 0){
                        playListIndex = favPlayList.size()-1;
                    }
                    prevNumber = favPlayList.get(playListIndex).getNumber();
                }
                numberTextView.setText(prevNumber);
                playButton.callOnClick();
            }
        });

        viewBindingUtil.getViewWithId(R.id.number_stop_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playState.equals(PLAY) || playState.equals(PAUSE)){
                    playState = STOP;
                    if(mediaPlayer != null){
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(0);
                    }
                }

                updateInfo();

            }
        });

        viewBindingUtil.getViewWithId(R.id.number_next_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nextNumber = String.valueOf(playingNumber+1);
                if(isPlaylistMode){
                    playListIndex = playListIndex+1;
                    if(playListIndex >= favPlayList.size()){
                        playListIndex = 0;
                    }
                    nextNumber = favPlayList.get(playListIndex).getNumber();
                }
                numberTextView.setText(nextNumber);
                playButton.callOnClick();
            }
        });


        viewBindingUtil.getViewWithId(R.id.number_add_fav_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!playState.equals(PLAY)){
                    return;
                }

                if(mediaPlayer == null){
                    return;
                }

                final String fileName = fileNameList.get(playingNumber-1);
                final String filePath = filePathList.get(playingNumber-1);

                if(filePath == null){
                    return;
                }

                final View customView = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_fav_dialog_layout, null);

                List<FavouriteModel> favouriteModelList = FavouriteModel.getPlayList();
                List<String> playList = new ArrayList<>();
                for(FavouriteModel item : favouriteModelList){
                    String playListName = item.getPlaylist();
                    playList.add(TextUtils.isEmpty(playListName)?"No Name": playListName);
                }
                ArrayAdapter<String> playListAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line, playList);
                ((TextInputAutoCompleteTextView)customView.findViewById(R.id.fav_playlist_edittext)).setAdapter(playListAdapter);
                ((TextInputAutoCompleteTextView)customView.findViewById(R.id.fav_playlist_edittext)).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        ((TextInputAutoCompleteTextView)customView.findViewById(R.id.fav_playlist_edittext)).showDropDown();
                        return false;
                    }
                });
                CommonUtil.showDialog2OptionCustomView(MainActivity.this, customView, "Add number "+playingNumber+" as Favourite?",
                        "Add", new Runnable() {
                            @Override
                            public void run() {
                                String name = ((EditText)customView.findViewById(R.id.fav_name_edittext)).getText().toString();
                                String playlist = ((EditText)customView.findViewById(R.id.fav_playlist_edittext)).getText().toString();

                                FavouriteModel favouriteModel = new FavouriteModel();
                                favouriteModel.setName(name);
                                favouriteModel.setPlaylist(playlist);
                                //favouriteModel.setNumber(String.valueOf(playingNumber));
                                favouriteModel.setNumber(fileName);
                                favouriteModel.setFilename(fileName);
                                favouriteModel.setPathname(filePath);
                                favouriteModel.save();


                                CommonUtil.showToast(MainActivity.this,name+" - "+playlist);
                            }
                        },"Cancel", null).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);;
            }
        });


        viewBindingUtil.getViewWithId(R.id.number_backward_textview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoBackwardDecrement = true;
                    backForwardHandler.post( new BackForwardUpdater() );
                    return false;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                            && mAutoBackwardDecrement ){
                        mAutoBackwardDecrement = false;
                    }
                    return false;
                }
                return false;
            }
        });

        viewBindingUtil.getViewWithId(R.id.number_forward_textview).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    mAutoForwardIncrement = true;
                    backForwardHandler.post( new BackForwardUpdater() );
                    return false;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    v.setPressed(false);
                    if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL)
                            && mAutoForwardIncrement ){
                        mAutoForwardIncrement = false;
                    }
                    return false;
                }

                return false;
            }
        });



        playingSeekBar = findViewById(R.id.playing_seekbar);
        playingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress * 1000);
                }
            }
        });

        EventsUtil.register(this);
    }

    private Handler mSeekBarHandler = new Handler();
    private Handler backForwardHandler = new Handler();
    public int mBackForwardValue;
    private boolean mAutoForwardIncrement = false;
    private boolean mAutoBackwardDecrement = false;
    private class BackForwardUpdater implements Runnable {
        public void run() {
            if (playState.equals(PLAY)) {
                if (mediaPlayer != null) {
                    if (mAutoForwardIncrement) {
                        mBackForwardValue += 30;
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + mBackForwardValue);
                        backForwardHandler.postDelayed(new BackForwardUpdater(), 50);
                    } else if (mAutoBackwardDecrement) {
                        mBackForwardValue -= 30;
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - mBackForwardValue);
                        backForwardHandler.postDelayed(new BackForwardUpdater(), 50);
                    }
                }
            }
        }
    }

    private void updateInfo(){
        String info = "Total : "+ filePathList.size();
        if(!TextUtils.isEmpty(repeatState)){
            info += "<br/>"+repeatState;

            switch (repeatState) {
                case NO_REPEAT:
                    ((ImageView) viewBindingUtil.getViewWithId(R.id.number_repeat_textview)).setImageResource(R.drawable.ic_repeat_no);
                    break;
                case REPEAT_ONE:
                    ((ImageView) viewBindingUtil.getViewWithId(R.id.number_repeat_textview)).setImageResource(R.drawable.ic_repeat_one);
                    break;
                case REPEAT_ALL:
                    ((ImageView) viewBindingUtil.getViewWithId(R.id.number_repeat_textview)).setImageResource(R.drawable.ic_repeat);
                    break;
            }

        }
        if(!TextUtils.isEmpty(playState)){
            info += "<br/>"+playState;
            switch (playState) {
                case PAUSE:
                    ((ImageView) viewBindingUtil.getViewWithId(R.id.number_pause_textview)).setImageResource(R.drawable.ic_play_arrow);
                    break;
                case PLAY:
                    ((ImageView) viewBindingUtil.getViewWithId(R.id.number_pause_textview)).setImageResource(R.drawable.ic_pause);
                    break;
            }
        }
        if(playingNumber > 0){
            info += "<br/><b>"+playingNumber+"</b>";
        }

        if(isPlaylistMode){
            FavouriteModel selectedItem = favPlayList.get(playListIndex);
            String title = selectedItem.getName()+" - "+selectedItem.getPlaylist();
            info += "<b>("+title+")</b>";
        }


        statusTextView.setText(CommonUtil.fromHtml(info));
    }

    private void appendNumber(String number){
        if(numberTextView.length() == 4){
            numberTextView.setText(number);
        } else {
            numberTextView.setText(numberTextView.getText()+number);
        }
    }

    private void releaseMediaPlayer(){
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void reCountMessage(){
        if(messageItemView != null) {
            messageItemView.setGravity(Gravity.CENTER_VERTICAL);
            messageItemView.setTypeface(null, Typeface.BOLD);
            messageItemView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            messageItemView.setText(""+ InformationModel.unreadInfoCount());
        }
    }

    @Subscribe
    public void onEvent(InfoCounterEvent event){
        reCountMessage();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(permissionUtil != null){
            permissionUtil.onRequestPermissionsResult(requestCode,permissions, grantResults);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        FCMIntentService.startSending(MainActivity.this,APIConstant.API_APPID,false, false);
    }

    @Subscribe
    public void onEvent(PlayThisListEvent event){
        List<FavouriteModel> dataList = event.getDataList();
        //if(filePathList.contains(dataList.get(0).getPathname())){
            favPlayList.addAll(dataList);
            isPlaylistMode = true;
            repeatState = REPEAT_ALL;

            String playListName = dataList.get(0).getPlaylist();
            getSupportActionBar().setTitle(playListName);

            reConfigurePlayList();
            playListIndex = 0;
            numberTextView.setText(favPlayList.get(playListIndex).getNumber());
            playButton.callOnClick();
            invalidateOptionsMenu();
        //} else {
        //    CommonUtil.showToast(MainActivity.this, "This favourite item is not from current sd card");
        //}
    }

    private void reConfigurePlayList() {
        viewBindingUtil.getTextView(R.id.number_1_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_2_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_3_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_4_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_5_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_6_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_7_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_8_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_9_textview).setEnabled(!isPlaylistMode);
        viewBindingUtil.getTextView(R.id.number_0_textview).setEnabled(!isPlaylistMode);
        playButton.setEnabled(!isPlaylistMode);

        if(isPlaylistMode){

        }
    }

    @Subscribe
    public void onEvent(PlayThisEvent event){
        FavouriteModel data = event.getData();
        //if(filePathList.contains(data.getPathname())){
            exitPlayList();
            numberTextView.setText(data.getNumber());
            playButton.callOnClick();
        //} else {
        //    CommonUtil.showToast(MainActivity.this, "This favourite item is not from current sd card");
        //}
    }

    @Override
    protected void onDestroy() {
        releaseMediaPlayer();
        EventsUtil.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(!isScanning){
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_exit_play_list).setEnabled(isPlaylistMode);
        menu.findItem(R.id.action_exit_play_list).setVisible(isPlaylistMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit_play_list) {
            exitPlayList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void exitPlayList(){
        favPlayList.clear();
        isPlaylistMode = false;
        playListIndex = 0;

        getSupportActionBar().setTitle(R.string.app_name);
        reConfigurePlayList();

        invalidateOptionsMenu();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            AboutUs.start(this,R.mipmap.ic_launcher,0,R.string.share_title,R.string.share_body_template,
                    0,R.string.feedback_mail_to, R.string.feedback_title, R.string.feedback_body_template,
                    0,R.raw.version_change_history, true, AppConfig.appLandingURL,
                    false, "AhsaiLabs", AppConfig.devURL,getString(R.string.feedback_mail_to),R.drawable.ahsailabs_logo,"2019\nAll right reserved",
                    R.color.colorPrimary,ContextCompat.getColor(this,android.R.color.white),ContextCompat.getColor(this,android.R.color.white),AppConfig.aboutAppURL, false);
        } else if (id == R.id.nav_app_list) {
            AppListActivity.start(this, false);
        } else if (id == R.id.nav_store) {
            StoreActivity.start(this, false);
        } else if (id == R.id.nav_message) {
            MessageListActivity.start(this, false);
        } else if(id == R.id.nav_socmed_facebook){
            CommonUtil.openBrowser(MainActivity.this, "https://www.facebook.com/Speaker-Quran-QUDS-1195404403873415/");
        } else if(id == R.id.nav_socmed_instagram){
            CommonUtil.openBrowser(MainActivity.this, "https://www.instagram.com/speakerquranquds/");
        } else if(id == R.id.nav_kontak){
            WebViewActivity.start(MainActivity.this,"file:///android_asset/www/kontak.html","Hubungi Kami",
                    "Maaf, jika ada kesalahan", ContextCompat.getColor(MainActivity.this,android.R.color.white),
                    "tentangkami",false);
        } else if(id == R.id.nav_tentang_kami){
            WebViewActivity.start(MainActivity.this,"file:///android_asset/www/tentang_kami.html","Tentang Kami",
                    "Maaf, jika ada kesalahan", ContextCompat.getColor(MainActivity.this,android.R.color.white),
                    "tentangkami", false);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkSDCardStatus() {
        String sdCardStatus = Environment.getExternalStorageState();

        // MEDIA_UNKNOWN: unrecognized SD card
        // MEDIA_REMOVED: no SD card
        // MEDIA_UNMOUNTED: SD card exists but not mounted, deprecated in Android 4.0+
        // MEDIA_CHECKING: preparing SD card
        // MEDIA_MOUNTED: mounted and ready to use
        // MEDIA_MOUNTED_READ_ONLY
        switch (sdCardStatus) {
            case Environment.MEDIA_MOUNTED:
                return true;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                return true;
            default:
                Toast.makeText(this, "SD card is not available.", Toast.LENGTH_LONG).show();
                return false;
        }
    }



    /**
     * Returns all available external SD-Card roots in the system.
     *
     * @return paths to all available external SD-Card roots in the system.
     */
    public static String[] getStorageDirectories(Context context) {
        String[] storageDirectories;
        String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            List<String> results = new ArrayList<String>();
            File[] externalDirs = context.getExternalFilesDirs(null);
            for (File file : externalDirs) {
                String path = null;
                try {
                    path = file.getPath().split("/Android")[0];
                } catch (Exception e) {
                    e.printStackTrace();
                    path = null;
                }
                if (path != null) {
                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Environment.isExternalStorageRemovable(file))
                            || (rawSecondaryStoragesStr != null && rawSecondaryStoragesStr.contains(path))) {
                        results.add(path);
                    }
                }
            }
            storageDirectories = results.toArray(new String[0]);
        } else {
            final Set<String> rv = new HashSet<String>();

            if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
                Collections.addAll(rv, rawSecondaryStorages);
            }
            storageDirectories = rv.toArray(new String[rv.size()]);
        }
        return storageDirectories;
    }

    public static void start(Context context){
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
}
