package com.mnemo.angler.ui.main_activity.fragments.artists.artist_tracks;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import javax.inject.Inject;

public class ArtistTracksPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistTracksPresenter() {
        AnglerApp.getAnglerComponent().injectArtistTracksPresenter(this);
    }

    // Load artist tracks from database
    void loadArtistTracks(String artist){

        setListener(repository.loadArtistTracksFromPlaylist("library", artist, tracks -> {

            if (getView() != null){
                ((ArtistTracksView)getView()).setArtistTracks(tracks);
            }
        }));
    }
}
