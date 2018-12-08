package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_clear;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class PlaylistClearPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    PlaylistClearPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistClearPresenter(this);
    }

    // Clear playlist
    void clearPlaylist(String playlist){
        repository.clearPlaylist(playlist);
    }
}
