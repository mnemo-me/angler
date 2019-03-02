package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;


import javax.inject.Inject;


public class ArtistAlbumsPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistAlbumsPresenter() {
        AnglerApp.getAnglerComponent().injectArtistAlbumsPresenter(this);
    }

    void loadArtistAlbums(String artist){

        setListener(repository.loadArtistAlbums(artist, albums -> {

            if (getView() != null){
                ((ArtistAlbumsView)getView()).setArtistAlbums(albums);
            }
        }));
    }

    // Check album cover exist
    boolean checkAlbumCoverExist(String artist, String album){
        return repository.checkAlbumCoverExist(artist, album);
    }
}
