package com.mnemo.angler;



import android.Manifest;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

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


import com.mnemo.angler.albums.AlbumsFragmentV2;
import com.mnemo.angler.artists.ArtistsFragment;
import com.mnemo.angler.background_changer.BackgroundChangerFragmentv2;
import com.mnemo.angler.background_changer.ImageAssistant;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerDBUpdateLoader;
import com.mnemo.angler.equalizer.EqualizerFragment;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.playlist_manager.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.playlist_manager.LyricsDialogFragment;
import com.mnemo.angler.playlist_manager.PlaylistManagerFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements MainPlaylistFragment.TrackFragmentListener,ArtistFragment.ArtistFragmentListener, LoaderManager.LoaderCallbacks {

    // Media Browser variable
    private MediaBrowser mMediaBrowser;


    // Media panel buttons views (initializing with butterknife
    @BindView(R.id.media_panel_play_pause)
    ImageButton mPlayPauseButton;

    @BindView(R.id.media_panel_add_to_playlist)
    ImageButton addTrackToPlaylist;

    @BindView(R.id.media_panel_lyrics)
    ImageButton showLyrics;

    @BindView(R.id.media_panel_seek_bar)
    SeekBar seekBar;


    // other variables
    private long durationMS;
    public static float density;
    private static final int LOADER_DB_UPDATE_ID = 1;


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
        mMediaBrowser = new MediaBrowser(this, new ComponentName(this, AnglerService.class), clientCallback, null);

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, AnglerService.class);
            intent.putExtra("active_playlist", getPreferences(Context.MODE_PRIVATE).getString("active_playlist", AnglerContract.SourceEntry.SOURCE_LIBRARY));
            startService(intent);

        }

        mMediaBrowser.connect();

        mMediaBrowser.subscribe("media_space", new MediaBrowser.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowser.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);

                if (!AnglerService.isDBInitialized) {
                    getLoaderManager().initLoader(LOADER_DB_UPDATE_ID, null, MainActivity.this);
                }
            }

        });


        setupBackground();


        // Setup seekbar
        final Handler seekHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (durationMS != 0) {
                    seekBar.setProgress((int) (AnglerService.seekProgress * 100 / durationMS));
                }
                seekHandler.postDelayed(this, 100);
            }
        };

        runOnUiThread(runnable);


        // create main fragment or restore visibility of main frame
        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, new MainFragment(), "Main fragment")
                    .commit();

        }else{

            int mainFrameVisibility = savedInstanceState.getInt("main_frame_visibility");
            findViewById(R.id.main_frame).setVisibility(mainFrameVisibility);
        }



    }

    // Media Browser client callbacks
    MediaBrowser.ConnectionCallback clientCallback = new MediaBrowser.ConnectionCallback() {
        @Override
        public void onConnected() {
            super.onConnected();

            MediaSession.Token token = mMediaBrowser.getSessionToken();
            MediaController mController = new MediaController(MainActivity.this, token);

            setMediaController(mController);

            buildTransport();
        }

    };


    private void buildTransport() {


        // Setup play/pause button
        if (getMediaController().getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
            mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);
        }

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pbState = getMediaController().getPlaybackState().getState();

                if (pbState == PlaybackState.STATE_PLAYING) {
                    getMediaController().getTransportControls().pause();
                } else {
                    getMediaController().getTransportControls().sendCustomAction(PlaybackManager.RESUME, null);
                }
            }
        });


        // Setup seekbar change listener
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
        MediaMetadata metadata = getMediaController().getMetadata();

        if (metadata != null) {
            showDescription(metadata);
            configureTrackButtons(metadata);
        }

        getMediaController().registerCallback(controllerCallback);

    }


    // Media Controller callbacks
    MediaController.Callback controllerCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            super.onPlaybackStateChanged(state);

            if (state.getState() == PlaybackState.STATE_PLAYING) {
                mPlayPauseButton.setImageResource(R.drawable.ic_pause_black_48dp);

            } else {
                mPlayPauseButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);

            }

        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            super.onMetadataChanged(metadata);

            Intent intent = new Intent();
            intent.setAction("track_changed");

            sendBroadcast(intent);

            showDescription(metadata);
            configureTrackButtons(metadata);


        }
    };


    // Show current track metadata in views
    private void showDescription(@Nullable MediaMetadata metadata) {

        //set metadata variables
        String title = String.valueOf(metadata.getDescription().getTitle());
        String artist = String.valueOf(metadata.getDescription().getSubtitle());
        durationMS = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);


        //binding metadata to views
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            TextView titleView = findViewById(R.id.media_panel_title);
            titleView.setText(artist + " - " + title);

        } else {

            TextView titleView = findViewById(R.id.media_panel_title);
            titleView.setText(title);

            TextView artistView = findViewById(R.id.media_panel_artist);
            artistView.setText(artist);

        }
    }


    // Setup add track to playlist and lyrics button based on current metadata
    private void configureTrackButtons(@Nullable MediaMetadata metadata) {

        addTrackToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddTrackToPlaylistDialogFragment addTrackToPlaylistDialogFragment = new AddTrackToPlaylistDialogFragment();
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

        if (getMediaController() != null) {
            getMediaController().unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("main_frame_visibility", findViewById(R.id.main_frame).getVisibility());
    }



    // Creating Drawer Item Fragment
    private void createDrawerItemFragment(View v, Fragment fragment, String tag) {
        if (!v.isSelected()) {
            deselectDrawerItem();
        }
        v.setSelected(true);

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
/*
         TextView[] drawerItems = new TextView[]{musicPlayerTextView, playlistsTextView, albumsTextView, artistsTextView,
                 backgroundTextView, equalizerTextView};

         for (TextView t : drawerItems){
             if (t.isSelected()){
                 t.setSelected(false);
             }
         }

        topLine.setVisibility(View.VISIBLE);
        topLine2.setVisibility(View.VISIBLE);
        bottomLine.setVisibility(View.VISIBLE);*/
    }

    @Override
    public void trackClicked() {
        getMediaController().getTransportControls().play();
    }

    @Override
    public void artistClicked(String artist) {

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
    public void showBackgroundLabel(String label) {
        TextView mArtistBackLabel = findViewById(R.id.artist_back);
        if (mArtistBackLabel != null) {
            mArtistBackLabel.setVisibility(View.VISIBLE);
            mArtistBackLabel.setText(label);
        }
    }

    public void hideBackgroundLabel() {
        TextView mArtistBackLabel = findViewById(R.id.artist_back);
        if (mArtistBackLabel != null) {
            mArtistBackLabel.setVisibility(View.INVISIBLE);
        }
    }

    public void showBackground() {
        findViewById(R.id.background_group).setVisibility(View.VISIBLE);
    }

    public void hideBackground() {
        findViewById(R.id.background_group).setVisibility(View.GONE);
    }

    public void showMediaPanel() {
        findViewById(R.id.media_panel_group).setVisibility(View.VISIBLE);
    }

    public void hideMediaPanel() {
        findViewById(R.id.media_panel_group).setVisibility(View.GONE);
    }

    public void showFrame() {
        findViewById(R.id.frame).setVisibility(View.VISIBLE);
    }

    public void hideFrame() {
        findViewById(R.id.frame).setVisibility(View.GONE);
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
    void musicPlayerSelect() {
        deselectDrawerItem();
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
        getMediaController().getTransportControls().sendCustomAction(PlaybackManager.REPEAT, null);
        if (PlaybackManager.repeatState) {
            v.setAlpha(0.4f);
        } else {
            v.setAlpha(0.8f);
        }
    }

    @OnClick(R.id.media_panel_shuffle)
    void shuffle(View v) {

        getMediaController().getTransportControls().sendCustomAction(PlaybackManager.SHUFFLE, null);
        if (PlaybackManager.shuffleState) {
            v.setAlpha(0.4f);
        } else {
            v.setAlpha(0.8f);
        }
    }

}
