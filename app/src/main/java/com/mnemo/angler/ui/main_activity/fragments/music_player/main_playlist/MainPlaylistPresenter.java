package com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist;


import android.annotation.SuppressLint;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.AnglerDB;
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

    // Load playlist from database
    void loadPlaylist(String playlist){

        repository.loadPlaylistTrack(playlist, playlistTracks -> {

            tracks = playlistTracks;

            if (getView() != null) {
                applyFilter(((MainPlaylistView)getView()).getFilter());
            }
        });
    }

    // Apply filter to tracks
    @SuppressLint("CheckResult")
    void applyFilter(String filter) {

        if (filter.equals("")){

            ((MainPlaylistView) getView()).setTracks(tracks);

        }else {

            Observable.fromIterable(tracks)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(track -> (track.getTitle().toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(((MainPlaylistView) getView())::setTracks);
        }
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Update library
    void updateLibrary(){
        repository.updateLibrary(new AnglerDB.LibraryUpdateListener() {
            @Override
            public void libraryUpdated() {

                if (getView() != null){
                    ((MainPlaylistView)getView()).completeUpdate();
                }
            }
        });
    }
}
