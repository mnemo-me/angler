package com.mnemo.angler.data.file_storage;


import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

public class AnglerFileStorage {

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
}
