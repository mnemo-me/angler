package com.mnemo.angler.player.service;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.Virtualizer;
import android.os.Build;
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


import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.player.notification.AnglerNotificationManager;
import com.mnemo.angler.util.MediaAssistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AnglerService extends MediaBrowserServiceCompat implements AnglerServiceView{


    private AnglerServicePresenter presenter;

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;

    private MediaMetadataCompat metadata;

    private boolean isFirstTrack = true;
    private boolean isPaused = false;

    private AudioManager mAudioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private boolean isPauseBeforeAudioFocusLoss = true;

    private Equalizer mEqualizer;
    private boolean equalizerOnOffState;

    private Virtualizer mVirtualizer;
    private boolean virtualizerOnOffState;
    private short virtualizerStrength;

    private BassBoost mBassBoost;
    private boolean bassBoostOnOffState;
    private short bassBoostStrength;

    private LoudnessEnhancer mAmplifier;
    private boolean amplifierOnOffState;
    private short amplifierGain;

    private ArrayList<Integer> bandsFrequencies;


    private AnglerNotificationManager mAnglerNotificationManager;

    private String queueTitle;
    private ArrayList<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
    private int queueIndex = -1;

    private int seekbarPosition = 0;

    private BroadcastReceiver receiver;


    public void onCreate() {

        super.onCreate();

        // Bind Presenter to View
        presenter = new AnglerServicePresenter();
        presenter.attachView(this);

        // Setup media session on service, set playback state and callbacks
        mMediaSession = new MediaSessionCompat(this, "Angler Service");
        mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PAUSED));
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

                    if (mMediaPlayer.isPlaying()) {

                        int newSeekbarPosition = mMediaPlayer.getCurrentPosition();

                        if (newSeekbarPosition / 1000 != seekbarPosition / 1000) {

                            seekbarPosition = newSeekbarPosition;

                            Intent intent = new Intent();
                            intent.setAction("seekbar_progress_changed");
                            intent.putExtra("seekbar_progress", seekbarPosition);
                            sendBroadcast(intent);
                        }
                    }
                }
                seekHandler.postDelayed(this,100);
            }
        });


        mMediaPlayer = new MediaPlayer();

        // Manage audio focus
        setupAudioFocus();

        // Setup playback state
        setupPlaybackState();

        // Setup equalizer and audio effects
        setupEqualizer();
        setupAudioEffects();

        // Initialize queue
        initializeQueue();
        presenter.refreshQueue();

        // Initialize notification manager
        mAnglerNotificationManager = new AnglerNotificationManager(this);

        // Register broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){

                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:

                        mMediaSession.getController().getTransportControls().pause();

                        break;

                    case "action_play":

                        if (mMediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_ERROR) {
                            mMediaSession.getController().getTransportControls().play();
                        }
                        break;

                    case "action_pause":

                        mMediaSession.getController().getTransportControls().pause();

                        break;

                    case "action_stop":

                        mMediaSession.getController().getTransportControls().stop();

                        break;

                    case "action_next":

                        mMediaSession.getController().getTransportControls().skipToNext();

                        break;

                    case "action_previous":

                        mMediaSession.getController().getTransportControls().skipToPrevious();

                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction("action_play");
        intentFilter.addAction("action_pause");
        intentFilter.addAction("action_stop");
        intentFilter.addAction("action_next");
        intentFilter.addAction("action_previous");


        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        presenter.saveQueueTitle(queueTitle);
        presenter.saveQueue(simplifyQueue());
        presenter.saveQueueIndex(queueIndex);
        presenter.saveCurrentTrack(simplifyCurrentlyPlaylingTrack());
        presenter.saveSeekbarPosition(seekbarPosition);
        presenter.saveRepeatMode(mMediaSession.getController().getRepeatMode());
        presenter.saveShuffleMode(mMediaSession.getController().getShuffleMode());

        unregisterReceiver(receiver);

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

    @Override
    public void updateQueue(List<Track> tracks) {

        for (MediaSessionCompat.QueueItem queueItem : queue){

            boolean isTrackExist = false;

            for (Track track : tracks){

                if (track.get_id().equals(queueItem.getDescription().getMediaId())){

                    isTrackExist = true;

                    if (!track.getUri().equals(queueItem.getDescription().getMediaUri())){

                        queue.set(queue.indexOf(queueItem), MediaAssistant.changeQueueItemUri(queueItem, track.getUri()));
                    }

                    break;
                }
            }

            if (!isTrackExist) {

                Bundle bundle = new Bundle();
                bundle.putInt("position", queue.indexOf(queueItem));

                mMediaSession.getController().getTransportControls().sendCustomAction("remove_queue_item", bundle);
            }
        }
    }

    // Initialize media session callbacks (play, pause, stop, etc)
    private MediaSessionCompat.Callback anglerServiceCallback = new MediaSessionCompat.Callback() {


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

            if (queueIndex < 0 || queue.isEmpty() || isFirstTrack) {
                return;
            }

            metadata = getCurrentMetadata();
            mMediaSession.setMetadata(metadata);

            if (!mMediaSession.isActive()) {
                mMediaSession.setActive(true);
            }

            initializeMedia(true);
        }


        @Override
        public void onPlay() {

            int result;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                result = mAudioManager.requestAudioFocus(audioFocusRequest);
            }else{
                result = mAudioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){

                startService(new Intent(getApplicationContext(), AnglerService.class));

                mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING));

                if (isPaused){

                    isPaused = false;
                    mAnglerNotificationManager.createNotification();
                    mMediaPlayer.start();

                }else {

                    onPrepare();

                    initializeMediaPlayer();

                    try {
                        mMediaPlayer.prepareAsync();
                    }catch (IllegalStateException e){
                        mMediaPlayer.pause();
                    }
                }
            }

        }



        @Override
        public void onPause() {
            super.onPause();

            mMediaPlayer.pause();
            isPaused = true;

            mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PAUSED));

            mAnglerNotificationManager.createNotification();
            stopForeground(false);
        }

        @Override
        public void onStop() {
            super.onStop();

            if (mMediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_ERROR) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mAudioManager.abandonAudioFocusRequest(audioFocusRequest);
                }else{
                    mAudioManager.abandonAudioFocus(audioFocusChangeListener);
                }

                mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_STOPPED));

                mMediaSession.setActive(false);
                mMediaSession.release();
                mMediaPlayer.stop();
                stopSelf();
            }else {
                stopSelf();
            }
        }




        // Skip methods
        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            if (seekbarPosition < 5000) {

                if (mMediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_ERROR && !queue.isEmpty()) {

                    if (mMediaSession.getController().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {

                        queueIndex = (int) (Math.random() * queue.size());

                    } else {

                        if (queueIndex == 0) {
                            queueIndex = queue.size() - 1;
                        } else {
                            queueIndex--;
                        }
                    }

                    if (isFirstTrack){
                        isFirstTrack = false;
                    }

                    isPaused = false;
                    onPlay();
                }

            }else{

                seekbarPosition = 0;
                mMediaPlayer.seekTo(seekbarPosition);

                Intent intent = new Intent();
                intent.setAction("seekbar_progress_changed");
                intent.putExtra("seekbar_progress", seekbarPosition);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();

            if (mMediaSession.getController().getPlaybackState().getState() != PlaybackStateCompat.STATE_ERROR && !queue.isEmpty()) {

                if (mMediaSession.getController().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {

                    queueIndex = (int) (Math.random() * queue.size());

                } else {

                    if (queueIndex == queue.size() - 1) {
                        queueIndex = 0;
                    } else {
                        queueIndex++;
                    }
                }

                if (isFirstTrack){
                    isFirstTrack = false;
                }

                isPaused = false;
                onPlay();
            }
        }

        @Override
        public void onSkipToQueueItem(long id) {
            queueIndex = (int)id;

            isPaused = false;

            if (isFirstTrack){
                isFirstTrack = false;
            }

            onPlay();
        }




        // Repeat / Shuffle
        @Override
        public void onSetRepeatMode(int repeatMode) {

            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                mMediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                mMediaPlayer.setLooping(true);
            }else{
                mMediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                mMediaPlayer.setLooping(false);
            }
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {

            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL){
                mMediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            }else{
                mMediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
            }
        }



        // SeekBar method
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if (mMediaPlayer != null) {

                seekbarPosition = (int) (pos * mMediaPlayer.getDuration() / 100);
                mMediaPlayer.seekTo(seekbarPosition);
            }

            if (isFirstTrack){

                seekbarPosition = (int) (pos * Integer.parseInt(presenter.getCurrentTrack().split(":::")[4]) / 100);

            }

            mMediaSession.getController().getTransportControls().sendCustomAction("get_track_progress", null);
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
                        mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PAUSED));
                    }else {
                        mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING));
                    }

                    break;

                case "remove_queue_item":

                    int index = extras.getInt("position");

                    if (index <= queueIndex){
                        queueIndex--;

                        if (isPaused){
                            mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PAUSED));
                        }else {
                            mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_PLAYING));
                        }
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

                case "get_track_progress":

                    Intent trackProgressIntent = new Intent();
                    trackProgressIntent.setAction("seekbar_progress_changed");
                    trackProgressIntent.putExtra("seekbar_progress", seekbarPosition);
                    sendBroadcast(trackProgressIntent);

                    break;

                case "equalizer_on_off":

                    equalizerOnOffState = extras.getBoolean("on_off_state");
                    mEqualizer.setEnabled(equalizerOnOffState);

                    if (equalizerOnOffState){

                        mVirtualizer.setEnabled(virtualizerOnOffState);
                        mVirtualizer.setStrength(virtualizerStrength);

                        mBassBoost.setEnabled(bassBoostOnOffState);
                        mBassBoost.setStrength(bassBoostStrength);

                        mAmplifier.setEnabled(amplifierOnOffState);
                        mAmplifier.setTargetGain(amplifierGain);

                    }else{

                        mVirtualizer.setEnabled(false);
                        mBassBoost.setEnabled(false);
                        mAmplifier.setEnabled(false);
                    }

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

                    virtualizerOnOffState = extras.getBoolean("on_off_state");
                    mVirtualizer.setEnabled(virtualizerOnOffState);

                    break;

                case "virtualizer_change_band_level":

                    virtualizerStrength = extras.getShort("virtualizer_band_level");
                    mVirtualizer.setStrength(virtualizerStrength);

                    break;

                case "bass_boost_on_off":

                    bassBoostOnOffState = extras.getBoolean("on_off_state");
                    mBassBoost.setEnabled(bassBoostOnOffState);

                    break;

                case "bass_boost_change_band_level":

                    bassBoostStrength = extras.getShort("bass_boost_band_level");
                    mBassBoost.setStrength(bassBoostStrength);

                    break;

                case "amplifier_on_off":

                    amplifierOnOffState = extras.getBoolean("on_off_state");
                    mAmplifier.setEnabled(amplifierOnOffState);

                    break;

                case "amplifier_change_band_level":

                    amplifierGain = extras.getShort("amplifier_band_level");
                    mAmplifier.setTargetGain(amplifierGain);

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

        try {
            MediaSessionCompat.QueueItem queueItem = queue.get(queueIndex);
            MediaDescriptionCompat description = queueItem.getDescription();

            return MediaAssistant.extractMetadata(description);

        }catch (IndexOutOfBoundsException e){

            // Replace broadcast intent "queue_error"
            return metadata;
        }
    }


    // Audio focus
    private void setupAudioFocus(){

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        audioFocusChangeListener = focusChange -> {

            switch (focusChange){

                case AudioManager.AUDIOFOCUS_GAIN:

                    mMediaPlayer.setVolume(1f, 1f);

                    if (isPaused & !isPauseBeforeAudioFocusLoss) {

                        mMediaSession.getController().getTransportControls().play();
                    }

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                    mMediaPlayer.setVolume(0.1f, 0.1f);

                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                    mMediaPlayer.setVolume(0.1f, 0.1f);

                    break;

                case AudioManager.AUDIOFOCUS_LOSS:

                    isPauseBeforeAudioFocusLoss = isPaused;
                    mMediaSession.getController().getTransportControls().pause();

                    break;
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build();
        }
    }

    // Playback
    private void setupPlaybackState() {

        // Current track
        String currentTrack = presenter.getCurrentTrack();

        if (currentTrack != null) {

            metadata = MediaAssistant.extractMetadata(currentTrack);
            mMediaSession.setMetadata(metadata);

            if (!mMediaSession.isActive()){
                mMediaSession.setActive(true);
            }

            initializeMedia(true);

        }else{

            initializeMedia(false);
        }


        // Seekbar
        seekbarPosition = presenter.getSeekbarPosition();


        // Repeat
        int repeatMode = presenter.getRepeatMode();

        if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
            mMediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
            mMediaPlayer.setLooping(true);
        } else {
            mMediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
            mMediaPlayer.setLooping(false);
        }


        // Shuffle
        int shuffleMode = presenter.getShuffleMode();

        if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL){
            mMediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
        }else{
            mMediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
        }
    }

    // Initialize media player
    private void initializeMediaPlayer(){

        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();

            if (mMediaSession.getController().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                mMediaPlayer.setLooping(true);
            }else{
                mMediaPlayer.setLooping(false);
            }

            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                if (queue.size() > 0) {
                    anglerServiceCallback.onSkipToNext();
                }
            });
            mMediaPlayer.setOnPreparedListener(mediaPlayer -> {
                mAnglerNotificationManager.createNotification();
                mMediaPlayer.start();

                if (isFirstTrack) {
                    mMediaPlayer.seekTo(seekbarPosition);
                    isFirstTrack = false;
                }

                mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());
                mVirtualizer = new Virtualizer(0, mMediaPlayer.getAudioSessionId());
                mBassBoost = new BassBoost(0, mMediaPlayer.getAudioSessionId());
                mAmplifier = new LoudnessEnhancer(mMediaPlayer.getAudioSessionId());

                Intent intent = new Intent();

                intent.setAction("queue_position_changed");
                intent.putExtra("queue_position", queueIndex);

                sendBroadcast(intent);

            });
            mMediaPlayer.setOnErrorListener((mp, what, extra) -> {

                if (queue.size() > 1){
                    mMediaSession.getController().getTransportControls().skipToNext();
                }else{

                    isPaused = true;

                    mp.stop();

                    mMediaSession.setPlaybackState(createPlaybackState(PlaybackStateCompat.STATE_ERROR));

                    mAnglerNotificationManager.createNotification();
                    stopForeground(false);

                    queue.clear();

                    mMediaSession.setQueueTitle("");
                    mMediaSession.setQueue(queue);
                }

                return true;
            });
            mMediaPlayer.setDataSource(AnglerService.this, metadata.getDescription().getMediaUri());


        } catch (IOException e) {
            e.printStackTrace();
        }

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


        ArrayList<String> equalizerPresets = new ArrayList<>();

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
        equalizerOnOffState = presenter.getEqualizerState();
        mEqualizer.setEnabled(equalizerOnOffState);

        if (equalizerOnOffState){

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
        virtualizerOnOffState = presenter.getVirtualizerState();

        if (equalizerOnOffState) {
            mVirtualizer.setEnabled(virtualizerOnOffState);
        }

        virtualizerStrength = (short)presenter.getVirtualizerStrength();
        mVirtualizer.setStrength(virtualizerStrength);


        // Bass boost
        bassBoostOnOffState = presenter.getBassBoostState();

        if (equalizerOnOffState) {
            mBassBoost.setEnabled(bassBoostOnOffState);
        }

        bassBoostStrength = (short)presenter.getBassBoostStrength();
        mBassBoost.setStrength(bassBoostStrength);


        // Amplifier
        amplifierOnOffState = presenter.getAmplifierState();

        if (equalizerOnOffState) {
            mAmplifier.setEnabled(amplifierOnOffState);
        }

        amplifierGain = (short)presenter.getAmplifierGain();
        mAmplifier.setTargetGain(amplifierGain);

    }



    // Queue
    private void initializeQueue(){

        queueTitle = presenter.getQueueTitle();

        if (queueTitle == null){

            // Initialize queue from library
            queueTitle = "Library";
            mMediaSession.setQueueTitle(queueTitle);

            initializeMedia(false);

            presenter.loadLibraryTracks();

        }else{

            mMediaSession.setQueueTitle(queueTitle);

            setQueue(presenter.getQueue());

            queueIndex = presenter.getQueueIndex();

        }
    }


    // Queue support methods
    private void setQueue(Set<String> simplifiedQueue){

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

    private String simplifyCurrentlyPlaylingTrack(){

        try {
            MediaMetadataCompat currentMetadata = mMediaSession.getController().getMetadata();

            Track currentTrack = MediaAssistant.combineMetadataInTrack(currentMetadata);

            return currentTrack.toString();

        }catch (NullPointerException e){
            return null;
        }
    }

    public void initializeMedia(boolean isMedia) {

        Intent intent = new Intent();
        intent.setAction("initialize_media");
        intent.putExtra("is_media", isMedia);
        sendBroadcast(intent);
    }

    // Playback state method
    private PlaybackStateCompat createPlaybackState(int state){

        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();

        builder.setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        builder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        builder.setActiveQueueItemId((long)queueIndex);

        return builder.build();
    }

}