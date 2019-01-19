package com.mnemo.angler.ui.main_activity.fragments.playlists.playlists;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import javax.inject.Inject;

public class PlaylistsPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    PlaylistsPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistsPresenter(this);
    }

    // Load playlists from database (excl. library)
    void loadPlaylists(){
        setListener(repository.loadPlaylistsCreatedByUser(playlists -> {

           if (getView() != null){

               ((PlaylistsView)getView()).setPlaylists(playlists);
           }
        }));
    }
}
