package com.mnemo.angler.util;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;

public class MediaAssistant {

    public static ArrayList<MediaDescriptionCompat> mergeMediaDescriptionArray(String playlist, Cursor cursor){

        ArrayList<MediaDescriptionCompat> descriptions = new ArrayList<>();

        if (cursor.getCount() > 0 ) {

            cursor.moveToFirst();

            do {

                String mediaId = cursor.getString(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String album = cursor.getString(3);
                long duration = cursor.getLong(4);
                String uri = cursor.getString(5);


                descriptions.add(mergeMediaDescription(mediaId, title, artist, album, duration, uri, playlist));

            } while (cursor.moveToNext());

        }

        return descriptions;
    }

    public static MediaDescriptionCompat mergeMediaDescription(String mediaId, String title, String artist, String album, long duration, String uri, String playlist){

        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(mediaId);
        builder.setTitle(title);
        builder.setSubtitle(artist);

        Bundle bundle = new Bundle();
        bundle.putString("track_playlist", playlist);
        bundle.putString("album", album);
        bundle.putLong("duration", duration);
        builder.setExtras(bundle);

        builder.setMediaUri(Uri.parse(uri));

        return builder.build();
    }

    public static MediaMetadataCompat extractMetadata(MediaDescriptionCompat description){

        String mediaId = description.getMediaId();
        String title = description.getTitle().toString();
        String artist = description.getSubtitle().toString();

        Bundle bundle = description.getExtras();
        String trackPlaylist = bundle.getString("track_playlist");
        String album = bundle.getString("album");
        long duration = bundle.getLong("duration");

        String uri = description.getMediaUri().toString();

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString("track_playlist", trackPlaylist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,album)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,duration)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri);

        return metadataBuilder.build();
    }

    public static Bundle putMetadataInBundle(MediaMetadataCompat metadata){

        Bundle bundle = new Bundle();

        String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        String uri = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);


        bundle.putString("media_id", mediaId);
        bundle.putString("title", title);
        bundle.putString("artist", artist);
        bundle.putString("album", album);
        bundle.putLong("duration", duration);
        bundle.putString("uri", uri);

        return bundle;
    }

    public static Bundle putMetadataInBundle(String mediaId, String title, String artist, String album, long duration, String uri){

        Bundle bundle = new Bundle();

        bundle.putString("media_id", mediaId);
        bundle.putString("title", title);
        bundle.putString("artist", artist);
        bundle.putString("album", album);
        bundle.putLong("duration", duration);
        bundle.putString("uri", uri);

        return bundle;

    }


    // Static method converting time from milliseconds in human readable format
    public static String convertToTime(long durationMS) {

        long durationS = durationMS / 1000;

        int hours = (int) durationS / 3600;
        int minutes = ((int) durationS - hours * 3600) / 60;
        int seconds = (int) durationS - hours * 3600 - minutes * 60;

        if (hours == 0) {
            return String.format("%2d:%02d", minutes, seconds);
        } else {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
