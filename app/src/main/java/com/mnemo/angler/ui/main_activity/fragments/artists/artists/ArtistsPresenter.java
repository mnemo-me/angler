package com.mnemo.angler.ui.main_activity.fragments.artists.artists;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import javax.inject.Inject;


public class ArtistsPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistsPresenter() {
        AnglerApp.getAnglerComponent().injectArtistsPresenter(this);
    }

    // Load artists
    void loadArtists(){
        setListener(repository.loadArtists("library", artists -> {

            if (getView() != null){
                ((ArtistsView)getView()).setArtists(artists);
            }
        }));
    }

    // Check artist image exist
    boolean checkArtistImageExist(String artist){
        return repository.checkArtistImageExist(artist);
    }
}
