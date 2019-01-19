package com.mnemo.angler.ui.main_activity.misc.add_track_to_playlist;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;
import com.mnemo.angler.ui.base.DisposableBasePresenter;


import javax.inject.Inject;


public class AddTrackToPlaylistPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    AddTrackToPlaylistPresenter() {
        AnglerApp.getAnglerComponent().injectAddTrackToPlaylistPresenter(this);
    }

    // Load playlists and playlist titles which contain this track
    void loadPlaylistsAndTitlesWithTrack(String trackId) {
        setListener(repository.loadPlaylistsAndTitlesWithTrack(trackId, (playlists, playlistsWithTrack) -> {

            if (getView() != null) {

                ((AddTrackToPlaylistView)getView()).setPlaylists(playlists, playlistsWithTrack);
            }
        }));
    }

    // Add track to playlist
    void addTrackToPlaylist(String playlist, Track track){
        repository.addTrackToPlaylist(playlist, track);
    }

}
