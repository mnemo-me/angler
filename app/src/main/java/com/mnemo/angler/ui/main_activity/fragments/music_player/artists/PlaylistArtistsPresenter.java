package com.mnemo.angler.ui.main_activity.fragments.music_player.artists;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PlaylistArtistsPresenter extends BasePresenter{

    @Inject
    AnglerRepository repository;

    private List<String> artists;

    PlaylistArtistsPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistArtistsPresenter(this);
    }

    // load playlist artists from database
    void loadArtists(String playlist){

        repository.loadArtists(playlist, playlistArtists -> {

            artists = playlistArtists;

            if (getView() != null) {
                applyFilter(((PlaylistArtistsFragment)getView()).getFilter());
            }
        });
    }

    // apply filter to artists
    void applyFilter(String filter){

        if (filter.equals("")){

            ((PlaylistArtistsView)getView()).setArtists(artists);

        }else {

            Observable.fromIterable(artists)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(artist -> (artist.toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(artists -> ((PlaylistArtistsView)getView()).setArtists(artists));

        }
    }
}
