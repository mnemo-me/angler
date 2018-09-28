package com.mnemo.angler.data;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.database.AnglerDB;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.data.file_storage.AnglerFileStorage;
import com.mnemo.angler.data.networking.AnglerNetworking;
import com.mnemo.angler.data.preferences.AnglerPreferences;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

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

        Completable.fromAction(() -> {
            anglerPreferences.initializePreferences();
            anglerFileStorage.createAppFolder();

            ArrayList<Track> tracks = anglerFileStorage.scanTracks(AnglerFileStorage.PHONE_STORAGE);
            anglerDB.updateDatabase(tracks);

            anglerNetworking.loadAlbums();
            anglerNetworking.loadArtistsImages();
            anglerNetworking.loadArtistsBio();
        }).subscribeOn(Schedulers.io()).subscribe();


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


    public void setMainPlaylist(String playlist){
        anglerPreferences.setMainPlaylist(playlist);
    }


    public void loadPlaylists(AnglerDB.PlaylistsUpdateListener listener){
        anglerDB.loadPlaylists(listener);
    }

    public void loadPlaylist(String playlist, AnglerDB.PlaylistLoadListener listener){
        anglerDB.loadPlaylist(playlist, listener);
    }

    public void loadArtists(String playlist, AnglerDB.ArtistsLoadListener listener){
        anglerDB.loadArtists(playlist, listener);
    }

    public void loadArtistTracksFromPlaylist(String playlist, String artist, AnglerDB.ArtistTracksLoadListener listener){
        anglerDB.loadArtistTracksFromPlaylist(playlist, artist, listener);
    }
}
