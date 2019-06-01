package com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist;


import android.annotation.SuppressLint;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MainPlaylistPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    MainPlaylistPresenter() {

        AnglerApp.getAnglerComponent().injectMainPlaylistPresenter(this);
    }

    // Check app first launch state
    boolean checkAppFirstLaunchState(){
        return repository.getFirstLaunch();
    }

    void saveAppFirstLaunchState(boolean appFirstLaunchState){
        repository.setFirstLaunch(appFirstLaunchState);
    }


    // Load playlist from database
    void loadPlaylist(String playlist){

        setListener(repository.loadPlaylistTracks(playlist, playlistTracks -> {

            tracks = playlistTracks;

            if (getView() != null) {
                applyFilter(((MainPlaylistView)getView()).getFilter());
            }
        }));
    }

    // Load folder tracks
    void loadFolderTracks(String folder){

        setListener(repository.loadFolderTracks(folder, folderTracks -> {

            tracks = folderTracks;

            if (getView() != null) {
                applyFilter(((MainPlaylistView)getView()).getFilter());
            }
        }));
    }

    // Apply filter to tracks
    @SuppressLint("CheckResult")
    void applyFilter(String filter) {

        if (filter.equals("")){

            if (getView() != null) {
                ((MainPlaylistView) getView()).setTracks(tracks);
            }

        }else {

            Observable.fromIterable(tracks)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(track -> (track.getTitle().toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(tracks -> {

                        if (getView() != null) {
                            ((MainPlaylistView) getView()).setTracks(tracks);
                        }
                    });
        }
    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Update library
    void updateLibrary(){
        repository.updateLibrary(() -> {

            if (getView() != null){
                ((MainPlaylistView)getView()).completeUpdate();
            }
        });
    }
}
