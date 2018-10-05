package com.mnemo.angler.ui.main_activity.fragments.artists.artist_bio;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;


public class ArtistBioPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistBioPresenter() {
        AnglerApp.getAnglerComponent().injectArtistBioPresenter(this);
    }

    // Load artist bio
    void loadBio(String artist){
        String bio = repository.loadArtistBio(artist);

        if (getView() != null){
            ((ArtistBioView)getView()).setBio(bio);
        }
    }
}
