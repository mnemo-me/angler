package com.mnemo.angler.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;

public final class AnglerContract {

    // Constants for Content Provider URI
    public static final String CONTENT_AUTHORITY = "com.mnemo.angler.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    /*
    constants for playlists SQLite db
     */
    public static final class PlaylistEntry implements BaseColumns {

        public static final String LIBRARY = "Library";

        public static final String TABLE_NAME = "playlists";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_RESOURCE = "image_resource";
        public static final String COLUMN_TRACKS_TABLE = "tracks_table";
        public static final String COLUMN_DEFAULT_PLAYLIST = "default_playlist";
    }

    /*
    common constants for tracks tables SQLite db
     */
    public static final class TrackEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/track_table";
        public static final String CONTENT_ALBUM_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/album";
        public static final String CONTENT_ALBUM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/album";
        public static final String CONTENT_ARTIST_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/artist";
        public static final String CONTENT_ARTIST_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/artist";
        public static final String CONTENT_PLAYLIST_ARTIST_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/playlist_artist";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_SOURCE = "source";
        public static final String COLUMN_POSITION = "position";
    }
}
