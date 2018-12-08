package com.mnemo.angler.player.service;



import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
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
import android.util.Log;


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.player.AnglerNotificationManager;
import com.mnemo.angler.util.MediaAssistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AnglerService extends MediaBrowserServiceCompat implements AnglerServiceView{

    AnglerServicePresenter presenter;

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;

    private boolean isPaused = false;

    private Equalizer mEqualizer;
    private Virtualizer mVirtualizer;
    private BassBoost mBassBoost;
    private LoudnessEnhancer mAmplifier;

    private ArrayList<Integer> bandsFrequencies;
    private ArrayList<String> equalizerPresets;


    private AnglerNotificationManager mAnglerNotificationManager;

    private String queueTitle;
    private ArrayList<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
    private int queueIndex = -1;

    int seekbarPosition = 0;


    public void onCreate() {

        super.onCreate();

        // Bind Presenter to View
        presenter = new AnglerServicePresenter();
        presenter.attachView(this);

        // Setup media session on service, set playback state and callbacks
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


        // Set session token to connect to service from clients
        setSessionToken(mMediaSession.getSessionToken());



        // Run another thread, that watch on seek bar position from service
        Handler seekHandler = new Handler();
        seekHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null){

                    int newSeekbarPosition = mMediaPlayer.getCurrentPosition();
                    Log.e("ffffffff", "pos + " + seekbarPosition + "       " + newSeekbarPosition);
                    if (newSeekbarPosition / 1000 != seekbarPosition / 1000){

                        seekbarPosition = newSeekbarPosition;

                        Intent intent = new Intent();
                        intent.setAction("seekbar_progress_changed");
                        intent.putExtra("seekbar_progress", seekbarPosition);
                        sendBroadcast(intent);
                    }
                }
                seekHandler.postDelayed(this,100);
            }
        });


        mMediaPlayer = new MediaPlayer();

        // Setup equalizer and audio effects
        setupEqualizer();
        setupAudioEffects();

        // Initialize queue
        initializeQueue();

        // Initialize notification manager
        mAnglerNotificationManager = new AnglerNotificationManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        presenter.saveQueueTitle(queueTitle);
        presenter.saveQueue(simplifyQueue());
        presenter.saveQueueIndex(queueIndex);
        presenter.saveSeekbarPosition(seekbarPosition);

        presenter.deattachView();
    }


    // MVP View methods
    @Override
    public void setQueue(List<Track> tracks) {

        for (MediaDescriptionCompat description : MediaAssistant.mergeMediaDescriptionArray("library", tracks)) {

            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, description.hashCode());
            queue.add(queueItem);
        }

        mMediaSession.setQueue(queue);
    }



    // Initialize media session callbacks (play, pause, stop, etc)
    MediaSessionCompat.Callback anglerServiceCallback = new MediaSessionCompat.Callback() {

        MediaMetadataCompat metadata;

        // Queue methods
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




        // Playback control methods
        @Override
        public void onPrepare() {

            if (queueIndex < 0 || queue.isEmpty()) {
                return;
            }

            metadata = getCurrentMetadata();
            mMediaSession.setMetadata(metadata);

            if (!mMediaSession.isActive()) {
                mMediaSession.setActive(true);
            }
        }


        @Override
        public void onPlay() {

            startService(new Intent(getApplicationContext(), AnglerService.class));

            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                    .setActiveQueueItemId((long)queueIndex)
                    .build());

            if (isPaused){

                isPaused = false;
                mAnglerNotificationManager.createNotification();
                mMediaPlayer.start();

            }else {

                onPrepare();

                try {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer.reset();
                    mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                        if (queue.size() > 0) {
                            anglerServiceCallback.onSkipToNext();
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
                        mAnglerNotificationManager.createNotification();
                        mMediaPlayer.start();
                        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
                        mVirtualizer = new Virtualizer(0, mMediaPlayer.getAudioSessionId());
                        mBassBoost = new BassBoost(0, mMediaPlayer.getAudioSessionId());
                        mAmplifier = new LoudnessEnhancer(mMediaPlayer.getAudioSessionId());

                        Intent intent = new Intent();

                        intent.setAction("queue_position_changed");
                        intent.putExtra("queue_position", queueIndex);

                        sendBroadcast(intent);

                    });
                    mMediaPlayer.setDataSource(AnglerService.this, metadata.getDescription().getMediaUri());
                    mMediaPlayer.prepareAsync();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }



        @Override
        public void onPause() {
            super.onPause();

            mMediaPlayer.pause();
            isPaused = true;

            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED,PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,0)
                    .setActiveQueueItemId((long)queueIndex)
                    .build());

            mAnglerNotificationManager.createNotification();
            stopForeground(false);
        }

        @Override
        public void onStop() {
            super.onStop();

            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                .build());

            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaPlayer.stop();
            stopSelf();
        }




        // Skip methods
        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            if (queueIndex == 0){
                queueIndex = queue.size() - 1;
            }else{
                queueIndex--;
            }
            isPaused = false;
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

            isPaused = false;
            onPlay();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            queueIndex = (int)id;

            isPaused = false;
            onPlay();
        }




        // Repeat / Shuffle
        @Override
        public void onSetRepeatMode(int repeatMode) {

            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                mMediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                mMediaPlayer.setLooping(true);
            }else{
                mMediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                mMediaPlayer.setLooping(false);
            }
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {

            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL){
                mMediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
            }else{
                mMediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            }
        }



        // SeekBar method
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if (mMediaPlayer != null) {
                Log.e("fffffffffff", "g " + pos);
                mMediaPlayer.seekTo((int) (pos * mMediaPlayer.getDuration() / 100));
            }
        }



        // Custom actions
        @Override
        public void onCustomAction(@NonNull String action, @Nullable Bundle extras) {
            super.onCustomAction(action, extras);

            switch(action){

                case "set_queue_title":

                    queueTitle = extras.getString("queue_title");

                    mMediaSession.setQueueTitle(queueTitle);

                    break;

                case "clear_queue":

                    if (queue != null){
                        queue.clear();

                        mMediaSession.setQueue(queue);
                    }

                    break;

                case "update_queue":

                    mMediaSession.setQueue(queue);

                    if (mMediaSession.getController().getMetadata() == null){
                        onPrepare();
                    }

                    break;


                case "update_queue_position":

                    queueIndex = extras.getInt("queue_position");

                    if (isPaused){
                        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                                .setActiveQueueItemId((long) queueIndex)
                                .build());
                    }else {
                        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0)
                                .setActiveQueueItemId((long) queueIndex)
                                .build());
                    }

                    break;

                case "remove_queue_item":

                    int index = extras.getInt("position");

                    if (index <= queueIndex){
                        queueIndex--;
                    }

                    queue.remove(index);
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

                    mMediaSession.setQueue(queue);

                    break;

                case "equalizer_on_off":

                    boolean equalizerOnOffState = extras.getBoolean("on_off_state");
                    mEqualizer.setEnabled(equalizerOnOffState);

                    break;

                case "equalizer_change_preset":

                    short presetNumber = extras.getShort("preset_number");

                    mEqualizer.usePreset(presetNumber);

                    Intent intent = new Intent();

                    ArrayList<Integer> bandsLevel = new ArrayList<>();

                    for (short i = 0; i < bandsFrequencies.size(); i++){
                        bandsLevel.add((int)mEqualizer.getBandLevel((i)));
                    }

                    intent.putIntegerArrayListExtra("bands_level", bandsLevel);

                    intent.setAction("equalizer_preset_changed");
                    sendBroadcast(intent);

                    break;

                case "equalizer_change_band_level":

                    short bandNumber = extras.getShort("band_number");
                    short bandLevel = extras.getShort("band_level");

                    mEqualizer.setBandLevel(bandNumber, bandLevel);

                    break;


                case "virtualizer_on_off":

                    boolean virtualizerOnOffState = extras.getBoolean("on_off_state");
                    mVirtualizer.setEnabled(virtualizerOnOffState);

                    break;

                case "virtualizer_change_band_level":

                    short virtualizerLevel = extras.getShort("virtualizer_band_level");
                    mVirtualizer.setStrength(virtualizerLevel);

                    break;

                case "bass_boost_on_off":

                    boolean bassBoostOnOffState = extras.getBoolean("on_off_state");
                    mVirtualizer.setEnabled(bassBoostOnOffState);

                    break;

                case "bass_boost_change_band_level":

                    short bassBoostLevel = extras.getShort("bass_boost_band_level");
                    mBassBoost.setStrength(bassBoostLevel);

                    break;

                case "amplifier_on_off":

                    boolean amplifierOnOffState = extras.getBoolean("on_off_state");
                    mAmplifier.setEnabled(amplifierOnOffState);

                    break;

                case "amplifier_change_band_level":

                    short amplifierLevel = extras.getShort("amplifier_band_level");
                    mAmplifier.setTargetGain(amplifierLevel);

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
        result.sendResult(new ArrayList<>());
    }


    private MediaMetadataCompat getCurrentMetadata(){

        MediaSessionCompat.QueueItem queueItem = queue.get(queueIndex);
        MediaDescriptionCompat description = queueItem.getDescription();

        return MediaAssistant.extractMetadata(description);
    }


    // Equalizer
    private void setupEqualizer(){

        mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());

        // Get equalizer variables and attach them to media session
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


        // Configure equalizer
        boolean onOffState = presenter.getEqualizerState();
        mEqualizer.setEnabled(onOffState);

        if (onOffState){

            short presetNumber = (short)presenter.getEqualizerPreset();

            if (presetNumber != 0){

                Bundle extras = new Bundle();
                extras.putShort("preset_number", (short)(presetNumber - 1));

                mMediaSession.getController().getTransportControls().sendCustomAction("equalizer_change_preset", extras);
            }else{

                // Get bands level
                List<Short> bandsLevel = presenter.getBandsLevel(bandsFrequencies.size());

                for (short i = 0; i < bandsFrequencies.size(); i++){

                    Bundle extras = new Bundle();
                    extras.putShort("band_" + i + "_level", bandsLevel.get(i));

                    mMediaSession.getController().getTransportControls().sendCustomAction("equalizer_change_band_level", extras);
                }
            }
        }

    }

    // Audio effects
    private void setupAudioEffects(){

        mVirtualizer = new Virtualizer(0, mMediaPlayer.getAudioSessionId());
        mBassBoost = new BassBoost(0, mMediaPlayer.getAudioSessionId());
        mAmplifier = new LoudnessEnhancer(mMediaPlayer.getAudioSessionId());

        // Virtualizer
        boolean virtualizerOnOffState = presenter.getVirtualizerState();
        mVirtualizer.setEnabled(virtualizerOnOffState);

        short virtualizerStrength = (short)presenter.getVirtualizerStrength();
        mVirtualizer.setStrength(virtualizerStrength);


        // Bass boost
        boolean bassBoostOnOffState = presenter.getBassBoostState();
        mBassBoost.setEnabled(bassBoostOnOffState);

        short bassBoostStrength = (short)presenter.getBassBoostStrength();
        mBassBoost.setStrength(bassBoostStrength);


        // Amplifier
        boolean amplifierOnOffState = presenter.getAmplifierState();
        mAmplifier.setEnabled(amplifierOnOffState);

        short amplifierGain = (short)presenter.getAmplifierGain();
        mAmplifier.setTargetGain(amplifierGain);

    }



    // Queue
    private void initializeQueue(){

        queueTitle = presenter.getQueueTitle();

        if (queueTitle == null){

            // Initialize queue from library
            queueTitle = "Library";
            mMediaSession.setQueueTitle(queueTitle);

            presenter.loadLibraryTracks();

            queueIndex = 0;

            if (mMediaPlayer != null){
                mMediaSession.getController().getTransportControls().prepare();
            }

        }else{

            mMediaSession.setQueueTitle(queueTitle);

            setQueue(presenter.getQueue());

            queueIndex = presenter.getQueueIndex();

            if (mMediaPlayer != null){
                mMediaSession.getController().getTransportControls().prepare();
            }

            seekbarPosition = presenter.getSeekbarPosition();

            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(seekbarPosition * mMediaPlayer.getDuration() / 100);
            }
        }
    }


    // Queue support methods
    public void setQueue(Set<String> simplifiedQueue){

        for (int i = 0; i < simplifiedQueue.size(); i++){
            queue.add(null);
        }

        for (String s : simplifiedQueue){
            restoreQueueItem(s);
        }

        mMediaSession.setQueue(queue);
    }


    private HashSet<String> simplifyQueue(){

        HashSet<String> set = new HashSet<>();

        for (int i = 0; i < queue.size(); i++){
            set.add(simplifyQueueItem(queue.get(i), i));
        }

        return set;
    }


    private String simplifyQueueItem(MediaSessionCompat.QueueItem queueItem, int position){
        return position
                + ":::" + queueItem.getDescription().getMediaId()
                + ":::" + queueItem.getDescription().getTitle()
                + ":::" + queueItem.getDescription().getSubtitle()
                + ":::" + queueItem.getDescription().getExtras().getString("album")
                + ":::" + queueItem.getDescription().getExtras().getLong("duration")
                + ":::" + queueItem.getDescription().getMediaUri().toString()
                + ":::" + queueItem.getDescription().getExtras().getString("track_playlist");
    }

    private void restoreQueueItem(String simplifiedQueueItem){

        String[] queueItemArray = simplifiedQueueItem.split(":::");

        MediaDescriptionCompat description = MediaAssistant.mergeMediaDescription(queueItemArray[1], queueItemArray[2],
                queueItemArray[3], queueItemArray[4], Long.parseLong(queueItemArray[5]), queueItemArray[6], queueItemArray[7]);

        queue.set(Integer.parseInt(queueItemArray[0]), new MediaSessionCompat.QueueItem(description, description.hashCode()));
    }

}