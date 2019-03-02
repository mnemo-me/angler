package com.mnemo.angler.ui.main_activity.activity;



import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
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
import com.mnemo.angler.ui.main_activity.misc.trial_expired.TrialExpiredDialogFragment;
import com.mnemo.angler.ui.main_activity.misc.welcome.WelcomeDialogFragment;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.util.MediaAssistant;
import com.mnemo.angler.ui.main_activity.fragments.equalizer.equalizer.EqualizerFragment;

import com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist.AddTrackToPlaylistDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlists.PlaylistsFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements MainActivityView {

    private MainActivityPresenter presenter;
    private AnglerClient anglerClient;
    private BillingClient billingClient;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.main_frame)
    FrameLayout mainFrame;

    @BindView(R.id.trial_version_text)
    TextView trialVersionText;

    @BindView(R.id.purchase_button)
    TextView purchaseButton;


    // Media panel buttons views (initializing with ButterKnife
    @BindView(R.id.media_panel_play_pause)
    ImageButton mPlayPauseButton;

    @BindView(R.id.media_panel_repeat)
    ImageButton repeatButton;

    @BindView(R.id.media_panel_shuffle)
    ImageButton shuffleButton;

    @BindView(R.id.media_panel_title)
    TextView titleView;

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

    // bind lines
    @BindView(R.id.grey_line1)
    View greyLine1;

    @BindView(R.id.grey_line2)
    View greyLine2;

    @BindView(R.id.grey_line3)
    View greyLine3;


    // other variables
    private Boolean isTrialAvailable;
    public static float density;
    private int orientation;

    private BroadcastReceiver receiver;

    private String mainPlaylistName = "library";
    private String currentPlaylistName = "";
    private String filter = "";

    private int selectedDrawerItemIndex = 0;

    private boolean isMedia = false;

    private BillingFlowParams flowParams;


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

                isTrialAvailable = savedInstanceState.getBoolean("is_trial_available");
                isMedia = savedInstanceState.getBoolean("is_media");

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

            // Check app status
            checkAppStatus();

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


                    case "initialize_media":

                        boolean initializeMedia = intent.getExtras().getBoolean("is_media");

                        if (isMedia != initializeMedia) {

                            isMedia = initializeMedia;

                            if (!isMedia) {

                                titleView.setText(getResources().getString(R.string.no_media));
                                seekBar.setEnabled(false);
                            } else {
                                seekBar.setEnabled(true);
                            }
                        }

                        break;

                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("seekbar_progress_changed");
        intentFilter.addAction("queue_error");
        intentFilter.addAction("initialize_media");

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

        if (isTrialAvailable != null) {
            outState.putBoolean("is_trial_available", isTrialAvailable);
        }

        outState.putBoolean("is_media", isMedia);

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

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if (backStackCount == 1 ) {
            findViewById(R.id.main_frame).setVisibility(View.VISIBLE);
            selectDrawerItem(0);

            super.onBackPressed();

        }else if (getSupportFragmentManager().findFragmentByTag("artist_track_fragment") != null &&
                backStackCount == 0 &&
                orientation == Configuration.ORIENTATION_PORTRAIT){

            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag("artist_track_fragment"))
                    .commit();

            ((MusicPlayerFragment)getSupportFragmentManager().findFragmentByTag("music_player_fragment")).setArtistSelected(null);

            findViewById(R.id.song_list).setVisibility(View.VISIBLE);
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

            titleView.setText(artist + " - " + title);

            durationView.setText(MediaAssistant.convertToTime(durationMS));

        } else {

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

    @Override
    public void setTrial( boolean isTrialAvailable) {

        this.isTrialAvailable = isTrialAvailable;
        checkTrial();
    }

    @Override
    public void showWelcomeDialog() {

        WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();

        welcomeDialogFragment.show(getSupportFragmentManager(), "Welcome dialog fragment");

    }

    // Setup media control buttons
    @OnClick(R.id.media_panel_play_pause)
    void playPause(){

        if (!isMedia){
            showNoMediaMessage();
            return;
        }

        anglerClient.playPause();
    }


    @OnClick(R.id.media_panel_next_track)
    void nextTrack(){

        if (!isMedia){
            showNoMediaMessage();
            return;
        }

        anglerClient.nextTrack();
    }

    @OnClick(R.id.media_panel_previous_track)
    void previousTrack(){

        if (!isMedia){
            showNoMediaMessage();
            return;
        }

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

        if (!isMedia){
            showNoMediaMessage();
            return;
        }

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

        seekBar.setEnabled(isMedia);

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
        drawer.closeDrawer(GravityCompat.START);
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

            manageGreyLinesVisibility();

            drawerItems.get(selectedDrawerItemIndex).setSelected(true);
        }
    }

    // Show / hide grey lines
    private void manageGreyLinesVisibility(){

        switch (selectedDrawerItemIndex){

            case 0:

                greyLine1.setVisibility(View.INVISIBLE);
                greyLine2.setVisibility(View.INVISIBLE);
                greyLine3.setVisibility(View.VISIBLE);

                break;

            case 1:

                greyLine1.setVisibility(View.VISIBLE);
                greyLine2.setVisibility(View.INVISIBLE);
                greyLine3.setVisibility(View.VISIBLE);

                break;

            case 5:

                greyLine1.setVisibility(View.VISIBLE);
                greyLine2.setVisibility(View.VISIBLE);
                greyLine3.setVisibility(View.INVISIBLE);

                break;

            default:

                greyLine1.setVisibility(View.VISIBLE);
                greyLine2.setVisibility(View.VISIBLE);
                greyLine3.setVisibility(View.VISIBLE);

                break;
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

    @OnClick(R.id.purchase_button)
    public void purchaseAngler(){

        if (billingClient != null && flowParams != null) {
            billingClient.launchBillingFlow(this, flowParams);
        }
    }


    // Show/hide methods
    public void showBackground() {
        findViewById(R.id.background_group).setVisibility(View.VISIBLE);
    }

    public void hideBackground() {
        findViewById(R.id.background_group).setVisibility(View.GONE);
    }


    // Show "no media" message
    private void showNoMediaMessage(){
        Toast.makeText(this, R.string.no_media, Toast.LENGTH_SHORT).show();
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

    public boolean isMedia() {
        return isMedia;
    }

    public void setMedia(boolean isMedia) {
        this.isMedia = isMedia;
    }



    // App status methods
    private void checkAppStatus(){
        checkPremium();
    }

    private void checkPremium(){

        billingClient = BillingClient.newBuilder(this).setListener((responseCode, purchases) -> {

            if (responseCode == BillingClient.BillingResponse.OK && purchases != null){

                Purchase purchase = purchases.get(0);

                if (purchase.getSku().equals("premium")){

                    trialVersionText.setVisibility(View.GONE);
                    purchaseButton.setVisibility(View.GONE);
                }
            }

        }).build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {

                if (responseCode == BillingClient.BillingResponse.OK) {

                    // Check premium purchase
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);

                    if (purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK){

                        List<Purchase> purchases = purchasesResult.getPurchasesList();

                        if (purchases.size() > 0){

                            if (purchases.get(0).getSku().equals("premium")){
                                return;
                            }
                        }

                        // Query in-app product list
                        List<String> skuList = new ArrayList<>();
                        skuList.add("premium");
                        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

                        billingClient.querySkuDetailsAsync(params.build(), (responseCode1, skuDetailsList) -> {

                            if (responseCode1 == BillingClient.BillingResponse.OK && skuDetailsList != null) {

                                if (skuDetailsList.size() > 0){
                                    SkuDetails premiumSkuDetails = skuDetailsList.get(0);
                                    Log.e("0909090", premiumSkuDetails.getPrice());

                                    flowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(premiumSkuDetails)
                                            .build();

                                    checkTrial();
                                }

                            }
                        });
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private void checkTrial(){

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        trialVersionText.setVisibility(View.VISIBLE);
        purchaseButton.setVisibility(View.VISIBLE);

        if (isTrialAvailable != null){

            if (!isTrialAvailable){

                if (getSupportFragmentManager().findFragmentByTag("Trial expired dialog") == null) {

                    TrialExpiredDialogFragment trialExpiredDialogFragment = new TrialExpiredDialogFragment();

                    trialExpiredDialogFragment.setCancelable(false);

                    trialExpiredDialogFragment.show(getSupportFragmentManager(), "Trial expired dialog");
                }
            }

        }else {

            presenter.checkTrial(androidId, new Date().getTime());
        }
    }
}
