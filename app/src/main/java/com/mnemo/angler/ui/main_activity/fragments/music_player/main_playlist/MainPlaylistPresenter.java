package com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MainPlaylistPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    MainPlaylistPresenter() {

        AnglerApp.getAnglerComponent().injectMainPlaylistPresenter(this);
    }

    // load playlist from database
    void loadPlaylist(String playlist){

        repository.loadPlaylistTrack(playlist, playlistTracks -> {

            tracks = playlistTracks;

            if (getView() != null) {
                applyFilter(((MainPlaylistView)getView()).getFilter());
            }
        });
    }

    // apply filter to tracks
    void applyFilter(String filter) {

        if (filter.equals("")){

            ((MainPlaylistView)getView()).setTracks(tracks);

        }else {

            Observable.fromIterable(tracks)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(track -> (track.getTitle().toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(tracks -> ((MainPlaylistView)getView()).setTracks(tracks));

        }
    }

}
