package com.mnemo.angler.player;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.mnemo.angler.main_activity.MainActivity;
import com.mnemo.angler.util.MediaAssistant;

import java.util.List;

public class AnglerClient{

    private Context context;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mController;


    private String mainPlaylistName;
    private String filter = "";
    private String playlistQueue = "";
    private int queuePosition = 0;
    private String queueFilter = "";
    private String currentTrackPlaylist = "";
    private String currentMediaId = "";

    private long durationMS;

    private Bundle serviceBundle;


    // receiver
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    public AnglerClient(Context context, Bundle args) {
        this.context = context;
        openClientServiceBundle(args);
    }

    public void init(){

        // Connect to Media Browser Service
        mMediaBrowser = new MediaBrowserCompat(context, new ComponentName(context, AnglerService.class), clientCallback, null);

        // Launch service
        Intent intent = new Intent(context, AnglerService.class);
        context.startService(intent);


        mMediaBrowser.subscribe("media_space", new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);

            }
        });
    }


    public void connect(){
        mMediaBrowser.connect();

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

        context.registerReceiver(receiver, intentFilter);
    }

    public void disconnect(){
        mController.unregisterCallback(controllerCallback);
        mMediaBrowser.disconnect();
        context.unregisterReceiver(receiver);
    }

    public void playPause(){

        int pbState = mController.getPlaybackState().getState();

        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            mController.getTransportControls().pause();
        } else {
            mController.getTransportControls().play();
        }

    }

    public void nextTrack(){
        mController.getTransportControls().skipToNext();
    }

    public void previousTrack(){
        mController.getTransportControls().skipToPrevious();
    }

    public void playNow(String playlistName, int position, Cursor cursor){

        if (!playlistName.equals(playlistQueue) || !filter.equals(queueFilter)) {


            mController.getTransportControls().sendCustomAction("clear_queue", null);

            for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, cursor)) {

                mController.addQueueItem(description);

            }

            playlistQueue = playlistName;
            mController.getTransportControls().sendCustomAction("update_queue", null);

        }

        mController.getTransportControls().skipToQueueItem(position);

    }

    public void addToQueue(String playlistName, Cursor cursor, boolean isPlayNext){

        int index = 0;

        for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, cursor)) {

            if (isPlayNext){
                mController.addQueueItem(description, ++index);
            }else{
                mController.addQueueItem(description);
            }

        }

        playlistQueue = "";
        mController.getTransportControls().sendCustomAction("update_queue", null);
    }

    public void addToQueue(MediaDescriptionCompat description, boolean isPlayNext){

        if (isPlayNext){
            mController.addQueueItem(description, 1);
        }else{
            mController.addQueueItem(description);
        }

        playlistQueue = "";
        mController.getTransportControls().sendCustomAction("update_queue", null);
    }

    public void seekTo(int progress){
        mController.getTransportControls().seekTo(progress);
    }




    private void showMetadata(MediaMetadataCompat metadata){

        //get metadata variables
        String title = String.valueOf(metadata.getDescription().getTitle());
        String artist = String.valueOf(metadata.getDescription().getSubtitle());
        durationMS = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);

        ((MainActivity)context).showDescription(title, artist, durationMS);
    }

    public MediaMetadataCompat getCurrentMetadata(){

        return mController.getMetadata();
    }








    // Media Browser client callbacks
    private MediaBrowserCompat.ConnectionCallback clientCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            super.onConnected();

            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
            try {
                mController = new MediaControllerCompat(context, token);
                MediaControllerCompat.setMediaController((MainActivity)context, mController);
                mController.registerCallback(controllerCallback);

                if (serviceBundle == null) {
                    serviceBundle = mController.getExtras();
                }

                setPlayPause(mController.getPlaybackState().getState());

                MediaMetadataCompat metadata = mController.getMetadata();

                if (metadata != null) {
                    showMetadata(metadata);
                }

            }catch (RemoteException e){
                e.printStackTrace();
            }

        }

    };

    // Media Controller callbacks
    private MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            setPlayPause(state.getState());
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

            context.sendBroadcast(intent);

            showMetadata(metadata);
            //configureTrackButtons(metadata);


        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);

        }
    };



    // set play/pause based on state
    private void setPlayPause(int state){

        String playPauseState;

        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playPauseState = "play";
        } else {
            playPauseState = "pause";
        }

        ((MainActivity)context).setPlayPause(playPauseState);
    }



    // getters/setters
    public void setMainPlaylistName(String mainPlaylistName) {
        this.mainPlaylistName = mainPlaylistName;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getMainPlaylistName() {
        return mainPlaylistName;
    }

    public String getFilter() {
        return filter;
    }

    public int getQueuePosition() {
        return queuePosition;
    }


    public long getDurationMS() {
        return durationMS;
    }

    // client/service bundle methods
    private void openClientServiceBundle(Bundle bundle){

        if (bundle != null) {
            mainPlaylistName = bundle.getString("main_playlist_name");
            filter = bundle.getString("filter");
            playlistQueue = bundle.getString("playlist_queue");
            queueFilter = bundle.getString("queue_filter");
            currentTrackPlaylist = bundle.getString("current_track_playlist");
            currentMediaId = bundle.getString("current_media_id");
            queuePosition = bundle.getInt("queue_position");
            serviceBundle = bundle.getBundle("service_bundle");
        }
    }

    public Bundle getClientBundle(){

        Bundle bundle = new Bundle();
        bundle.putString("main_playlist_name", mainPlaylistName);
        bundle.putString("filter", filter);
        bundle.putString("playlist_queue", playlistQueue);
        bundle.putString("queue_filter", queueFilter);
        bundle.putString("current_track_playlist", currentTrackPlaylist);
        bundle.putString("current_media_id", currentMediaId);
        bundle.putInt("queue_position", queuePosition);
        bundle.putBundle("service_bundle", serviceBundle);

        return bundle;
    }

    public Bundle getServiceBundle() {
        return serviceBundle;
    }
}
