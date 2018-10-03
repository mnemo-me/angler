package com.mnemo.angler.ui.main_activity.activity;



import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.ui.main_activity.fragments.albums.albums.AlbumsFragment;
import com.mnemo.angler.ui.main_activity.fragments.artists.artists.ArtistsFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerFragment;
import com.mnemo.angler.player.AnglerClient;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.fragments.background_changer.BackgroundChangerFragmentv2;
import com.mnemo.angler.queue.QueueDialogFragment;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.util.MediaAssistant;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.EqualizerFragment;

import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.PlaylistPlaylistArtistTracksFragment;
import com.mnemo.angler.ui.main_activity.misc.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlists.PlaylistsFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    MainActivityPresenter presenter;
    AnglerClient anglerClient;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.main_frame)
    FrameLayout mainFrame;


    // Media panel buttons views (initializing with ButterKnife
    @BindView(R.id.media_panel_play_pause)
    ImageButton mPlayPauseButton;

    @BindView(R.id.media_panel_seek_bar)
    SeekBar seekBar;

    @BindView(R.id.media_panel_progress_time)
    TextView progressView;

    @BindView(R.id.media_panel_duration_time)
    TextView durationView;


    // bind background views
    @BindView(R.id.main_fragment_background)
    ImageView background;

    @BindView(R.id.overlay)
    FrameLayout overlay;


    // bind drawer items
    @BindViews({R.id.music_player_drawer_item, R.id.playlists_drawer_item, R.id.albums_drawer_item, R.id.artists_drawer_item, R.id.equalizer_drawer_item, R.id.background_drawer_item})
    List<TextView> drawerItems;


    // other variables
    public static float density;
    private int orientation;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    private String mainPlaylistName = "library";
    private String currentPlaylistName = "";
    private String filter = "";


    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request read/write permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        // get display metrics
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;

        // get orientation
        orientation = getResources().getConfiguration().orientation;

        // inject views
        ButterKnife.bind(this);

        // bind Presenter to View
        presenter = new MainActivityPresenter();
        presenter.attachView(this);
        presenter.init();

        // get client/service bundles
        Bundle args = null;

        if (savedInstanceState != null){

            mainPlaylistName = savedInstanceState.getString("main_playlist_name");
            currentPlaylistName = savedInstanceState.getString("current_playlist_name");
            filter = savedInstanceState.getString("filter");

            args = new Bundle();
            args.putBundle("client_bundle", savedInstanceState.getBundle("client_bundle"));
            args.putBundle("service_bundle", savedInstanceState.getBundle("service_bundle"));
        }

        // initialize player client
        anglerClient = new AnglerClient(this, args);
        anglerClient.init();

        // Setup seek bar
        setupSeekBar();

        // create main fragment or restore it visibility
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new MusicPlayerFragment(), "music_player_fragment")
                    .commit();

        }else {

            mainFrame.setVisibility(savedInstanceState.getInt("main_frame_visibility"));
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        anglerClient.connect();

        presenter.attachView(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){

                    case "seekbar_progress_changed":

                        int seekbarProgress = intent.getExtras().getInt("seekbar_progress");
                        long durationMS = anglerClient.getDurationMS();

                        if (durationMS != 0) {
                            seekBar.setProgress((int) (seekbarProgress * 100 / anglerClient.getDurationMS()));
                            progressView.setText(MediaAssistant.convertToTime(seekbarProgress));
                        }

                        break;
                }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("seekbar_progress_changed");

        registerReceiver(receiver, intentFilter);

    }


    @Override
    protected void onStop() {
        super.onStop();

        anglerClient.disconnect();

        presenter.deattachView();

        unregisterReceiver(receiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("main_playlist_name", mainPlaylistName);
        outState.putString("current_playlist_name", currentPlaylistName);
        outState.putString("filter", filter);

        outState.putInt("main_frame_visibility", findViewById(R.id.main_frame).getVisibility());
        outState.putBundle("client_bundle", anglerClient.getClientBundle());
        outState.putBundle("service_bundle", anglerClient.getServiceBundle());
    }

    @Override
    public void onBackPressed() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START);
        } else if (backStackCount == 1 || (backStackCount == 2 && getSupportFragmentManager().findFragmentById(R.id.song_list) instanceof PlaylistPlaylistArtistTracksFragment)) {
            findViewById(R.id.main_frame).setVisibility(View.VISIBLE);

            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }




    // MVP View methods
    // Set background
    public void setBackground(String backgroundImage, int opacity) {

        // set image height based on orientation
        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageHeight = 520;
        }else{
            backgroundImage = backgroundImage.replace("port", "land");
            imageHeight = 203;
        }

        ImageAssistant.loadImage(this, backgroundImage, background, imageHeight);
        overlay.setBackgroundColor(Color.argb(opacity, 0, 0, 0));
    }


    // Show current track metadata in views
    public void showDescription(String title, String artist, long durationMS) {

        //binding metadata to views
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            TextView titleView = findViewById(R.id.media_panel_title);
            titleView.setText(artist + " - " + title);

            durationView.setText(MediaAssistant.convertToTime(durationMS));

        } else {

            TextView titleView = findViewById(R.id.media_panel_title);
            titleView.setText(title);

            TextView artistView = findViewById(R.id.media_panel_artist);
            artistView.setText(artist);

            durationView.setText(" / " + MediaAssistant.convertToTime(durationMS));
        }

    }

    // set play/pause image based on state
    public void setPlayPause(String playPauseState){

        if (playPauseState.equals("play")){
            mPlayPauseButton.setImageResource(R.drawable.baseline_pause_white_48dp);
        }else{
            mPlayPauseButton.setImageResource(R.drawable.baseline_play_arrow_white_48dp);
        }
    }




    // Setup media control buttons

    @OnClick(R.id.media_panel_play_pause)
    void playPause(){
        anglerClient.playPause();
    }


    @OnClick(R.id.media_panel_next_track)
    void nextTrack(){
        anglerClient.nextTrack();
    }

    @OnClick(R.id.media_panel_previous_track)
    void previousTrack(){
        anglerClient.previousTrack();
    }

    @OnClick(R.id.media_panel_repeat)
    void repeat(View v) {
/*
        if (MediaControllerCompat.getMediaController(this).getRepeatMode() == 1) {
            v.setAlpha(0.4f);
            MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(0);
        } else {
            v.setAlpha(0.8f);
            MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(1);
        }*/
    }

    @OnClick(R.id.media_panel_shuffle)
    void shuffle(View v) {
/*
        getMediaController().getTransportControls().sendCustomAction(PlaybackManager.SHUFFLE, null);
        if (PlaybackManager.shuffleState) {
            v.setAlpha(0.4f);
        } else {
            v.setAlpha(0.8f);
        }*/
    }



    // Setup add track to playlis button
    @OnClick(R.id.media_panel_add_to_playlist)
    void addToPlaylist(){

        Bundle args = MediaAssistant.putMetadataInBundle(anglerClient.getCurrentMetadata());

        AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();
        addTrackToPlaylistDialogFragment.setArguments(args);
        addTrackToPlaylistDialogFragment.show(getSupportFragmentManager(), "Add track to playlist dialog");
    }


    // Setup queue button
    @OnClick(R.id.media_panel_queue)
    void showQueue(){

        if (MediaControllerCompat.getMediaController(MainActivity.this).getQueue().size() > 0){
            QueueDialogFragment queueDialogFragment = new QueueDialogFragment();
            queueDialogFragment.show(getSupportFragmentManager(), "queue_dialog_fragment");
        }else{
            Toast.makeText(MainActivity.this, R.string.empty_queue, Toast.LENGTH_SHORT).show();
        }
    }

    // Setup seekbar
    void setupSeekBar(){

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    anglerClient.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }



    // Creating Drawer Item Fragment
    private void createDrawerItemFragment(View v, Fragment fragment, String tag) {
        if (!v.isSelected()) {
            deselectDrawerItem();
            v.setSelected(true);
            ((TextView)v).setTypeface(null, Typeface.BOLD);
        }

        checkDrawerItemFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment, tag)
                .addToBackStack(null)
                .commit();

        mainFrame.setVisibility(View.GONE);
        drawer.closeDrawer(Gravity.START);
    }


    // Checking if last fragment in back stack is child of Drawer Item Fragment, if so - deleting it
    private void checkDrawerItemFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
        if (fragment instanceof DrawerItem) {
            getSupportFragmentManager().popBackStack();
        }
    }


    // Deselecting selected item in drawer panel and make lines visible
    public void deselectDrawerItem() {

        for (TextView t : drawerItems){
            if (t.isSelected()){
                t.setSelected(false);
                t.setTypeface(null, Typeface.NORMAL);
            }
        }

    }


    //Initializing items in drawer panel and associate them with corresponding Drawer Item Fragment
    @OnClick(R.id.music_player_drawer_item)
    void musicPlayerSelect(View v) {
        if (!v.isSelected()) {
            deselectDrawerItem();
            v.setSelected(true);
            ((TextView)v).setTypeface(null, Typeface.BOLD);
        }

        checkDrawerItemFragment();
        onBackPressed();
        findViewById(R.id.main_frame).setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.playlists_drawer_item)
    void playlistsSeleсt(View v) {
        createDrawerItemFragment(v, new PlaylistsFragment(), "Playlist fragment");

    }

    @OnClick(R.id.albums_drawer_item)
    void albumsSelect(View v) {
        createDrawerItemFragment(v, new AlbumsFragment(), "Albums fragment");

    }

    @OnClick(R.id.artists_drawer_item)
    void artistsSelect(View v) {
        createDrawerItemFragment(v, new ArtistsFragment(), "Artists fragment");

    }

    @OnClick(R.id.equalizer_drawer_item)
    void equalizerSelect(View v) {
        createDrawerItemFragment(v, new EqualizerFragment(), "Equalizer fragment");

    }

    @OnClick(R.id.background_drawer_item)
    void backgroundSelect(View v) {
        createDrawerItemFragment(v, new BackgroundChangerFragmentv2(), "Background fragment");

    }


    // Show/hide methods
    public void showBackground() {
        findViewById(R.id.background_group).setVisibility(View.VISIBLE);
    }

    public void hideBackground() {
        findViewById(R.id.background_group).setVisibility(View.GONE);
    }


    // Getter
    public AnglerClient getAnglerClient() {
        return anglerClient;
    }


    public String getMainPlaylistName() {
        return mainPlaylistName;
    }

    public void setMainPlaylistName(String mainPlaylistName) {
        this.mainPlaylistName = mainPlaylistName;
    }

    public String getCurrentPlaylistName() {
        return currentPlaylistName;
    }

    public void setCurrentPlaylistName(String currentPlaylistName) {
        this.currentPlaylistName = currentPlaylistName;
    }

    public String getCurrentMediaId() {
        return anglerClient.getCurrentMediaId();
    }


    public int getPlaybackState() {
        return anglerClient.getPlaybackState();
    }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
