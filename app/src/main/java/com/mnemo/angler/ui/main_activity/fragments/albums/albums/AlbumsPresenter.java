package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;
import com.mnemo.angler.util.MediaAssistant;

import javax.inject.Inject;

public class AlbumsPresenter extends BasePresenter{

    @Inject
    AnglerRepository repository;

    AlbumsPresenter() {
        AnglerApp.getAnglerComponent().injectAlbumsPresenter(this);
    }

    // Load albums from database
    void loadAlbums(){

        repository.loadPlaylistTracks("library", tracks -> {

            if (getView() != null){

                ((AlbumsView)getView()).setAlbums(MediaAssistant.getAlbums(tracks));
            }
        });
    }


}
