package com.mnemo.angler.ui.main_activity.misc.contextual_menu;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import javax.inject.Inject;

public class ContextualMenuPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ContextualMenuPresenter() {
        AnglerApp.getAnglerComponent().injectContextualMenuPresenter(this);
    }

    // Get artist image path
    String getArtistImagePath(String artist){
        return repository.getArtistImagePath(artist);
    }

    // Get album image path
    String getAlbumImagePath(String artist, String album){
        return repository.getAlbumImagePath(artist, album);
    }

    // Check album cover exist
    boolean checkAlbumCoverExist(String artist, String album){
        return repository.checkAlbumCoverExist(artist, album);
    }

    // Delete track from playlist
    void deleteTrack(String playlist, String trackId){
        repository.deleteTrack(playlist, trackId, (position) -> {

            if (getView() != null){
                ((ContextualMenuView)getView()).showDeleteTrackSnackbar(playlist, trackId, position);
            }
        });
    }

    // Restore track
    void restoreTrack(String playlist, String trackId, int position){
        repository.restoreTrack(playlist, trackId, position);
    }
}
