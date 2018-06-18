package com.mnemo.angler;



import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;


public class AnglerService extends MediaBrowserService {

    private MediaSession mMediaSession;
    private MediaPlayer mMediaPlayer;
    //public static Equalizer mEqualizer;
    private PlaylistManager mPlaylistManager;
    private AnglerNotificationManager mAnglerNotificationManager;
    public static long seekProgress;
    public static boolean isSeekAvailable;
    private String activePlaylist;

    public static boolean isDBInitialized = false;


    public void onCreate() {

        super.onCreate();

        /*
        Setup media session on service, set playback state and callbacks
         */
        mMediaSession = new MediaSession(this, "Angler Service");
        mMediaSession.setPlaybackState(new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE).build());
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
    MediaSession.Callback anglerServiceCallback = new MediaSession.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
                onPlayFromMediaId(mPlaylistManager.getCurrentId(),null);
        }

        @Override
        public void onPause() {
            super.onPause();

            mMediaPlayer.pause();

            mMediaSession.setPlaybackState(new PlaybackState.Builder()
                .setState(PlaybackState.STATE_PAUSED,PlaybackState.PLAYBACK_POSITION_UNKNOWN,0).build());

            mAnglerNotificationManager.unregisterCallback();
            stopForeground(false);
        }

        @Override
        public void onStop() {
            super.onStop();

            mAnglerNotificationManager.unregisterCallback();
            mMediaSession.setActive(false);
            mMediaPlayer.stop();
            stopSelf();

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            mMediaSession.setActive(true);

            MediaMetadata metadata = mPlaylistManager.getCurrentMetadata();
            mMediaSession.setMetadata(metadata);

            mMediaSession.setPlaybackState(new PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING,PlaybackState.PLAYBACK_POSITION_UNKNOWN,0).build());


            Uri mediaUri = metadata.getDescription().getMediaUri();


            if (mMediaPlayer != null) {
                mMediaPlayer.release();
            }
            mMediaPlayer = MediaPlayer.create(AnglerService.this, mediaUri);
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(onCompletionListener);

            mAnglerNotificationManager.createNotification();

        }


        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            if (!PlaybackManager.shuffleState) {
                PlaylistManager.position--;
            }

            onPlayFromMediaId(mPlaylistManager.getCurrentId(), null);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();

            if (!PlaybackManager.shuffleState) {
                PlaylistManager.position++;
            }

            onPlayFromMediaId(mPlaylistManager.getCurrentId(), null);
        }

        @Override
        public void onCustomAction(@NonNull String action, @Nullable Bundle extras) {
            super.onCustomAction(action, extras);

            switch(action){
                case PlaybackManager.REPEAT:
                    if (mMediaPlayer != null) {
                        PlaybackManager.repeatState = !PlaybackManager.repeatState;
                    }
                    break;
                case PlaybackManager.SHUFFLE:
                    PlaybackManager.shuffleState = !PlaybackManager.shuffleState;
                    break;
                case PlaybackManager.RESUME:
                    mMediaSession.setActive(true);

                    if (mMediaPlayer == null){
                        PlaylistManager.position = 0;
                        onPlayFromMediaId(mPlaylistManager.getCurrentId(),null);
                    }

                    mMediaPlayer.start();
                    mMediaPlayer.setOnCompletionListener(onCompletionListener);

                    mMediaSession.setPlaybackState(new PlaybackState.Builder()
                            .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0).build());
                    break;
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo((int) (pos * mMediaPlayer.getDuration() / 100));
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
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {

        result.sendResult(new ArrayList<MediaBrowser.MediaItem>());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        activePlaylist = intent.getExtras().getString("active_playlist");

        /*
        Initialize playlist manager and notification manager
        Both are custom helper classes
         */
        mPlaylistManager = new PlaylistManager(getContentResolver(), activePlaylist);
        mAnglerNotificationManager = new AnglerNotificationManager(this);

        return super.onStartCommand(intent, flags, startId);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(!PlaybackManager.repeatState) {
                anglerServiceCallback.onSkipToNext();
            }else{
                anglerServiceCallback.onPlayFromMediaId(mPlaylistManager.getCurrentId(),null);
            }
        }
    };



}