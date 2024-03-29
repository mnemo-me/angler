package com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

public class AlbumConfigurationPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    AlbumConfigurationPresenter() {
        AnglerApp.getAnglerComponent().injectAlbumConfigurationPresenter(this);
    }

    // Load album tracks
    void loadAlbumTracks(String artist, String album){

        setListener(repository.loadAlbumTracks(artist, album, tracks -> {

            if (getView() != null){

                this.tracks = tracks;
                ((AlbumConfigurationView)getView()).setAlbumTracks(tracks);
            }
        }));
    }

    // Check album cover exist
    boolean checkAlbumCoverExist(String artist, String album){
        return repository.checkAlbumCoverExist(artist, album);
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Get year
    void getYear(String artist, String album){

        repository.loadAlbumYear(artist, album, year -> {

            if (getView() != null){

                ((AlbumConfigurationView)getView()).setAlbumYear(year);
            }
        });
    }
}
