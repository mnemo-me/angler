package com.mnemo.angler.ui.main_activity.activity;



import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.ui.main_activity.fragments.albums.albums.AlbumsFragment;
import com.mnemo.angler.ui.main_activity.fragments.artists.artists.ArtistsFragment;
import com.mnemo.angler.ui.main_activity.fragments.background_changer.background_changer.BackgroundChangerFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerFragment;
import com.mnemo.angler.player.client.AnglerClient;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.player.queue.QueueDialogFragment;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.util.MediaAssistant;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.equalizer.EqualizerFragment;

import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.PlaylistArtistTracksFragment;
import com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlists.PlaylistsFragment;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    private MainActivityPresenter presenter;
    private AnglerClient anglerClient;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.main_frame)
    FrameLayout mainFrame;


    // Media panel buttons views (initializing with ButterKnife
    @BindView(R.id.media_panel_play_pause)
    ImageButton mPlayPauseButton;

    @BindView(R.id.media_panel_repeat)
    ImageButton repeatButton;

    @BindView(R.id.media_panel_shuffle)
    ImageButton shuffleButton;

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

    private BroadcastReceiver receiver;

    private String mainPlaylistName = "library";
    private String currentPlaylistName = "";
    private String filter = "";

    private int selectedDrawerItemIndex = 0;


    protected void onCreate(final Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // request read/write permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{

            setContentView(R.layout.activity_main);

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

            // get client/service bundles
            Bundle args = null;

            if (savedInstanceState != null) {

                mainPlaylistName = savedInstanceState.getString("main_playlist_name");
                currentPlaylistName = savedInstanceState.getString("current_playlist_name");
                filter = savedInstanceState.getString("filter");
                selectedDrawerItemIndex = savedInstanceState.getInt("selected_drawer_item");

                args = new Bundle();
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

            } else {

                mainFrame.setVisibility(savedInstanceState.getInt("main_frame_visibility"));
            }


            // Select current drawer item
            drawerItems.get(selectedDrawerItemIndex).setSelected(true);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (anglerClient != null) {
            anglerClient.connect();
        }

        if (presenter != null) {
            presenter.attachView(this);

            // Set background
            presenter.setupBackground();
        }


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

                    case "queue_error":



                        break;

                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("seekbar_progress_changed");
        intentFilter.addAction("queue_error");

        registerReceiver(receiver, intentFilter);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (anglerClient != null) {
            anglerClient.disconnect();
        }

        if (presenter != null) {
            presenter.deattachView();
        }

        unregisterReceiver(receiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (anglerClient != null) {
            outState.putString("main_playlist_name", mainPlaylistName);
            outState.putString("current_playlist_name", currentPlaylistName);
            outState.putString("filter", filter);
            outState.putInt("selected_drawer_item", selectedDrawerItemIndex);

            outState.putInt("main_frame_visibility", findViewById(R.id.main_frame).getVisibility());
            outState.putBundle("service_bundle", anglerClient.getServiceBundle());
        }
    }

    @Override
    public void onBackPressed() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START);
        } else if (backStackCount == 1 || (backStackCount == 2 && getSupportFragmentManager().findFragmentById(R.id.song_list) instanceof PlaylistArtistTracksFragment)) {
            findViewById(R.id.main_frame).setVisibility(View.VISIBLE);
            selectDrawerItem(0);

            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){

            case 1:

                if (grantResults.length > 0) {

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        Intent intent = getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        finish();

                        overridePendingTransition(0, 0);

                        startActivity(getIntent());

                    } else {
                        finish();
                    }
                }

                break;
        }
    }

    // MVP View methods
    // Set background
    public void setBackground(String backgroundImage, int opacity) {

        // Set image path & height based on orientation
        String imagePath;
        int imageHeight;

        if (backgroundImage.startsWith("R.drawable.")) {
            imagePath = backgroundImage;
        }else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                imagePath = AnglerFolder.PATH_BACKGROUND_PORTRAIT + File.separator + backgroundImage;
            } else {
                imagePath = AnglerFolder.PATH_BACKGROUND_LANDSCAPE + File.separator + backgroundImage;
            }
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageHeight = 520;
        }else{
            imageHeight = 203;
        }

        ImageAssistant.loadImage(this, imagePath, background, imageHeight);
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

            durationView.setText(" /" + MediaAssistant.convertToTime(durationMS));
        }

    }

    // Set play/pause image based on state
    public void setPlayPause(String playPauseState){

        if (playPauseState.equals("play")){
            mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);
        }else{
            mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
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

        boolean isRepeat = anglerClient.changeRepeatMode();

        if (isRepeat){
            v.setAlpha(0.8f);
        }else{
            v.setAlpha(0.2f);
        }
    }

    @OnClick(R.id.media_panel_shuffle)
    void shuffle(View v) {

        boolean isShuffle = anglerClient.changeShuffleMode();

        if (isShuffle){
            v.setAlpha(0.8f);
        }else{
            v.setAlpha(0.2f);
        }
    }


    public void setRepeatState(boolean repeatState){

        if (repeatState){
            repeatButton.setAlpha(0.8f);
        }else{
            repeatButton.setAlpha(0.2f);
        }
    }

    public void setShuffleState(boolean shuffleState) {

        if (shuffleState) {
            shuffleButton.setAlpha(0.8f);
        }else{
            shuffleButton.setAlpha(0.2f);
        }

    }



    // Setup add track to playlis button
    @OnClick(R.id.media_panel_add_to_playlist)
    void addToPlaylist(){

        if (getPlaybackState() != PlaybackStateCompat.STATE_ERROR) {

            Track track = MediaAssistant.combineMetadataInTrack(anglerClient.getCurrentMetadata());

            Bundle args = new Bundle();
            args.putParcelable("track", track);

            AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();
            addTrackToPlaylistDialogFragment.setArguments(args);
            addTrackToPlaylistDialogFragment.show(getSupportFragmentManager(), "Add track to playlist dialog");

        }else{

            Toast.makeText(this, getString(R.string.track_is_missing), Toast.LENGTH_SHORT).show();
        }
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
    private void setupSeekBar(){

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
    private void createDrawerItemFragment(int drawerItemIndex, Fragment fragment, String tag) {

        selectDrawerItem(drawerItemIndex);

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
    public void selectDrawerItem(int drawerItemIndex) {

        if (!drawerItems.get(drawerItemIndex).isSelected()) {

            drawerItems.get(selectedDrawerItemIndex).setSelected(false);

            selectedDrawerItemIndex = drawerItemIndex;

            drawerItems.get(selectedDrawerItemIndex).setSelected(true);
        }
    }


    //Initializing items in drawer panel and associate them with corresponding Drawer Item Fragment
    @OnClick(R.id.music_player_drawer_item)
    void musicPlayerSelect() {

        selectDrawerItem(0);

        checkDrawerItemFragment();
        onBackPressed();
        findViewById(R.id.main_frame).setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.playlists_drawer_item)
    void playlistsSelect() {
        createDrawerItemFragment(1, new PlaylistsFragment(), "Playlist fragment");
    }

    @OnClick(R.id.albums_drawer_item)
    void albumsSelect() {
        createDrawerItemFragment(2, new AlbumsFragment(), "Albums fragment");
    }

    @OnClick(R.id.artists_drawer_item)
    void artistsSelect() {
        createDrawerItemFragment(3, new ArtistsFragment(), "Artists fragment");
    }

    @OnClick(R.id.equalizer_drawer_item)
    void equalizerSelect() {
        createDrawerItemFragment(4, new EqualizerFragment(), "Equalizer fragment");
    }

    @OnClick(R.id.background_drawer_item)
    void backgroundSelect() {
        createDrawerItemFragment(5, new BackgroundChangerFragment(), "Background fragment");
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
