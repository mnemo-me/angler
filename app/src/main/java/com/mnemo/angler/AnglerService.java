package com.mnemo.angler;



import android.content.Intent;
import android.media.MediaPlayer;
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

import java.util.ArrayList;
import java.util.List;


public class AnglerService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mMediaSession;
    private MediaPlayer mMediaPlayer;
    //public static Equalizer mEqualizer;

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
        //mEqualizer = new Equalizer(0, mMediaPlayer.getAudioSessionId());

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

            if (mMediaPlayer != null){
                mMediaPlayer.release();
            }

            mMediaPlayer = MediaPlayer.create(AnglerService.this, metadata.getDescription().getMediaUri());
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(onCompletionListener);

            mAnglerNotificationManager.createNotification();

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


        // Initialize qu_queue and notification manager
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

}