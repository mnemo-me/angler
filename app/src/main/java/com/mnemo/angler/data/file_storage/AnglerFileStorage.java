package com.mnemo.angler.data.file_storage;


import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.data.database.Entities.Track;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

public class AnglerFileStorage {

    public static final String PHONE_STORAGE = Environment.getExternalStorageDirectory().getPath();

    @Inject
    public AnglerFileStorage() {
    }


    public void createAppFolder() {

        new File(AnglerFolder.PATH_MAIN).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE).mkdir();
        new File(AnglerFolder.PATH_PLAYLIST_COVER).mkdir();
        new File(AnglerFolder.PATH_ALBUM_COVER).mkdir();
        new File(AnglerFolder.PATH_ARTIST_IMAGE).mkdir();

        try {
            new File(AnglerFolder.PATH_MAIN, ".nomedia").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFileExist(String filepath){
        return new File(filepath).exists();
    }

    // recursively retrieve metadata from phone storage
    public ArrayList<Track> scanTracks(String filepath){

        ArrayList<Track> tracks = new ArrayList<>();

        File directory = new File(filepath);
        ArrayList<String> files = new ArrayList<>(Arrays.asList(directory.list()));

        if (directory.getName().startsWith(".") || tracks.contains(".nomedia")){
            return tracks;
        }

        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

        for (String file : files) {

            File temp = new File(filepath + File.separator + file);

            if (temp.isDirectory()) {

                tracks.addAll(scanTracks(filepath + File.separator + file));

            } else {

                String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                if (mimeType != null) {
                    if (mimeType.startsWith("audio/")) {

                        try {
                            mRetriever.setDataSource(filepath + File.separator + file);
                        }catch (Exception e){
                            continue;
                        }

                        String title = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String album = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        long duration = Long.parseLong(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        String id = (title + "-" + artist + "-" + album).replace(" ", "_");
                        String uri = filepath + File.separator + file;

                        tracks.add(new Track(id, title, artist, album, duration, uri));

                    }
                }
            }
        }

        return tracks;
    }
}
