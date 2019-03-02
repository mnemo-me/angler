package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import javax.inject.Inject;

public class AlbumsPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    AlbumsPresenter() {
        AnglerApp.getAnglerComponent().injectAlbumsPresenter(this);
    }

    // Load albums from database
    void loadAlbums(){

        setListener(repository.loadAlbums(albums -> {

            if (getView() != null){

                ((AlbumsView)getView()).setAlbums(albums);
            }
        }));
    }

    // Check album cover exist
    boolean checkAlbumCoverExist(String artist, String album){
        return repository.checkAlbumCoverExist(artist, album);
    }
}
