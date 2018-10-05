package com.mnemo.angler.data.file_storage;


import android.os.Environment;

import java.io.File;

public class AnglerFolder {

    public static final String PATH_MAIN = Environment.getExternalStorageDirectory() + File.separator + "Angler";
    public static final String PATH_BACKGROUND = PATH_MAIN + File.separator + "background";
    public static final String PATH_BACKGROUND_PORTRAIT = PATH_BACKGROUND + File.separator + "port";
    public static final String PATH_BACKGROUND_LANDSCAPE = PATH_BACKGROUND + File.separator + "land";
    public static final String PATH_PLAYLIST_COVER = PATH_MAIN + File.separator + "playlist cover";
    public static final String PATH_ALBUM_COVER = PATH_MAIN + File.separator + "album cover";
    public static final String PATH_ARTIST_IMAGE = PATH_MAIN + File.separator + "artist image";
    public static final String PATH_ARTIST_BIO = PATH_MAIN + File.separator + "artist bio";

}
