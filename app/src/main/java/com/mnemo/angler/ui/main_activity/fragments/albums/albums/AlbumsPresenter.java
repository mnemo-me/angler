package com.mnemo.angler.ui.main_activity.fragments.albums.albums;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;
import com.mnemo.angler.ui.main_activity.classes.Album;
import com.mnemo.angler.util.MediaAssistant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

public class AlbumsPresenter extends BasePresenter{

    @Inject
    AnglerRepository repository;

    AlbumsPresenter() {
        AnglerApp.getAnglerComponent().injectAlbumsPresenter(this);
    }

    // Load albums from database
    void loadAlbums(){

        repository.loadPlaylistTrack("library", tracks -> {

            if (getView() != null){

                ((AlbumsView)getView()).setAlbums(MediaAssistant.getAlbums(tracks));
            }
        });
    }


}
