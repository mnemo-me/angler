package com.mnemo.angler;



import android.Manifest;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadata;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.albums.AlbumsFragmentV2;
import com.mnemo.angler.background_changer.BackgroundChangerFragmentv2;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerDBUpdateLoader;
import com.mnemo.angler.data.MediaAssistant;
import com.mnemo.angler.equalizer.EqualizerFragment;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.music_player.ArtistsFragment;
import com.mnemo.angler.music_player.ArtistTrackFragment;
import com.mnemo.angler.music_player.MusicPlayerFragment;
import com.mnemo.angler.playlists.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.playlists.LyricsDialogFragment;
import com.mnemo.angler.playlists.PlaylistManagerFragment;
import com.mnemo.angler.queue_manager.QueueDialogFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements ArtistsFragment.ArtistFragmentListener, LoaderManager.LoaderCallbacks {

    // Media Browser variable
    private MediaBrowserCompat mMediaBrowser;


    // Media panel buttons views (initializing with butterknife
    @BindView(R.id.media_panel_play_pause)
    ImageButton mPlayPauseButton;

    @BindView(R.id.media_panel_add_to_playlist)
    ImageButton addTrackToPlaylist;

    @BindView(R.id.media_panel_lyrics)
    ImageButton showLyrics;

    @BindView(R.id.media_panel_seek_bar)
    SeekBar seekBar;

    @BindView(R.id.media_panel_progress_time)
    TextView progressView;

    @BindView(R.id.media_panel_duration_time)
    TextView durationView;

    @BindView(R.id.media_panel_queue)
    ImageButton queueButton;

    @BindViews({R.id.music_player_drawer_item, R.id.playlists_drawer_item, R.id.albums_drawer_item, R.id.artists_drawer_item, R.id.equalizer_drawer_item, R.id.background_drawer_item})
    List<TextView> drawerItems;


    // other variables
    private long durationMS;
    public static float density;
    private String mainPlaylistName;
    private String filter = "";
    private static final int LOADER_DB_UPDATE_ID = 1;
    private String playlistQueue = "";
    private String queueFilter = "";
    private String currentTrackPlaylist = "";
    private String currentMediaId = "";

    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    int queuePosition = 0;

    private Bundle serviceBundle;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;

        ButterKnife.bind(this);

        createAppFolder();


        // Connect to Media Browser Service
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, AnglerService.class), clientCallback, null);

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, AnglerService.class);
            startService(intent);

        }

        mMediaBrowser.connect();

        mMediaBrowser.subscribe("media_space", new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);

                if (!AnglerService.isDBInitialized) {
                    getLoaderManager().initLoader(LOADER_DB_UPDATE_ID, null, MainActivity.this);
                }
            }

        });


        setupBackground();


        // Setup SeekBar
        final Handler seekHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (durationMS != 0) {

                    long progressTime = AnglerService.seekProgress;
                    seekBar.setProgress((int)(progressTime * 100 / durationMS));
                    progressView.setText(convertToTime(progressTime));
                }
                seekHandler.postDelayed(this, 100);
            }
        };

        runOnUiThread(runnable);


        // create main fragment or restore visibility of main frame and restore filters
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new MusicPlayerFragment(), "Main fragment")
                    .commit();

        }else{

            int mainFrameVisibility = savedInstanceState.getInt("main_frame_visibility");
            findViewById(R.id.main_frame).setVisibility(mainFrameVisibility);

            mainPlaylistName = savedInstanceState.getString("main_playlist_name");
            filter = savedInstanceState.getString("filter");
            playlistQueue = savedInstanceState.getString("playlist_queue");
            queueFilter = savedInstanceState.getString("queue_filter");
            currentTrackPlaylist = savedInstanceState.getString("current_track_playlist");
            currentMediaId = savedInstanceState.getString("current_media_id");
            queuePosition = savedInstanceState.getInt("queue_position");
            serviceBundle = savedInstanceState.getBundle("service_bundle");
        }

        queueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MediaControllerCompat.getMediaController(MainActivity.this).getQueue().size() > 0){
                    QueueDialogFragment queueDialogFragment = new QueueDialogFragment();
                    queueDialogFragment.show(getSupportFragmentManager(), "queue_dialog_fragment");
                }else{
                    Toast.makeText(MainActivity.this, R.string.empty_queue, Toast.LENGTH_SHORT).show();
                }
            }
        });



        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "queue_position_changed":

                        queuePosition = intent.getIntExtra("queue_position", 0);

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("queue_position_changed");

        registerReceiver(receiver, intentFilter);

    }

    // Media Browser client callbacks
    MediaBrowserCompat.ConnectionCallback clientCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            super.onConnected();

            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
            try {
                MediaControllerCompat mController = new MediaControllerCompat(MainActivity.this, token);
                MediaControllerCompat.setMediaController(MainActivity.this, mController);

                if (serviceBundle == null) {
                    serviceBundle = MediaControllerCompat.getMediaController(MainActivity.this).getExtras();
                }

            }catch (RemoteException e){
                e.printStackTrace();
            }


            buildTransport();
        }

    };


    private void buildTransport() {


        // Setup play/pause button
        if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);
        }

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = getMediaController().getPlaybackState().getState();

                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    getMediaController().getTransportControls().pause();
                } else {
                    getMediaController().getTransportControls().play();
                }
            }
        });


        // Setup SeekBar change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    getMediaController().getTransportControls().seekTo(progress);
                    if (!AnglerService.isSeekAvailable) {
                        seekBar.setProgress(0);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        // Get current metadata
        MediaMetadataCompat metadata = MediaControllerCompat.getMediaController(MainActivity.this).getMetadata();

        if (metadata != null) {
            showDescription(metadata);
            configureTrackButtons(metadata);
        }

        MediaControllerCompat.getMediaController(MainActivity.this).registerCallback(controllerCallback);

    }


    // Media Controller callbacks
    MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);

            } else {
                mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);

            }

        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            currentTrackPlaylist = metadata.getString("track_playlist");
            currentMediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);

            Intent intent = new Intent();
            intent.setAction("track_changed");
            intent.putExtra("track_playlist", currentTrackPlaylist);
            intent.putExtra("media_id", currentMediaId);

            sendBroadcast(intent);

            showDescription(metadata);
            configureTrackButtons(metadata);


        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);

            Log.e("1111", String.valueOf(queue.size()));
        }
    };


    // Show current track metadata in views
    private void showDescription(@Nullable MediaMetadataCompat metadata) {

        //set metadata variables
        String title = String.valueOf(metadata.getDescription().getTitle());
        String artist = String.valueOf(metadata.getDescription().getSubtitle());
        durationMS = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);


        //binding metadata to views
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            TextView titleView = findViewById(R.id.media_panel_title);
            titleView.setText(artist + " - " + title);

            durationView.setText(convertToTime(durationMS));

        } else {

            TextView titleView = findViewById(R.id.media_panel_title);
            titleView.setText(title);

            TextView artistView = findViewById(R.id.media_panel_artist);
            artistView.setText(artist);

            durationView.setText(" / " + convertToTime(durationMS));
        }

    }


    // Setup add track to playlist and lyrics button based on current metadata
    private void configureTrackButtons(@Nullable MediaMetadataCompat metadata) {

        final Bundle args = MediaAssistant.putMetadataInBundle(metadata);

        addTrackToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();
                addTrackToPlaylistDialogFragment.setArguments(args);
                addTrackToPlaylistDialogFragment.show(getSupportFragmentManager(), "Add track to playlist dialog");

            }
        });

        showLyrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LyricsDialogFragment lyricsDialogFragment = new LyricsDialogFragment();
                lyricsDialogFragment.show(getSupportFragmentManager(), "Lyrics dialog");

            }
        });
    }


    // Static method converting time from milliseconds in human readable format
    public static String convertToTime(long durationMS) {

        long durationS = durationMS / 1000;

        int hours = (int) durationS / 3600;
        int minutes = ((int) durationS - hours * 3600) / 60;
        int seconds = (int) durationS - hours * 3600 - minutes * 60;

        if (hours == 0) {
            return String.format("%2d:%02d", minutes, seconds);
        } else {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }


    // Setup background
    private void setupBackground() {

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        String backgroundImage = sharedPref.getString("background", "R.drawable.back");

        if (!backgroundImage.startsWith("R.drawable.")) {
            if (!new File(backgroundImage).exists()) {
                backgroundImage = "R.drawable.back";
                sharedPref.edit().putString("background", backgroundImage).apply();
            }
        }

        int imageHeight;

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            imageHeight = 520;
        }else{
            backgroundImage = backgroundImage.replace("port", "land");
            imageHeight = 203;
        }

        ImageView background = findViewById(R.id.main_fragment_background);
        ImageAssistant.loadImage(this, backgroundImage, background, imageHeight);

        int alpha = sharedPref.getInt("overlay", 203);
        setOverlay(alpha);

    }


    // Setup skrim
    public void setOverlay(int alpha) {
        FrameLayout overlay = findViewById(R.id.overlay);
        overlay.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("main_frame_visibility", findViewById(R.id.main_frame).getVisibility());
        outState.putString("main_playlist_name", mainPlaylistName);
        outState.putString("filter", filter);
        outState.putString("playlist_queue", playlistQueue);
        outState.putString("queue_filter", queueFilter);
        outState.putString("current_track_playlist", currentTrackPlaylist);
        outState.putString("current_media_id", currentMediaId);
        outState.putInt("queue_position", queuePosition);
        outState.putBundle("service_bundle", serviceBundle);
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

        findViewById(R.id.main_frame).setVisibility(View.GONE);
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(Gravity.START);
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




    public void playNow(String playlistName, int position, Cursor cursor){

        if (!playlistName.equals(playlistQueue) || !filter.equals(queueFilter)) {


            MediaControllerCompat.getMediaController(this).getTransportControls().sendCustomAction("clear_queue", null);

            for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, cursor)) {

                MediaControllerCompat.getMediaController(this).addQueueItem(description);

            }

            playlistQueue = playlistName;
            MediaControllerCompat.getMediaController(this).getTransportControls().sendCustomAction("update_queue", null);

        }

        MediaControllerCompat.getMediaController(this).getTransportControls().skipToQueueItem(position);

    }

    public void addToQueue(String playlistName, Cursor cursor, boolean isPlayNext){

        int index = 0;

        for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, cursor)) {

            if (isPlayNext){
                MediaControllerCompat.getMediaController(this).addQueueItem(description, ++index);
            }else{
                MediaControllerCompat.getMediaController(this).addQueueItem(description);
            }

        }

        playlistQueue = "";
        MediaControllerCompat.getMediaController(this).getTransportControls().sendCustomAction("update_queue", null);
    }

    public void addToQueue(MediaDescriptionCompat description, boolean isPlayNext){

        if (isPlayNext){
            MediaControllerCompat.getMediaController(this).addQueueItem(description, 1);
        }else{
            MediaControllerCompat.getMediaController(this).addQueueItem(description);
        }

        playlistQueue = "";
        MediaControllerCompat.getMediaController(this).getTransportControls().sendCustomAction("update_queue", null);
    }






    @Override
    public void artistClicked(String artist) {

        filter = "";
        ((SearchView)findViewById(R.id.search_toolbar)).setQuery("", false);

        Bundle bundle = new Bundle();
        bundle.putString("artist", artist);

        ArtistTrackFragment artistTrackFragment = new ArtistTrackFragment();
        artistTrackFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.song_list, artistTrackFragment)
                .addToBackStack(null)
                .commit();

    }










    // Show/hide methods

    public void showBackground() {
        findViewById(R.id.background_group).setVisibility(View.VISIBLE);
    }

    public void hideBackground() {
        findViewById(R.id.background_group).setVisibility(View.GONE);
    }



    @Override
    public void onBackPressed() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START);
        } else if (backStackCount == 1 || (backStackCount == 2 && getSupportFragmentManager().findFragmentById(R.id.song_list) instanceof ArtistTrackFragment)) {
            findViewById(R.id.main_frame).setVisibility(View.VISIBLE);

            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }


    private void createAppFolder() {

        new File(AnglerFolder.PATH_MAIN).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE).mkdir();
        new File(AnglerFolder.PATH_PLAYLIST_COVER).mkdir();
        new File(AnglerFolder.PATH_ALBUM_COVER).mkdir();
        new File(AnglerFolder.PATH_ARTIST_IMAGE).mkdir();

        try {
            new File(AnglerFolder.PATH_MAIN, ".nomedia").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_DB_UPDATE_ID:
                return new AnglerDBUpdateLoader(this);
            default:
                return null;
        }


    }


    @Override
    public void onLoadFinished(Loader loader, Object o) {

        switch (loader.getId()) {
            case LOADER_DB_UPDATE_ID:

                AnglerService.isDBInitialized = true;
                try {
                    //((MainPlaylistFragment) (getSupportFragmentManager().findFragmentByTag("main_playlist_fragment"))).initializePlaylist();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

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
        createDrawerItemFragment(v, new PlaylistManagerFragment(), "Playlists fragment");

    }

    @OnClick(R.id.albums_drawer_item)
    void albumsSelect(View v) {
        createDrawerItemFragment(v, new AlbumsFragmentV2(), "Albums fragment");

    }

    @OnClick(R.id.artists_drawer_item)
    void artistsSelect(View v) {
        createDrawerItemFragment(v, new com.mnemo.angler.artists.ArtistsFragment(), "Artists fragment");

    }

    @OnClick(R.id.equalizer_drawer_item)
    void equalizerSelect(View v) {
        createDrawerItemFragment(v, new EqualizerFragment(), "Equalizer fragment");

    }

    @OnClick(R.id.background_drawer_item)
    void backgroundSelect(View v) {
        createDrawerItemFragment(v, new BackgroundChangerFragmentv2(), "Background fragment");

    }



    // Setup media control buttons

    @OnClick(R.id.media_panel_previous_track)
    void previousTrack() {
        getMediaController().getTransportControls().skipToPrevious();
    }

    @OnClick(R.id.media_panel_next_track)
    void nextTrack() {
        getMediaController().getTransportControls().skipToNext();
    }

    @OnClick(R.id.media_panel_repeat)
    void repeat(View v) {

        if (MediaControllerCompat.getMediaController(this).getRepeatMode() == 1) {
            v.setAlpha(0.4f);
            MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(0);
        } else {
            v.setAlpha(0.8f);
            MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(1);
        }
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

    public String getMainPlaylistName() {
        return mainPlaylistName;
    }

    public void setMainPlaylistName(String mainPlaylistName) {
        this.mainPlaylistName = mainPlaylistName;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getCurrentTrackPlaylist() {
        return currentTrackPlaylist;
    }

    public String getCurrentMediaId() {
        return currentMediaId;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public Bundle getServiceBundle() {
        return serviceBundle;
    }
}
