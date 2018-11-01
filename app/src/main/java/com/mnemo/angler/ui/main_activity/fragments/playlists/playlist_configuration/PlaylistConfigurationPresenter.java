package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

public class PlaylistConfigurationPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    PlaylistConfigurationPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistConfigurationPresenter(this);
    }

    // Load playlist tracks
    void loadPlaylistTracks(String playlist){
        repository.loadPlaylistTrack(playlist, tracks -> {

            if (getView() != null){

                this.tracks = tracks;

                ((PlaylistConfigurationView)getView()).setPlaylistTracks(tracks);
            }
        });
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }
}
