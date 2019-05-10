package com.mnemo.angler.player.notification;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

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

    private String channelId = "angler_service_channel";

    private AnglerService anglerService;
    private MediaControllerCompat mediaController;

    private MediaSessionCompat.Token token;

    public AnglerNotificationManager(AnglerService anglerService) {

        this.anglerService = anglerService;

        /*
        Connecting to Angler service via Media Session Token.
        Creating Media Controller and get Transport Controls to control Media Session through Media Session Callbacks
         */
        token = anglerService.getSessionToken();
        try {
            mediaController = new MediaControllerCompat(anglerService, token);
        }catch (RemoteException e){
            e.printStackTrace();
        }


        // Creating new Notification Channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

    }


    // Creating new Notification
    public void createNotification(){


        //Get metadata via Media Controller
        MediaMetadataCompat metadata = mediaController.getMetadata();

        if (metadata != null) {
            String title = String.valueOf(metadata.getDescription().getTitle());
            String artist = String.valueOf(metadata.getDescription().getSubtitle());
            String album = String.valueOf(metadata.getDescription().getDescription());

            String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

            Bitmap albumImage;

            if (new File(albumImagePath).exists()) {
                albumImage = BitmapFactory.decodeFile(albumImagePath);
            } else {
                albumImage = BitmapFactory.decodeResource(anglerService.getResources(), R.drawable.album_default);
            }


            // Creating Content Intent that launch Angler Client on Notification click
            Intent contentIntent = new Intent(anglerService, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("meta", metadata);
            contentIntent.putExtras(bundle);

            // Configure Notification Builder
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(anglerService, channelId)
                    .setSmallIcon((mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) ?
                            R.drawable.play_white : R.drawable.pause_white)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentTitle(title)
                    .setContentText(artist)
                    .setSubText(album)
                    .setLargeIcon(albumImage)
                    .addAction(new NotificationCompat.Action(R.drawable.fast_back, "previous",
                            PendingIntent.getBroadcast(anglerService, 0, new Intent("action_previous"), 0)))
                    .addAction((mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) ?
                            new NotificationCompat.Action(R.drawable.pause_white, "pause",
                                    PendingIntent.getBroadcast(anglerService, 0, new Intent("action_pause"), 0)) :
                            new NotificationCompat.Action(R.drawable.play_white, "play",
                                    PendingIntent.getBroadcast(anglerService, 0, new Intent("action_play"), 0)))
                    .addAction(new NotificationCompat.Action(R.drawable.fast_forward, "next",
                            PendingIntent.getBroadcast(anglerService, 0, new Intent("action_next"), 0)))
                    .setShowWhen(false)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(token)
                            .setShowActionsInCompactView(0, 1, 2))
                    .setContentIntent(PendingIntent.getActivity(anglerService, 1, contentIntent, 0))
                    .setDeleteIntent(PendingIntent.getBroadcast(anglerService, 0, new Intent("action_stop"), PendingIntent.FLAG_CANCEL_CURRENT))
                    .setOngoing(false);


            // Bind Notification with Angler Service
            anglerService.startForeground(191, mBuilder.build());
        }
    }


    /*
    Creating Notification Channel
    Necessary to API >=26
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
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
