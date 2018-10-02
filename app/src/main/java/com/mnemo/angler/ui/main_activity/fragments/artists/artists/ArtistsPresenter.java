package com.mnemo.angler.ui.main_activity.fragments.artists.artists;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class ArtistsPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistsPresenter() {
        AnglerApp.getAnglerComponent().injectArtistsPresenter(this);
    }

    void loadArtists(){
        repository.loadArtists("library", artists -> {

            if (getView() != null){
                ((ArtistsView)getView()).setArtists(artists);
            }
        });
    }
}
