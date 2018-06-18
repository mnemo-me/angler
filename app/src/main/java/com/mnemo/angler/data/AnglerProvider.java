package com.mnemo.angler.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AnglerProvider extends ContentProvider{

    AnglerSQLiteDBHelper dbHelper;

    private static final int PLAYLISTS = 100;
    private static final int ALBUM_LIST = 200;
    private static final int ALBUM = 201;
    private static final int ARTIST_LIST = 300;
    private static final int ARTIST = 301;
    private static final int PLAYLIST_ARTIST = 302;
    private static final int TRACK_TABLE = 400;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, AnglerContract.PlaylistEntry.TABLE_NAME, PLAYLISTS);
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, "album_list/*", ALBUM_LIST);
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, "album/*/*", ALBUM);
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, "artist_list/*", ARTIST_LIST);
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, "artist/*", ARTIST);
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, "playlist_artist/*/*", PLAYLIST_ARTIST);
        sUriMatcher.addURI(AnglerContract.CONTENT_AUTHORITY, "*", TRACK_TABLE);
    }

    @Override
    public boolean onCreate() {

        dbHelper = new AnglerSQLiteDBHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){

            case PLAYLISTS:

                cursor = db.query(AnglerContract.PlaylistEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;

            case ALBUM_LIST:

                cursor = db.query(uri.getPath().substring(12),
                        projection, selection, selectionArgs,
                        AnglerContract.TrackEntry.COLUMN_ALBUM, null, sortOrder);

                break;

            case ALBUM:

                String artistAlbum = uri.getPath().substring(7);
                String artist = artistAlbum.substring(0, artistAlbum.lastIndexOf("/")).replace("\\", "/");
                String album = artistAlbum.substring(artistAlbum.lastIndexOf("/") + 1).replace("\\", "/");

                if (projection != null) {
                    if (projection.length == 2) {
                        if (projection[0].equals("_id") && projection[1].equals(AnglerContract.TrackEntry.COLUMN_ARTIST)) {
                            cursor = db.query(AnglerContract.SourceEntry.SOURCE_LIBRARY, projection,
                                    AnglerContract.TrackEntry.COLUMN_ARTIST + " = ? AND " + AnglerContract.TrackEntry.COLUMN_ALBUM + " = ?", new String[]{artist, album},
                                    AnglerContract.TrackEntry.COLUMN_ARTIST, null, sortOrder);
                            break;
                        }
                    }
                }

                cursor = db.query(AnglerContract.SourceEntry.SOURCE_LIBRARY, projection,
                        AnglerContract.TrackEntry.COLUMN_ARTIST + " = ? AND " + AnglerContract.TrackEntry.COLUMN_ALBUM + " = ?", new String[]{artist, album},
                        null, null,
                        AnglerContract.TrackEntry.COLUMN_TITLE + " ASC");
                break;

            case ARTIST_LIST:

                cursor = db.query(uri.getPath().substring(13),
                        projection, selection, selectionArgs,
                        AnglerContract.TrackEntry.COLUMN_ARTIST, null, sortOrder);

                break;

            case ARTIST:

                cursor = db.query(AnglerContract.SourceEntry.SOURCE_LIBRARY, projection,
                        AnglerContract.TrackEntry.COLUMN_ARTIST + " = ?", new String[]{uri.getPath().substring(8)},
                        null, null,
                        AnglerContract.TrackEntry.COLUMN_TITLE + " ASC");

                break;

            case PLAYLIST_ARTIST:

                String playlistArtistPath = uri.getPath().substring(17);
                String playlist = playlistArtistPath.substring(0, playlistArtistPath.lastIndexOf("/")).replace("\\", "/");
                String playlistArtist = playlistArtistPath.substring(playlistArtistPath.lastIndexOf("/") + 1).replace("\\", "/");

                cursor = db.query(playlist, projection,
                        AnglerContract.TrackEntry.COLUMN_ARTIST + " = ?", new String[]{playlistArtist},
                        null, null,
                        AnglerContract.TrackEntry.COLUMN_TITLE + " ASC");

                break;

            case TRACK_TABLE:

                cursor = db.query(uri.getPath().substring(1),
                        projection,
                        selection, selectionArgs,
                        null, null, sortOrder);

                break;

            default:
                throw new IllegalArgumentException("Unknown uri type " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = sUriMatcher.match(uri);

        switch (match){

            case PLAYLISTS:
                return AnglerContract.PlaylistEntry.CONTENT_LIST_TYPE;
            case ALBUM_LIST:
                return AnglerContract.TrackEntry.CONTENT_ALBUM_LIST_TYPE;
            case ALBUM:
                return AnglerContract.TrackEntry.CONTENT_ALBUM_TYPE;
            case ARTIST_LIST:
                return AnglerContract.TrackEntry.CONTENT_ARTIST_LIST_TYPE;
            case ARTIST:
                return AnglerContract.TrackEntry.CONTENT_ARTIST_TYPE;
            case PLAYLIST_ARTIST:
                return AnglerContract.TrackEntry.CONTENT_PLAYLIST_ARTIST_TYPE;
            case TRACK_TABLE:
                return AnglerContract.TrackEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalArgumentException("Unknown MIME Type");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Long id;

        int match = sUriMatcher.match(uri);

        switch (match){

            case PLAYLISTS:
                id = db.insert(AnglerContract.PlaylistEntry.TABLE_NAME, null, contentValues);
                break;
            case TRACK_TABLE:
                id = db.insert(uri.getPath().substring(1), null, contentValues);
                break;
            default:
                throw  new IllegalArgumentException("Insertion failed");
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        int match = sUriMatcher.match(uri);

        switch (match){

            case PLAYLISTS:
                rowsDeleted = db.delete(AnglerContract.PlaylistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRACK_TABLE:
                rowsDeleted = db.delete(uri.getPath().substring(1), selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;

        int match = sUriMatcher.match(uri);

        switch (match){

            case PLAYLISTS:
                rowsUpdated = db.update(AnglerContract.PlaylistEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case TRACK_TABLE:
                rowsUpdated = db.update(uri.getPath().substring(1), contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update failed");
        }

        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

}
