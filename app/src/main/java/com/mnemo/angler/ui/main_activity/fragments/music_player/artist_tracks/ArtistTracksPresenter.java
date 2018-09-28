package com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ArtistTracksPresenter extends BasePresenter{

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    ArtistTracksPresenter() {

        AnglerApp.getAnglerComponent().injectArtistTracksPresenter(this);
    }

    // load artist tracks from database
    void loadArtistTracksFromPlaylist(String playlist, String artist){

        repository.loadArtistTracksFromPlaylist(playlist, artist, artistTracks -> {

            tracks = artistTracks;

            if (getView() != null) {
                applyFilter(((ArtistTracksFragment)getView()).getFilter());
            }
        });
    }

    // apply filter to tracks
    void applyFilter(String filter) {

        if (filter.equals("")){

            ((ArtistTracksView)getView()).setTracks(tracks);

        }else {

            Observable.fromIterable(tracks)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(track -> (track.getTitle().toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(tracks -> ((ArtistTracksView)getView()).setTracks(tracks));
        }
    }
}
