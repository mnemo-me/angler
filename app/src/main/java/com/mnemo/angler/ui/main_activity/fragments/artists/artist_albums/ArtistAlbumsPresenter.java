package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;


import javax.inject.Inject;


public class ArtistAlbumsPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistAlbumsPresenter() {
        AnglerApp.getAnglerComponent().injectArtistAlbumsPresenter(this);
    }

    void loadArtistAlbums(String artist){

        repository.loadArtistAlbums(artist, albums -> {

            if (getView() != null){
                ((ArtistAlbumsView)getView()).setArtistAlbums(albums);
            }
        });
    }
}
