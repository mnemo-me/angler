package com.mnemo.angler;



import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;


import com.mnemo.angler.data.MediaAssistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AnglerService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;
    private Equalizer mEqualizer;

    private ArrayList<Integer> bandsFrequencies;
    private ArrayList<String> equalizerPresets;


    private AnglerNotificationManager mAnglerNotificationManager;

    private ArrayList<MediaSessionCompat.QueueItem> queue;
    private int queueIndex = 0;

    public static long seekProgress;
    public static boolean isSeekAvailable;

    public static boolean isDBInitialized = false;
    public static boolean isQueueInitialized = false;


    public void onCreate() {

        super.onCreate();

        /*
        Setup media session on service, set playback state and callbacks
         */
        mMediaSession = new MediaSessionCompat(this, "Angler Service");
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(
                                    PlaybackStateCompat.ACTION_PLAY |
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                    PlaybackStateCompat.ACTION_PAUSE |
                                    PlaybackStateCompat.STATE_PLAYING).build());
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(anglerServiceCallback);


        /*
        set session token to connect to service from clients
         */
        setSessionToken(mMediaSession.getSessionToken());


        /*
        Run another thread, that watch on seek bar position from service
         */
        final Handler seekHandler = new Handler();
        seekHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null){
                    seekProgress = mMediaPlayer.getCurrentPosition();
                    if (!isSeekAvailable) {
                        isSeekAvailable = true;
                    }
                }
                seekHandler.postDelayed(this,100);
            }
        });


        mMediaPlayer = new MediaPlayer();

        // Setup equalizer
        setupEqualizer();

    }



    /*
    Initialize media session callbacks (play, pause, stop, etc)
     */
    MediaSessionCompat.Callback anglerServiceCallback = new MediaSessionCompat.Callback() {

        MediaMetadataCompat metadata;

        // queue methods
        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {

            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, description.hashCode());
            queue.add(queueItem);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {

            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, description.hashCode());
            queue.add(queueIndex + index, queueItem);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {

            String mediaId = description.getMediaId();

            for (MediaSessionCompat.QueueItem queueItem : queue){

                if (queueItem.getDescription().getMediaId().equals(mediaId)){

                    int index = queue.indexOf(queueItem);

                    if (index <= queueIndex){
                        queueIndex--;
                    }

                    queue.remove(queueItem);
                    break;
                }
            }

            mMediaSession.setQueue(queue);
        }

        @Override
        public void onRemoveQueueItemAt(int index) {

            if (index <= queueIndex){
                queueIndex--;
            }
            queue.remove(index);
            mMediaSession.setQueue(queue);
        }





        // control playback methods
        @Override
        public void onPrepare() {

            if (queueIndex < 0 && queue.isEmpty()){
                return;
            }

            metadata = getCurrentMetadata();
            mMediaSession.setMetadata(metadata);

            if (!mMediaSession.isActive()){
                mMediaSession.setActive(true);
            }

            Intent intent = new Intent();
            intent.setAction("queue_position_changed");
            intent.putExtra("queue_position", queueIndex);

            sendBroadcast(intent);
        }


        @Override
        public void onPlay() {

            onPrepare();

            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(AnglerService.this, metadata.getDescription().getMediaUri());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                mMediaPlayer.setOnCompletionListener(onCompletionListener);

                mAnglerNotificationManager.createNotification();

            }catch (IOException e){
                e.printStackTrace();
            }

        }



        @Override
        public void onPause() {
            super.onPause();

            mMediaPlayer.pause();

            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,0).build());

            mAnglerNotificationManager.unregisterCallback();
            stopForeground(false);
        }

        @Override
        public void onStop() {
            super.onStop();

            mAnglerNotificationManager.unregisterCallback();
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaPlayer.stop();
            stopSelf();

        }




        // skip methods
        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            if (queueIndex == 0){
                queueIndex = queue.size() - 1;
            }else{
                queueIndex--;
            }

            onPlay();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();

            if (queueIndex == queue.size() -1){
                queueIndex = 0;
            }else{
                queueIndex++;
            }

            onPlay();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            queueIndex = (int)id;
            onPlay();
        }




        // repeat / shuffle
        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
        }



        // SeekBar method
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo((int) (pos * mMediaPlayer.getDuration() / 100));
            }
        }



        // custom actions
        @Override
        public void onCustomAction(@NonNull String action, @Nullable Bundle extras) {
            super.onCustomAction(action, extras);

            switch(action){

                case "clear_queue":

                    if (queue != null){
                        queue.clear();
                    }

                    break;

                case "update_queue":

                    mMediaSession.setQueue(queue);

                    break;

                case "replace_queue_items":

                    int oldPosition = extras.getInt("old_position");
                    int newPosition = extras.getInt("new_position");

                    MediaSessionCompat.QueueItem queueItem = queue.get(oldPosition);
                    queue.remove(oldPosition);
                    queue.add(newPosition, queueItem);

                    if (oldPosition == queueIndex){
                        queueIndex = newPosition;
                    }else if (newPosition == queueIndex){
                        queueIndex = oldPosition;
                    }

                    break;

                case "equalizer_on_off":

                    boolean onOffState = extras.getBoolean("on_off_state");
                    mEqualizer.setEnabled(onOffState);

                    break;

                case "equalizer_change_preset":

                    short presetNumber = extras.getShort("preset_number");

                    mEqualizer.usePreset(presetNumber);

                    Intent intent = new Intent();
                    for (short i = 0; i < bandsFrequencies.size(); i++){
                        intent.putExtra("band_" + i + "_level", mEqualizer.getBandLevel(i));
                    }
                    intent.setAction("equalizer_preset_changed");
                    sendBroadcast(intent);

                    break;

                case "equalizer_change_band_level":

                    short bandNumber = extras.getShort("band_number");
                    short bandLevel = extras.getShort("band_level");

                    mEqualizer.setBandLevel(bandNumber, bandLevel);

                    break;

            }
        }

    };


    /*
    Override methods from Media Browser Service to access Media Space from client
    I built another class to have playlist control
    so these two methods is useless
     */
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("media_space", null);
    }


    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // Initialize queue and notification manager
        queue = new ArrayList<>();
        mAnglerNotificationManager = new AnglerNotificationManager(this);

        return super.onStartCommand(intent, flags, startId);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            if (queue.size() > 0) {
                anglerServiceCallback.onSkipToNext();
            }
        }
    };


    private MediaMetadataCompat getCurrentMetadata(){

        MediaSessionCompat.QueueItem queueItem = queue.get(queueIndex);
        MediaDescriptionCompat description = queueItem.getDescription();

        return MediaAssistant.extractMetadata(description);
    }


    private void setupEqualizer(){

        SharedPreferences sharedPreferences = getSharedPreferences("equalizer_pref", MODE_PRIVATE);

        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());

        // get equalizer variables and attach them to media session
        short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
        short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];


        bandsFrequencies = new ArrayList<>();

        for (short i = 0; i < mEqualizer.getNumberOfBands(); i ++){
            bandsFrequencies.add(mEqualizer.getCenterFreq(i));
        }


        equalizerPresets = new ArrayList<>();

        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++){
            equalizerPresets.add(mEqualizer.getPresetName(i));
        }


        Bundle bundle = new Bundle();
        bundle.putShort("lower_equalizer_band_level", lowerEqualizerBandLevel);
        bundle.putShort("upper_equalizer_band_level", upperEqualizerBandLevel);
        bundle.putIntegerArrayList("bands_frequencies", bandsFrequencies);
        bundle.putStringArrayList("equalizer_presets", equalizerPresets);

        mMediaSession.setExtras(bundle);


        // configure equalizer from shared preferences
        boolean onOffState = sharedPreferences.getBoolean("on_off_state", false);
        mEqualizer.setEnabled(onOffState);

        if (onOffState){

            short presetNumber = (short) sharedPreferences.getInt("active_preset", 0);

            if (presetNumber != 0){

                Bundle extras = new Bundle();
                extras.putShort("preset_number", (short)(presetNumber - 1));

                mMediaSession.getController().getTransportControls().sendCustomAction("equalizer_change_preset", extras);
            }else{

                for (short i = 0; i < bandsFrequencies.size(); i++){

                    short bandLevel = (short)sharedPreferences.getInt("band_" + i + "_level", 0);

                    Bundle extras = new Bundle();
                    extras.putShort("band_" + i + "_level", bandLevel);

                    mMediaSession.getController().getTransportControls().sendCustomAction("equalizer_change_band_level", extras);
                }
            }
        }


    }
}