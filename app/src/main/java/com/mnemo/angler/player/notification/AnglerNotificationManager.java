package com.mnemo.angler.player.notification;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.mnemo.angler.R;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.player.service.AnglerService;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AnglerNotificationManager {

    private static final String ACTION_PLAY = "action_play";
    private static final String ACTION_PAUSE = "action_pause";
    private static final String ACTION_STOP = "action_stop";
    private static final String ACTION_NEXT = "action_next";
    private static final String ACTION_PREV = "action_prev";

    private String channelId = "angler_service_channel";

    private AnglerService anglerService;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.TransportControls transportControls;
    private BroadcastReceiver noiseReceiver;
    private IntentFilter intentFilter;

    MediaSessionCompat.Token token;

    //private RemoteViews notificationView;

    public AnglerNotificationManager(AnglerService anglerService) {

        this.anglerService = anglerService;

        /*
        Connecting to Angler service via Media Session Token.
        Creating Media Controller and get Transport Controls to control Media Session through Media Session Callbacks
         */
        token = anglerService.getSessionToken();
        try {
            mediaController = new MediaControllerCompat(anglerService, token);
            transportControls = mediaController.getTransportControls();
        }catch (RemoteException e){
            e.printStackTrace();
        }

        /*
        Creating Broadcast Receiver to manage intents, getting from Notification and run corresponding Transport Controls action
         */
        noiseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                switch (action){
                    case ACTION_PLAY:

                        if (mediaController.getPlaybackState().getState() != PlaybackStateCompat.STATE_ERROR) {
                            transportControls.play();
                        }
                        break;
                    case ACTION_PAUSE:
                        transportControls.pause();
                        break;
                    case ACTION_STOP:
                        context.unregisterReceiver(noiseReceiver);
                        transportControls.stop();
                        break;
                    case ACTION_NEXT:
                        transportControls.skipToNext();
                        break;
                    case ACTION_PREV:
                        transportControls.skipToPrevious();
                        break;
                }
            }
        };

        /*
        Configure Intent Filter for Broadcast Receiver
        It describes what intents Broadcast Receiver can receive
         */
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_PAUSE);
        intentFilter.addAction(ACTION_STOP);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREV);

        /*
        Registering Broadcast Receiver with Intent Filter
         */
        anglerService.registerReceiver(noiseReceiver, intentFilter);


        /*
        Creating new Notification Channel
         */
        createNotificationChannel();

        /*
        Get Remote View for Notification
        It allows use custom layouts for Notification instead default ones
         */
        //notificationView = new RemoteViews(anglerService.getPackageName(), R.layout.notification_layout);

    }

    /*
    Creating new Notification
     */
    public void createNotification(){

        /*
        Get metadata via Media Controller
         */
        MediaMetadataCompat metadata = mediaController.getMetadata();
        String title = String.valueOf(metadata.getDescription().getTitle());
        String artist = String.valueOf(metadata.getDescription().getSubtitle());
        String album = String.valueOf(metadata.getDescription().getDescription());

        String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";
        Bitmap albumImage = BitmapFactory.decodeFile(albumImagePath);


        /*
        Creating Content Intent that launch Angler Client on Notification click
         */
        Intent contentIntent = new Intent(anglerService, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("meta", metadata);
        contentIntent.putExtras(bundle);

        // Configure Notification Builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(anglerService, channelId)
                .setSmallIcon((mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) ?
                        R.drawable.baseline_play_arrow_white_36dp_boarderless : R.drawable.baseline_pause_white_36dp_boarderless)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(album)
                .setLargeIcon(albumImage)
                .addAction(new NotificationCompat.Action(R.drawable.baseline_fast_rewind_white_36dp_left_cut, "previous",
                        PendingIntent.getBroadcast(anglerService, 0, new Intent(ACTION_PREV), 0)))
                .addAction((mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) ?
                        new NotificationCompat.Action(R.drawable.baseline_pause_white_36dp, "pause",
                        PendingIntent.getBroadcast(anglerService, 0, new Intent(ACTION_PAUSE), 0)) :
                        new NotificationCompat.Action(R.drawable.baseline_play_arrow_white_36dp, "play",
                        PendingIntent.getBroadcast(anglerService, 0, new Intent(ACTION_PLAY), 0)))
                .addAction(new NotificationCompat.Action(R.drawable.baseline_fast_forward_white_36dp, "next",
                        PendingIntent.getBroadcast(anglerService, 0, new Intent(ACTION_NEXT), 0)))
                .setShowWhen(false)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0,1,2))
                .setContentIntent(PendingIntent.getActivity(anglerService,1, contentIntent,0))
                .setDeleteIntent(PendingIntent.getBroadcast(anglerService,0,new Intent(ACTION_STOP),PendingIntent.FLAG_CANCEL_CURRENT))
                .setOngoing(false);


        // Bind Notification with Angler Service
        anglerService.startForeground(191, mBuilder.build());
    }


    /*
    Creating Notification Channel
    Necessary to API >=26
     */
    private void createNotificationChannel(){

        NotificationManager mNotificationManager = (NotificationManager) anglerService.getSystemService(NOTIFICATION_SERVICE);
        CharSequence name = "angler_service";
        String description = "some media lives here";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mNotificationChannel = new NotificationChannel(channelId, name, importance);
        mNotificationChannel.setDescription(description);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
    }


}
