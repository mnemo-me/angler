package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

public class PlaylistConfigurationPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    PlaylistConfigurationPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistConfigurationPresenter(this);
    }

    // Load playlist tracks
    void loadPlaylistTracks(String playlist){
        setListener(repository.loadPlaylistTracks(playlist, tracks -> {

            if (getView() != null){

                this.tracks = tracks;

                ((PlaylistConfigurationView)getView()).setPlaylistTracks(tracks);
            }
        }));
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Get library tracks count
    public void checkLibraryTracksCount(){

        repository.getLibraryTracksCount(count -> {

            ((PlaylistConfigurationView)getView()).setAddTracksAvailable(count != 0);
        });
    }
}
