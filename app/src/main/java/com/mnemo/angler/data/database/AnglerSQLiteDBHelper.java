package com.mnemo.angler.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.data.networking.AnglerMediaScrobbler;
import com.mnemo.angler.ui.main_activity.fragments.playlists.Track;

import java.io.File;

public class AnglerSQLiteDBHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "angler";
    private static final int DB_VERSION = 1;
    private Context context;

    public AnglerSQLiteDBHelper(Context context){
        super(context, DB_NAME,null,DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating playlists table and add library playlist
        db.execSQL("CREATE TABLE " + AnglerContract.PlaylistEntry.TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AnglerContract.PlaylistEntry.COLUMN_NAME + " TEXT, "
                + AnglerContract.PlaylistEntry.COLUMN_IMAGE_RESOURCE + " TEXT, "
                + AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE + " TEXT, "
                + AnglerContract.PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " INTEGER);");
        insertPlaylist(db, AnglerContract.PlaylistEntry.LIBRARY, "R.drawable.back3", AnglerContract.PlaylistEntry.LIBRARY,2);


        // creating library track table
        createTrackTable(db, AnglerContract.PlaylistEntry.LIBRARY);

        // creating temp track table
        createTrackTable(db, "temp_table");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // inserting playlist into playlists table
    private void insertPlaylist(SQLiteDatabase db, String name, String imageResource, String tracksTable, int defaultPlaylist){

        ContentValues contentValues = new ContentValues();
        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_NAME, name);
        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_IMAGE_RESOURCE,imageResource);
        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_TRACKS_TABLE, tracksTable);
        contentValues.put(AnglerContract.PlaylistEntry.COLUMN_DEFAULT_PLAYLIST,defaultPlaylist);
        db.insert(AnglerContract.PlaylistEntry.TABLE_NAME,null,contentValues);
    }


    // creating track table
    public void createTrackTable(SQLiteDatabase db, String name){
        db.execSQL("CREATE TABLE " + name + "(_id TEXT PRIMARY KEY, "
                + AnglerContract.TrackEntry.COLUMN_TITLE + " TEXT, "
                + AnglerContract.TrackEntry.COLUMN_ARTIST + " TEXT, "
                + AnglerContract.TrackEntry.COLUMN_ALBUM + " TEXT, "
                + AnglerContract.TrackEntry.COLUMN_DURATION + " INTEGER, "
                + AnglerContract.TrackEntry.COLUMN_URI + " TEXT, "
                + AnglerContract.TrackEntry.COLUMN_POSITION + " INTEGER);");
    }


    // inserting track into track table

    public void insertTrack(SQLiteDatabase db, String trackTable, String title, String artist, String album,long duration, String uri, long position){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",(title + "-" + artist + "-" + album).replace(" ", "_"));
        contentValues.put(AnglerContract.TrackEntry.COLUMN_TITLE, title);
        contentValues.put(AnglerContract.TrackEntry.COLUMN_ARTIST, artist);
        contentValues.put(AnglerContract.TrackEntry.COLUMN_ALBUM, album);
        contentValues.put(AnglerContract.TrackEntry.COLUMN_DURATION, duration);
        contentValues.put(AnglerContract.TrackEntry.COLUMN_URI, uri);
        contentValues.put(AnglerContract.TrackEntry.COLUMN_POSITION, position);
        db.insert(trackTable,null,contentValues);
    }



    private ContentValues putTrackInContentValues(Track track){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",track.getId());
        contentValues.put(AnglerContract.TrackEntry.COLUMN_TITLE, track.getTitle());
        contentValues.put(AnglerContract.TrackEntry.COLUMN_ARTIST, track.getArtist());
        contentValues.put(AnglerContract.TrackEntry.COLUMN_ALBUM, track.getAlbum());
        contentValues.put(AnglerContract.TrackEntry.COLUMN_DURATION, track.getDuration());
        contentValues.put(AnglerContract.TrackEntry.COLUMN_URI, track.getUri());

        return contentValues;
    }


    // recursively retrieve metadata from phone storage and add to local track table
    private void gatherAudioTracks(String path) {

        String artist = null;
        String album = null;
        String uri = null;

                        if (artist != null) {

                            String artistPath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist;
                            new File(artistPath).mkdir();
                            File file = new File(artistPath, album + ".jpg");

                            if (!file.exists()) {
                                AnglerMediaScrobbler.getAlbumCover(artist, album, uri);
                            }

                            File artistFile = new File(AnglerFolder.PATH_ARTIST_IMAGE, artist + ".jpg");

                            if (!artistFile.exists()) {
                                AnglerMediaScrobbler.getArtistImage(artist);
                            }



        }


    }





    public static String createTrackTableName(String title){

        return title.replaceAll("[ !.,:'-]", "_");
    }
}
