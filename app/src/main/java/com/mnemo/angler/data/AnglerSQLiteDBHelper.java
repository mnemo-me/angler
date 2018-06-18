package com.mnemo.angler.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.playlist_manager.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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

        /*
        creating sources table and add local source to it
         */
        db.execSQL("CREATE TABLE " + SourceEntry.TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SourceEntry.COLUMN_NAME + " TEXT, "
                + SourceEntry.COLUMN_IMAGE_RESOURCE + " TEXT, "
                + SourceEntry.COLUMN_DESCRIPTION + " TEXT, "
                + SourceEntry.COLUMN_TRACKS_TABLE + " TEXT,"
                + SourceEntry.COLUMN_TRACKS_COUNT + " INTEGER);");
        insertSource(db, SourceEntry.SOURCE_PHONE_STORAGE, "R.drawable.ic_phone_android_black_48dp", Build.MANUFACTURER + " " + Build.MODEL, SourceEntry.SOURCE_PHONE_STORAGE, 0);


        /*
        creating playlists table and add library and local playlists
         */
        db.execSQL("CREATE TABLE " + PlaylistEntry.TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PlaylistEntry.COLUMN_NAME + " TEXT, "
                + PlaylistEntry.COLUMN_IMAGE_RESOURCE + " TEXT, "
                + PlaylistEntry.COLUMN_TRACKS_TABLE + " TEXT, "
                + PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " INTEGER);");
        insertPlaylist(db, SourceEntry.SOURCE_LIBRARY, "R.drawable.back3",SourceEntry.SOURCE_LIBRARY,2);
        insertPlaylist(db,SourceEntry.SOURCE_LOCAL, "R.drawable.back",SourceEntry.SOURCE_LOCAL,1);

        /*
        creating library and phone storage track tables
         */
        createTrackTable(db,SourceEntry.SOURCE_LIBRARY);
        createTrackTable(db,SourceEntry.SOURCE_PHONE_STORAGE);

        /*
        creating local track table (combine phone storage and sd card tables)
         */
        createTrackTable(db, SourceEntry.SOURCE_LOCAL);

        /*
        creating temp track table
         */
        createTrackTable(db, "temp_table");

        /*
        creating album track table
         */
        createTrackTable(db, "album_play_table");


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
    inserting source into sources table
     */
    private void insertSource(SQLiteDatabase db, String name, String imageResource, String description, String tracksTable, long tracksCount){

        ContentValues contentValues = new ContentValues();
        contentValues.put(SourceEntry.COLUMN_NAME, name);
        contentValues.put(SourceEntry.COLUMN_IMAGE_RESOURCE,imageResource);
        contentValues.put(SourceEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(SourceEntry.COLUMN_TRACKS_TABLE,tracksTable);
        contentValues.put(SourceEntry.COLUMN_TRACKS_COUNT, tracksCount);
        db.insert(SourceEntry.TABLE_NAME,null,contentValues);
    }


    /*
    inserting playlist into playlists table
     */
    public void insertPlaylist(SQLiteDatabase db, String name, String imageResource, String tracksTable, int defaultPlaylist){

        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistEntry.COLUMN_NAME, name);
        contentValues.put(PlaylistEntry.COLUMN_IMAGE_RESOURCE,imageResource);
        contentValues.put(PlaylistEntry.COLUMN_TRACKS_TABLE, tracksTable);
        contentValues.put(PlaylistEntry.COLUMN_DEFAULT_PLAYLIST,defaultPlaylist);
        db.insert(PlaylistEntry.TABLE_NAME,null,contentValues);
    }


    /*
    creating track table
     */
    public void createTrackTable(SQLiteDatabase db, String name){
        db.execSQL("CREATE TABLE " + name + "(_id TEXT PRIMARY KEY, "
                + TrackEntry.COLUMN_TITLE + " TEXT, "
                + TrackEntry.COLUMN_ARTIST + " TEXT, "
                + TrackEntry.COLUMN_ALBUM + " TEXT, "
                + TrackEntry.COLUMN_DURATION + " INTEGER, "
                + TrackEntry.COLUMN_URI + " TEXT, "
                + TrackEntry.COLUMN_SOURCE + " TEXT, "
                + TrackEntry.COLUMN_POSITION + " INTEGER);");
    }

    /*
    inserting track into track table
     */
    private void insertTrack(SQLiteDatabase db, String trackTable, String title, String artist, String album,long duration, String uri, String source){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",(title + "-" + artist + "-" + album).replace(" ", "_"));
        contentValues.put(TrackEntry.COLUMN_TITLE, title);
        contentValues.put(TrackEntry.COLUMN_ARTIST, artist);
        contentValues.put(TrackEntry.COLUMN_ALBUM, album);
        contentValues.put(TrackEntry.COLUMN_DURATION, duration);
        contentValues.put(TrackEntry.COLUMN_URI, uri);
        contentValues.put(TrackEntry.COLUMN_SOURCE, source);
        db.insert(trackTable,null,contentValues);
    }



    private void insertTrack(SQLiteDatabase db, String trackTable, String title, String artist, String album,long duration, String uri, String source, long position){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",(title + "-" + artist + "-" + album).replace(" ", "_"));
        contentValues.put(TrackEntry.COLUMN_TITLE, title);
        contentValues.put(TrackEntry.COLUMN_ARTIST, artist);
        contentValues.put(TrackEntry.COLUMN_ALBUM, album);
        contentValues.put(TrackEntry.COLUMN_DURATION, duration);
        contentValues.put(TrackEntry.COLUMN_URI, uri);
        contentValues.put(TrackEntry.COLUMN_SOURCE, source);
        contentValues.put(TrackEntry.COLUMN_POSITION, position);
        db.insert(trackTable,null,contentValues);
    }


    private void insertTrack(SQLiteDatabase db, String trackTable, Track track){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",track.getId());
        contentValues.put(TrackEntry.COLUMN_TITLE, track.getTitle());
        contentValues.put(TrackEntry.COLUMN_ARTIST, track.getArtist());
        contentValues.put(TrackEntry.COLUMN_ALBUM, track.getAlbum());
        contentValues.put(TrackEntry.COLUMN_DURATION, track.getDuration());
        contentValues.put(TrackEntry.COLUMN_URI, track.getUri());
        contentValues.put(TrackEntry.COLUMN_SOURCE, track.getSource());
        db.insert(trackTable,null,contentValues);
    }

    /*
    recursively retrieve metadata from phone storage and add to local track table
     */
    private ArrayList<Track> createLocalPlaylist(SQLiteDatabase db, String path) {

        ArrayList<Track> trackArrayList = new ArrayList<>();

        File directory = new File(path);
        ArrayList<String> tracks = new ArrayList<>(Arrays.asList(directory.list()));

        if (directory.getName().startsWith(".") || tracks.contains(".nomedia")){
            return trackArrayList;
        }

        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

        for (String track : tracks) {

            File temp = new File(path + File.separator + track);

            if (temp.isDirectory()) {

                trackArrayList.addAll(createLocalPlaylist(db, path + File.separator + track));

            } else {

                String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                if (mimeType != null) {
                    if (mimeType.startsWith("audio/")) {

                        try {
                            mRetriever.setDataSource(path + File.separator + track);
                        }catch (Exception e){
                            continue;
                        }

                        String title = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String album = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        long duration = Long.parseLong(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        String id = (title + "-" + artist + "-" + album).replace(" ", "_");

                        String uri = path + File.separator + track;

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

                            String source = SourceEntry.SOURCE_PHONE_STORAGE;

                            trackArrayList.add(new Track(id, title, artist, album, duration, uri, source));
                        }
                    }
                }
            }
        }

        /*
        update tracks_count column in sources table
         */
        Cursor cursor = db.query(SourceEntry.SOURCE_PHONE_STORAGE,new String[]{"COUNT(_id) As Count"},null, null, null, null, null);
        cursor.moveToFirst();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SourceEntry.COLUMN_TRACKS_COUNT, cursor.getInt(0));
        db.update(SourceEntry.TABLE_NAME,contentValues,  SourceEntry.COLUMN_TRACKS_TABLE + " == ?",new String[]{SourceEntry.SOURCE_PHONE_STORAGE});

        cursor.close();

        return trackArrayList;
    }


    public void updateSources(){

        SQLiteDatabase db = getWritableDatabase();

        // update local playlist
        ArrayList<Track> localTracks = createLocalPlaylist(db, Environment.getExternalStorageDirectory().getPath());
        context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, SourceEntry.SOURCE_PHONE_STORAGE), null, null);

        for(Track track : localTracks){
            insertTrack(db, SourceEntry.SOURCE_PHONE_STORAGE, track);
        }


        // populating local track table
        context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, SourceEntry.SOURCE_LOCAL), null, null);
        db.execSQL("INSERT INTO " + SourceEntry.SOURCE_LOCAL + " SELECT * FROM " + SourceEntry.SOURCE_PHONE_STORAGE + ";");


        // combine track tables into library
        context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, SourceEntry.SOURCE_LIBRARY), null, null);
        db.execSQL("INSERT INTO " + SourceEntry.SOURCE_LIBRARY + " SELECT * FROM " + SourceEntry.SOURCE_LOCAL + ";");


        // update playlists
        Cursor playlistsCursor = db.query(PlaylistEntry.TABLE_NAME, null, PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " = 0 ", null, null, null, null);
        Cursor tracksTableCursor = null;
        Cursor checkTrackCursor = null;

        if (playlistsCursor.getCount() != 0){

            playlistsCursor.moveToFirst();

            do{
                String tracksTable = playlistsCursor.getString(playlistsCursor.getColumnIndex(PlaylistEntry.COLUMN_TRACKS_TABLE));

                tracksTableCursor = db.query(tracksTable, null, null, null, null, null, null);

                if (tracksTableCursor.getCount() != 0){

                    tracksTableCursor.moveToFirst();

                    do {
                        String checkingId = tracksTableCursor.getString(0);

                        checkTrackCursor = db.query(SourceEntry.SOURCE_LIBRARY, null, "_id = '" + checkingId + "'", null, null, null, null);

                        if (checkTrackCursor.getCount() == 0){
                            context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, tracksTable), "_id = '" + checkingId + "'", null);
                        }

                    }while (tracksTableCursor.moveToNext());
                }


            }while(playlistsCursor.moveToNext());
        }


        playlistsCursor.close();
        if (tracksTableCursor != null) {
            tracksTableCursor.close();
        }
        if (checkTrackCursor != null) {
            checkTrackCursor.close();
        }
        db.close();

    }


    public static String createTrackTableName(String title){

        return title.replaceAll("[ !.,:'-]", "_");
    }
}
