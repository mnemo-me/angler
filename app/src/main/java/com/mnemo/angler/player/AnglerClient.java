package com.mnemo.angler.player;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

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
import android.util.Log;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.util.MediaAssistant;

import java.util.ArrayList;
import java.util.List;

public class AnglerClient{

    private Context context;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mController;

    private String playlistQueue = "";
    private String queueFilter = "";

    private long durationMS;

    private Bundle serviceBundle;


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
    }

    public void disconnect(){

        if (mController != null) {
            mController.unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
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

    public void playNow(String playlistName, int position, List<Track> tracks){

        if (!playlistName.equals(playlistQueue) || !((MainActivity)context).getFilter().equals(queueFilter)) {


            mController.getTransportControls().sendCustomAction("clear_queue", null);

            for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, tracks)) {

                mController.addQueueItem(description);

            }

            playlistQueue = playlistName;
            mController.getTransportControls().sendCustomAction("update_queue", null);

        }

        mController.getTransportControls().skipToQueueItem(position);

    }

    public ArrayList<MediaSessionCompat.QueueItem> getQueue(){
        return (ArrayList<MediaSessionCompat.QueueItem>)mController.getQueue();
    }

    public void addToQueue(String playlistName, List<Track> tracks, boolean isPlayNext){

        int index = 0;

        for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, tracks)) {

            if (isPlayNext){
                mController.addQueueItem(description, ++index);
            }else{
                mController.addQueueItem(description);
            }

        }

        playlistQueue = "";
        mController.getTransportControls().sendCustomAction("update_queue", null);
    }

    public void addToQueue(Track track, String playlist, boolean isPlayNext){

        MediaDescriptionCompat description = MediaAssistant.mergeMediaDescription(track, playlist);

        if (isPlayNext){
            mController.addQueueItem(description, 1);
        }else{
            mController.addQueueItem(description);
        }

        playlistQueue = "";
        mController.getTransportControls().sendCustomAction("update_queue", null);
    }

    public void skipToQueuePosition(long position){
        mController.getTransportControls().skipToQueueItem(position);
    }

    public void removeQueueItemAt(int position){
        mController.removeQueueItemAt(position);
        playlistQueue = "";
    }

    public void replaceQueueItems(int oldPosition, int newPosition){

        Bundle bundle = new Bundle();
        bundle.putInt("old_position", oldPosition);
        bundle.putInt("new_position", newPosition);


        mController.getTransportControls().sendCustomAction("replace_queue_items", bundle);
    }

    public int getQueuePosition() {

        return (int) mController.getPlaybackState().getActiveQueueItemId();
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


    public void setEqualizer(boolean equalizerState){

        Bundle extras = new Bundle();
        extras.putBoolean("on_off_state", equalizerState);

        mController.getTransportControls().sendCustomAction("equalizer_on_off", extras);
    }

    public void setEqualizerBandLevel(short band, short bandLevel){

        Bundle extras = new Bundle();
        extras.putShort("band_number", band);
        extras.putShort("band_level", bandLevel);
        Log.e("fdfdfdf", band + " " + bandLevel);

        mController.getTransportControls().sendCustomAction("equalizer_change_band_level", extras);
    }

    public void setEqualizerPreset(short preset){

        Bundle extras = new Bundle();
        extras.putShort("preset_number", preset);

        mController.getTransportControls().sendCustomAction("equalizer_change_preset", extras);
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

                    String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);

                    Intent idIntent = new Intent();
                    idIntent.setAction("track_changed");
                    idIntent.putExtra("track_playlist", ((MainActivity)context).getCurrentPlaylistName());
                    idIntent.putExtra("media_id", mediaId);

                    context.sendBroadcast(idIntent);

                    Intent quIntent = new Intent();

                    quIntent.setAction("queue_position_changed");
                    quIntent.putExtra("queue_position", getQueuePosition());

                    context.sendBroadcast(quIntent);


                    int playbackState = mController.getPlaybackState().getState();

                    Intent pbIntent = new Intent();
                    pbIntent.setAction("playback_state_changed");
                    pbIntent.putExtra("playback_state", playbackState);

                    context.sendBroadcast(pbIntent);
                }

                // Initialize queue
                if (!AnglerService.isQueueInitialized){
                    ((MainActivity)context).initializeQueue();
                    AnglerService.isQueueInitialized = true;
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

            Intent intent = new Intent();
            intent.setAction("playback_state_changed");
            intent.putExtra("playback_state", state.getState());

            context.sendBroadcast(intent);
        }

        @Override
        public void onMetadataChanged(@Nullable MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            String trackPlaylist = metadata.getString("track_playlist");

            ((MainActivity)context).setCurrentPlaylistName(trackPlaylist);

            Intent intent = new Intent();
            intent.setAction("track_changed");
            intent.putExtra("track_playlist", trackPlaylist);
            intent.putExtra("media_id", mediaId);

            context.sendBroadcast(intent);

            showMetadata(metadata);
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
    public long getDurationMS() {
        return durationMS;
    }

    // client/service bundle methods
    private void openClientServiceBundle(Bundle bundle){

        if (bundle != null) {
            playlistQueue = bundle.getString("playlist_queue");
            queueFilter = bundle.getString("queue_filter");
            serviceBundle = bundle.getBundle("service_bundle");
        }
    }

    public Bundle getClientBundle(){

        Bundle bundle = new Bundle();
        bundle.putString("playlist_queue", playlistQueue);
        bundle.putString("queue_filter", queueFilter);
        bundle.putBundle("service_bundle", serviceBundle);

        return bundle;
    }

    public Bundle getServiceBundle() {
        return serviceBundle;
    }

    public String getCurrentMediaId(){
        if (mController != null) {
            if (mController.getMetadata() != null) {
                return mController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

    public int getPlaybackState(){
        if (mController != null) {
            return mController.getPlaybackState().getState();
        }else{
            return PlaybackStateCompat.STATE_PAUSED;
        }
    }
}
