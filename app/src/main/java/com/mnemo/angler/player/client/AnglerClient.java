package com.mnemo.angler.player.client;


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
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.player.service.AnglerService;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.util.MediaAssistant;

import java.util.ArrayList;
import java.util.List;

public class AnglerClient implements AnglerClientView{

    private AnglerClientPresenter presenter;

    private Context context;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mController;

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

        mMediaBrowser.subscribe("media_space", new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);

            }
        });

        // Bind Presenter to View
        presenter = new AnglerClientPresenter();
        presenter.attachView(this);
    }


    public void connect(){

        mMediaBrowser.connect();

        presenter.attachView(this);
        queueFilter = presenter.getQueueFilter();
    }

    public void disconnect(){

        if (mController != null) {
            mController.unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();

        presenter.saveQueueFilter(queueFilter);
        presenter.deattachView();
    }

    public void playPause(){

        int pbState = mController.getPlaybackState().getState();

        switch (pbState){

            case PlaybackStateCompat.STATE_PLAYING:

                mController.getTransportControls().pause();

                break;

            case PlaybackStateCompat.STATE_ERROR:

                Toast.makeText(context, context.getString(R.string.track_is_missing), Toast.LENGTH_SHORT).show();

                break;

            default:

                mController.getTransportControls().play();

                break;
        }

    }

    public boolean changeRepeatMode(){

        int repeatMode = mController.getRepeatMode();

        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE){
            mController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
            return false;
        }else{
            mController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
            return true;
        }
    }

    public boolean changeShuffleMode(){

        int shuffleMode = mController.getShuffleMode();

        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL){
            mController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
            return false;
        }else{
            mController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            return true;
        }
    }

    public void nextTrack(){
        mController.getTransportControls().skipToNext();
    }

    public void previousTrack(){
        mController.getTransportControls().skipToPrevious();
    }

    public void playNow(String type, String playlistName, int position, List<Track> tracks){

        if (!playlistName.equals(getQueueTitle()) || (type.equals("music_player") && !((MainActivity)context).getFilter().equals(queueFilter))) {

            setupQueue(type, playlistName, tracks);
        }

        mController.getTransportControls().skipToQueueItem(position);

    }

    private void setupQueue(String type, String playlistName, List<Track> tracks){

        clearQueue();

        for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(playlistName, tracks)) {

            mController.addQueueItem(description);

        }

        Bundle bundle = new Bundle();
        bundle.putString("queue_title", playlistName);

        mController.getTransportControls().sendCustomAction("set_queue_title", bundle);
        mController.getTransportControls().sendCustomAction("update_queue", null);

        if (type.equals("music_player")){
            queueFilter = ((MainActivity)context).getFilter();
        }
    }


    public ArrayList<MediaSessionCompat.QueueItem> getQueue(){
        return (ArrayList<MediaSessionCompat.QueueItem>)mController.getQueue();
    }

    public String getQueueTitle(){
        return mController.getQueueTitle().toString();
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

        Bundle bundle = new Bundle();
        bundle.putString("queue_title", "");

        mController.getTransportControls().sendCustomAction("set_queue_title", bundle);
        mController.getTransportControls().sendCustomAction("update_queue", null);
    }

    public void addToQueue(Track track, String playlist, boolean isPlayNext){

        MediaDescriptionCompat description = MediaAssistant.mergeMediaDescription(track, playlist);

        if (isPlayNext){
            mController.addQueueItem(description, 1);
        }else{
            mController.addQueueItem(description);
        }

        Bundle bundle = new Bundle();
        bundle.putString("queue_title", "");

        mController.getTransportControls().sendCustomAction("set_queue_title", bundle);
        mController.getTransportControls().sendCustomAction("update_queue", null);
    }


    public void addToPlaylistQueue(List<Track> tracks){

        for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray(getQueueTitle(), tracks)) {
            mController.addQueueItem(description);
        }

        mController.getTransportControls().sendCustomAction("update_queue", null);
    }

    public void skipToQueuePosition(long position){
        mController.getTransportControls().skipToQueueItem(position);
    }

    public void removeQueueItemAt(int position){

        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

        mController.getTransportControls().sendCustomAction("remove_queue_item", bundle);

        Bundle bundle2 = new Bundle();
        bundle2.putString("queue_title", "");

        mController.getTransportControls().sendCustomAction("set_queue_title", bundle2);
    }

    public void replaceQueueItems(int oldPosition, int newPosition){

        Bundle bundle = new Bundle();
        bundle.putInt("old_position", oldPosition);
        bundle.putInt("new_position", newPosition);

        mController.getTransportControls().sendCustomAction("replace_queue_items", bundle);
    }

    public void setQueuePosition(int position){

        Bundle bundle = new Bundle();
        bundle.putInt("queue_position", position);

        mController.getTransportControls().sendCustomAction("update_queue_position", bundle);
    }


    public int getQueuePosition() {

        return (int) mController.getPlaybackState().getActiveQueueItemId();
    }

    public void clearQueue(){
        mController.getTransportControls().sendCustomAction("clear_queue", null);
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
                setRepeatState(mController.getRepeatMode());
                setShuffleState(mController.getShuffleMode());

                MediaMetadataCompat metadata = mController.getMetadata();

                if (metadata != null) {

                    Intent intent = new Intent();
                    intent.setAction("initialize_media");
                    intent.putExtra("is_media", true);
                    context.sendBroadcast(intent);

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

                    mController.getTransportControls().sendCustomAction("get_track_progress", null);
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

    // set repeat state
    private void setRepeatState(int repeatMode){

        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE){
            ((MainActivity)context).setRepeatState(true);
        }else{
            ((MainActivity)context).setRepeatState(false);
        }
    }

    // set shuffle state
    private void setShuffleState(int shuffleMode){

        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL){
            ((MainActivity)context).setShuffleState(true);
        }else{
            ((MainActivity)context).setShuffleState(false);
        }
    }

    // getters/setters
    public long getDurationMS() {
        return durationMS;
    }

    // client/service bundle methods
    private void openClientServiceBundle(Bundle bundle){

        if (bundle != null) {
            serviceBundle = bundle.getBundle("service_bundle");
        }
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
