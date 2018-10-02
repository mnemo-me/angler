package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class PlaylistConfigurationPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    PlaylistConfigurationPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistConfigurationPresenter(this);
    }

    // Load playlist tracks
    void loadPlaylistTracks(String playlist){
        repository.loadPlaylist(playlist, tracks -> {

            if (getView() != null){
                ((PlaylistConfigurationView)getView()).setPlaylistTracks(tracks);
            }
        });
    }
}
