package com.mnemo.angler;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadata;
import android.net.Uri;

import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;

import java.util.Random;

public class PlaylistManager {

    private ContentResolver contentResolver;

    public static String mainPlaylistName;
    public static String currentPlaylistName;

    public static int position;
    private MediaMetadata metadata = null;

    PlaylistManager(ContentResolver contentResolver, String activePlaylist) {

        this.contentResolver = contentResolver;

        mainPlaylistName = activePlaylist;
        currentPlaylistName = activePlaylist;
    }


    // get id of currently playing track
    String getCurrentId(){

        String dbName;

        if (currentPlaylistName.startsWith("artist/") || currentPlaylistName.startsWith("album/") || currentPlaylistName.startsWith("playlist_artist/")){
            dbName = currentPlaylistName;
        }else {
            dbName = AnglerSQLiteDBHelper.createTrackTableName(currentPlaylistName.replace("playlist/", ""));
        }


        Cursor cursor = contentResolver.query(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, dbName),
                null, null, null,
                TrackEntry.COLUMN_POSITION + " ASC, " + TrackEntry.COLUMN_TITLE + " ASC, " + TrackEntry.COLUMN_ARTIST + " ASC");


        checkPosition(cursor.getCount());

        cursor.moveToPosition(position);
        String id = cursor.getString(0);

        // extract metadata
        String title = cursor.getString(1);
        String artist = cursor.getString(2);
        String album = cursor.getString(3);
        long duration = cursor.getLong(4);
        String uri = cursor.getString(5);

        MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE,title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST,artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM,album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION,duration)
                .putString(MediaMetadata.METADATA_KEY_MEDIA_URI, uri);

        metadata = metadataBuilder.build();

        cursor.close();

        return id;

    }


    //get metadata of currently playing track
    MediaMetadata getCurrentMetadata(){
        return metadata;
    }

    private void checkPosition(int size){

        if (PlaybackManager.shuffleState){
            position = new Random().nextInt(size);
            return;
        }

        if (position < 0){
            position = size -1;
            return;
        }

        if (position >= size){
            position = 0;
        }
    }


}
