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
    constants for sources SQLite db
     */
    public static final class SourceEntry implements BaseColumns {

        public static final String TABLE_NAME = "sources";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;


        public static final String SOURCE_LIBRARY = "Library";
        public static final String SOURCE_PHONE_STORAGE = "Phone_storage";
        public static final String SOURCE_SD_CARD = "SD_card";
        public static final String SOURCE_LOCAL = "Local";

        public static final String SOURCE_SPOTIFY = "Spotify";
        public static final String SOURCE_NAPSTER = "Napster";

        public static final String SOURCE_GOOGLE_DRIVE = "Google_Drive";
        public static final String SOURCE_DROPBOX = "Dropbox";
        public static final String SOURCE_AMAZON_DRIVE = "Amazon_Drive";
        public static final String SOURCE_BOX = "Box";
        public static final String SOURCE_ONEDRIVE = "OneDrive";
        public static final String SOURCE_YANDEX_DISK = "Yandex_Disk";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_RESOURCE = "image_resource";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TRACKS_TABLE = "tracks_table";
        public static final String COLUMN_TRACKS_COUNT = "tracks_count";

        public static final ArrayList<String> SOURCES = new ArrayList<String>(){{
            add(SOURCE_LIBRARY);
            add(SOURCE_PHONE_STORAGE);
            add(SOURCE_SD_CARD);
            add(SOURCE_LOCAL);
            add(SOURCE_SPOTIFY);
            add(SOURCE_NAPSTER);
            add(SOURCE_GOOGLE_DRIVE);
            add(SOURCE_DROPBOX);
            add(SOURCE_AMAZON_DRIVE);
            add(SOURCE_BOX);
            add(SOURCE_ONEDRIVE);
            add("Яндекс.Диск");
        }};

        public static final ArrayList<String> SERVICES = new ArrayList<String>(){{
            add(SOURCE_SPOTIFY);
            add(SOURCE_NAPSTER);
        }};

        public static final ArrayList<String> STORAGE = new ArrayList<String>(){{
            add(SOURCE_PHONE_STORAGE);
            add(SOURCE_GOOGLE_DRIVE);
            add(SOURCE_DROPBOX);
            add(SOURCE_AMAZON_DRIVE);
            add(SOURCE_BOX);
            add(SOURCE_ONEDRIVE);
            add("Яндекс.Диск");
        }};
    }

    /*
    constants for playlists SQLite db
     */
    public static final class PlaylistEntry implements BaseColumns {

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
