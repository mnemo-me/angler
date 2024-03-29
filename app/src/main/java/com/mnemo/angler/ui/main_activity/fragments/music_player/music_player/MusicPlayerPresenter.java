package com.mnemo.angler.ui.main_activity.fragments.music_player.music_player;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;


import javax.inject.Inject;

public class MusicPlayerPresenter extends DisposableBasePresenter {


    @Inject
    AnglerRepository repository;

    MusicPlayerPresenter() {

        AnglerApp.getAnglerComponent().injectMusicPlayerPresenter(this);
    }

    // Load playlists from database
    void loadPlaylists(){
        setListener(repository.loadPlaylistTitlesAndFolderLinks(playlistTitles -> {

            if (getView() != null) {

                ((MusicPlayerView) getView()).updateSpinner(playlistTitles);
            }
        }));
    }

    // Update playlist
    void updateMainPlaylist(String playlist){
        repository.setMainPlaylist(playlist);
    }

}
