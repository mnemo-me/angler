package com.mnemo.angler.ui.main_activity.fragments.playlists.manage_tracks;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

public class ManageTracksPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    ManageTracksPresenter() {
        AnglerApp.getAnglerComponent().injectManageTracksPresenter(this);
    }

    // Load tracks from database
    void loadTracks(String playlist){
        setListener(repository.loadPlaylistTracks(playlist, tracks -> {

            if (getView() != null){
                ((ManageTracksView)getView()).setTracks(tracks);
            }
        }));
    }

    // Save tracks
    void saveTracks(String playlist, List<Track> tracks){
        repository.updatePlaylistTracks(playlist, tracks);
    }

}
