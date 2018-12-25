package com.mnemo.angler.util;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.mnemo.angler.data.database.Entities.Track;

import java.util.ArrayList;
import java.util.List;

public class MediaAssistant {


    public static ArrayList<MediaDescriptionCompat> mergeMediaDescriptionArray(String playlist, List<Track> tracks){

        ArrayList<MediaDescriptionCompat> descriptions = new ArrayList<>();

        for (Track track : tracks) {

            String mediaId = track.get_id();
            String title = track.getTitle();
            String artist = track.getArtist();
            String album = track.getAlbum();
            long duration = track.getDuration();
            String uri = track.getUri();

            descriptions.add(mergeMediaDescription(mediaId, title, artist, album, duration, uri, playlist));
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


    public static MediaDescriptionCompat mergeMediaDescription(Track track, String playlist){

        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(track.get_id());
        builder.setTitle(track.getTitle());
        builder.setSubtitle(track.getArtist());

        Bundle bundle = new Bundle();
        bundle.putString("track_playlist", playlist);
        bundle.putString("album", track.getAlbum());
        bundle.putLong("duration", track.getDuration());
        builder.setExtras(bundle);

        builder.setMediaUri(Uri.parse(track.getUri()));

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

    public static MediaMetadataCompat extractMetadata(String track) {

        String[] trackMetadataArray = track.split(":::");

        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, trackMetadataArray[0])
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, trackMetadataArray[1])
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, trackMetadataArray[2])
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, trackMetadataArray[3])
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(trackMetadataArray[4]))
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, trackMetadataArray[5]);

        return metadataBuilder.build();
    }

    public static Track combineMetadataInTrack(MediaMetadataCompat metadata){

        String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        String uri = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);

        int year = (int)metadata.getLong(MediaMetadataCompat.METADATA_KEY_YEAR);


        return new Track(mediaId, title, artist, album, duration, uri, year, 0);

    }


    public static MediaSessionCompat.QueueItem changeQueueItemUri(MediaSessionCompat.QueueItem queueItem, String uri){

        String mediaId = queueItem.getDescription().getMediaId();
        String title = queueItem.getDescription().getTitle().toString();
        String artist = queueItem.getDescription().getSubtitle().toString();
        String alnum = queueItem.getDescription().getExtras().getString("album");
        long duration = queueItem.getDescription().getExtras().getLong("duration");
        String trackPlaylist = queueItem.getDescription().getExtras().getString("track_playlist");

        long position = queueItem.getQueueId();

        MediaDescriptionCompat description = mergeMediaDescription(mediaId, title, artist, alnum, duration, uri, trackPlaylist);

        return new MediaSessionCompat.QueueItem(description, position);
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
