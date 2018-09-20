package com.mnemo.angler.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.playlists.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
        db.execSQL("CREATE TABLE " + PlaylistEntry.TABLE_NAME + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PlaylistEntry.COLUMN_NAME + " TEXT, "
                + PlaylistEntry.COLUMN_IMAGE_RESOURCE + " TEXT, "
                + PlaylistEntry.COLUMN_TRACKS_TABLE + " TEXT, "
                + PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " INTEGER);");
        insertPlaylist(db, PlaylistEntry.LIBRARY, "R.drawable.back3",PlaylistEntry.LIBRARY,2);


        // creating library track table
        createTrackTable(db, PlaylistEntry.LIBRARY);

        // creating temp track table
        createTrackTable(db, "temp_table");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    // inserting playlist into playlists table
    private void insertPlaylist(SQLiteDatabase db, String name, String imageResource, String tracksTable, int defaultPlaylist){

        ContentValues contentValues = new ContentValues();
        contentValues.put(PlaylistEntry.COLUMN_NAME, name);
        contentValues.put(PlaylistEntry.COLUMN_IMAGE_RESOURCE,imageResource);
        contentValues.put(PlaylistEntry.COLUMN_TRACKS_TABLE, tracksTable);
        contentValues.put(PlaylistEntry.COLUMN_DEFAULT_PLAYLIST,defaultPlaylist);
        db.insert(PlaylistEntry.TABLE_NAME,null,contentValues);
    }


    // creating track table
    public void createTrackTable(SQLiteDatabase db, String name){
        db.execSQL("CREATE TABLE " + name + "(_id TEXT PRIMARY KEY, "
                + TrackEntry.COLUMN_TITLE + " TEXT, "
                + TrackEntry.COLUMN_ARTIST + " TEXT, "
                + TrackEntry.COLUMN_ALBUM + " TEXT, "
                + TrackEntry.COLUMN_DURATION + " INTEGER, "
                + TrackEntry.COLUMN_URI + " TEXT, "
                + TrackEntry.COLUMN_POSITION + " INTEGER);");
    }


    // inserting track into track table

    public void insertTrack(SQLiteDatabase db, String trackTable, String title, String artist, String album,long duration, String uri, long position){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",(title + "-" + artist + "-" + album).replace(" ", "_"));
        contentValues.put(TrackEntry.COLUMN_TITLE, title);
        contentValues.put(TrackEntry.COLUMN_ARTIST, artist);
        contentValues.put(TrackEntry.COLUMN_ALBUM, album);
        contentValues.put(TrackEntry.COLUMN_DURATION, duration);
        contentValues.put(TrackEntry.COLUMN_URI, uri);
        contentValues.put(TrackEntry.COLUMN_POSITION, position);
        db.insert(trackTable,null,contentValues);
    }



    private ContentValues putTrackInContentValues(Track track){

        ContentValues contentValues = new ContentValues();
        contentValues.put("_id",track.getId());
        contentValues.put(TrackEntry.COLUMN_TITLE, track.getTitle());
        contentValues.put(TrackEntry.COLUMN_ARTIST, track.getArtist());
        contentValues.put(TrackEntry.COLUMN_ALBUM, track.getAlbum());
        contentValues.put(TrackEntry.COLUMN_DURATION, track.getDuration());
        contentValues.put(TrackEntry.COLUMN_URI, track.getUri());

        return contentValues;
    }


    // recursively retrieve metadata from phone storage and add to local track table
    private HashMap<String, Track> gatherAudioTracks(String path) {

        HashMap<String, Track> trackMap = new HashMap<>();

        File directory = new File(path);
        ArrayList<String> tracks = new ArrayList<>(Arrays.asList(directory.list()));

        if (directory.getName().startsWith(".") || tracks.contains(".nomedia")){
            return trackMap;
        }

        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

        for (String track : tracks) {

            File temp = new File(path + File.separator + track);

            if (temp.isDirectory()) {

                trackMap.putAll(gatherAudioTracks(path + File.separator + track));

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


                            trackMap.put(id, new Track(id, title, artist, album, duration, uri));
                        }
                    }
                }
            }
        }

        return trackMap;
    }


    void updateSources(){

        // update library
        HashMap<String, Track> localTracks = gatherAudioTracks(Environment.getExternalStorageDirectory().getPath());

        for (Track track : localTracks.values()){

             Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, PlaylistEntry.LIBRARY),
                    null,
                    "_id = ?", new String[]{track.getId()}, null);

             if (cursor.getCount() == 0){

                 // insert new track in library
                 context.getContentResolver().insert(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, PlaylistEntry.LIBRARY),
                         putTrackInContentValues(track));

             }else{

                 cursor.moveToFirst();

                 String uri = cursor.getString(5);

                 if (!uri.equals(track.getUri())){

                     // update track in library if it was moved
                     context.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, PlaylistEntry.LIBRARY),
                             putTrackInContentValues(track), "_id = ?", new String[]{track.getId()});
                 }
             }

             cursor.close();
        }


        // delete tracks from library
        Cursor deletionCursor = context.getContentResolver().query(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, PlaylistEntry.LIBRARY),
                null, null, null, null);

        if (deletionCursor.getCount() != 0){

            deletionCursor.moveToFirst();

            do{
                if (!localTracks.containsKey(deletionCursor.getString(0))){

                    context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, PlaylistEntry.LIBRARY),
                            "_id = ?", new String[]{deletionCursor.getString(0)});
                }

            }while (deletionCursor.moveToNext());
        }

        deletionCursor.close();



        // update playlists
        Cursor playlistsCursor = context.getContentResolver().query(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, PlaylistEntry.TABLE_NAME),
                null, PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + "= 0", null, null);


        if (playlistsCursor.getCount() != 0){

            playlistsCursor.moveToFirst();

            do{
                String tracksTable = playlistsCursor.getString(playlistsCursor.getColumnIndex(PlaylistEntry.COLUMN_TRACKS_TABLE));

                Cursor tracksTableCursor = context.getContentResolver().query(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, tracksTable),
                        null, null, null, null);

                if (tracksTableCursor.getCount() != 0){

                    tracksTableCursor.moveToFirst();

                    do {
                        String checkingId = tracksTableCursor.getString(0);

                        if (!localTracks.containsKey(checkingId)){

                            // delete track from table
                            context.getContentResolver().delete(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, tracksTable),
                                    "_id = ?", new String[]{checkingId});
                        }else{

                            String uri = tracksTableCursor.getString(5);

                            Track track = localTracks.get(checkingId);

                            if (!uri.equals(track.getUri())){

                                 // update track in table if it was moved
                                 context.getContentResolver().update(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, tracksTable),
                                         putTrackInContentValues(track), "_id = ?", new String[]{checkingId});
                            }

                        }



                    }while (tracksTableCursor.moveToNext());


                    tracksTableCursor.close();
                }


            }while(playlistsCursor.moveToNext());
        }

        playlistsCursor.close();

    }



    public static String createTrackTableName(String title){

        return title.replaceAll("[ !.,:'-]", "_");
    }
}
