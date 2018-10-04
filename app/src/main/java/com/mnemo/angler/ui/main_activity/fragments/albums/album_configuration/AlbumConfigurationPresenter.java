package com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

public class AlbumConfigurationPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    AlbumConfigurationPresenter() {
        AnglerApp.getAnglerComponent().injectAlbumConfigurationPresenter(this);
    }

    // Load album tracks
    void loadAlbumTracks(String artist, String album){

        repository.loadAlbumTracks(artist, album, tracks -> {

            if (getView() != null){

                this.tracks = tracks;
                ((AlbumConfigurationView)getView()).setAlbumTracks(tracks);
            }
        });
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }
}
