package com.mnemo.angler.ui.main_activity.fragments.music_player.artists;


import android.annotation.SuppressLint;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlaylistArtistsPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<String> artists;

    PlaylistArtistsPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistArtistsPresenter(this);
    }

    // load playlist artists from database
    void loadArtists(String playlist){

        setListener(repository.loadArtists(playlist, playlistArtists -> {

            artists = playlistArtists;

            if (getView() != null) {
                applyFilter(((PlaylistArtistsFragment)getView()).getFilter());
            }
        }));
    }

    // apply filter to artists
    @SuppressLint("CheckResult")
    void applyFilter(String filter){

        if (filter.equals("")){

            if (getView() != null) {
                ((PlaylistArtistsView) getView()).setArtists(artists);
            }

        }else {

            Observable.fromIterable(artists)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(artist -> (artist.toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(artists -> {

                        if (getView() != null) {
                            ((PlaylistArtistsView) getView()).setArtists(artists);
                        }
                    });


        }
    }
}
