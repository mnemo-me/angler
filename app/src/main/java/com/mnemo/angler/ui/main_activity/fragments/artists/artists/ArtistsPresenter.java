package com.mnemo.angler.ui.main_activity.fragments.artists.artists;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.networking.AnglerNetworking;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class ArtistsPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistsPresenter() {
        AnglerApp.getAnglerComponent().injectArtistsPresenter(this);
    }

    // Load artists
    void loadArtists(){
        repository.loadArtists("library", artists -> {

            if (getView() != null){
                ((ArtistsView)getView()).setArtists(artists);
            }
        });
    }

    // Refresh artists images
    void refreshArtistsImages(){
        repository.refreshArtistImages(() -> {

            if (getView() != null){
                ((ArtistsView)getView()).completeRefreshingImages();
            }
        });
    }
}
