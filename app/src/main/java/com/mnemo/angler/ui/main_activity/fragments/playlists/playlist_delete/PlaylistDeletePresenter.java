package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_delete;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class PlaylistDeletePresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    PlaylistDeletePresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistDeletePresenter(this);
    }

    // Delete playlist from database and cover image from file storage
    void deletePlaylist(String playlist){
        repository.deletePlaylist(playlist);
    }
}
