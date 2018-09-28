package com.mnemo.angler.ui.main_activity.fragments.music_player.music_player;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;


import javax.inject.Inject;

public class MusicPlayerPresenter extends BasePresenter {


    @Inject
    AnglerRepository repository;

    MusicPlayerPresenter() {

        AnglerApp.getAnglerComponent().injectMusicPlayerPresenter(this);
    }

    // load playlists from database
    void loadPlaylists(){
        repository.loadPlaylists(playlists -> {

            if (getView() != null) {

                ((MusicPlayerView) getView()).updateSpinner(playlists);
            }
        });
    }

    // update playlist
    void updateMainPlaylist(String playlist){
        repository.setMainPlaylist(playlist);
    }

}
