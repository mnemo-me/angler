package com.mnemo.angler.util;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.classes.Album;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static Track combineMetadataInTrack(MediaMetadataCompat metadata){

        String mediaId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        String uri = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);

        return new Track(mediaId, title, artist, album, duration, uri);

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


    // Extract albums from tracks
    public static List<Album> getAlbums(List<Track> tracks){

        List<Album> albums = new ArrayList<>();
        Set<String> albumTitles = new HashSet<>();


        for (Track track : tracks){

            if (!albumTitles.contains(track.getAlbum())){

                albumTitles.add(track.getAlbum());
                albums.add(new Album(track.getAlbum(), track.getArtist()));
            }
        }

        return albums;
    }
}
