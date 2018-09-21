package com.mnemo.angler.data;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.database.AnglerDB;
import com.mnemo.angler.data.file_storage.AnglerFileStorage;
import com.mnemo.angler.data.networking.AnglerNetworking;
import com.mnemo.angler.data.preferences.AnglerPreferences;

import javax.inject.Inject;
public class AnglerRepository {

    @Inject
    AnglerDB anglerDB;

    @Inject
    AnglerFileStorage anglerFileStorage;

    @Inject
    AnglerNetworking anglerNetworking;

    @Inject
    AnglerPreferences anglerPreferences;

    @Inject
    public AnglerRepository() {

        AnglerApp.getAnglerComponent().injectAnglerRepository(this);

        anglerPreferences.initializePreferences();
        anglerFileStorage.createAppFolder();
        anglerDB.updateDatabase();

        anglerNetworking.loadAlbums();
        anglerNetworking.loadArtistsImages();
        anglerNetworking.loadArtistsBio();
    }


    public String getBackgroundImage(){

       String backgroundImage = anglerPreferences.getBackgroundImage();

        if (!backgroundImage.startsWith("R.drawable.")) {
            if (!anglerFileStorage.isFileExist(backgroundImage)) {
                backgroundImage = "R.drawable.back";
                anglerPreferences.setBackgroundImage(backgroundImage);
            }
        }

       return backgroundImage;

    }

    public int getBackgroundOpacity(){
        return anglerPreferences.getBackgroundOpacity();
    }
}
