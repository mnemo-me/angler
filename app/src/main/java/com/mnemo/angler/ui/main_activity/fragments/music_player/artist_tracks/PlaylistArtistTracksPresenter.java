package com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks;


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

public class PlaylistArtistTracksPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    PlaylistArtistTracksPresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistArtistTracksPresenter(this);
    }

    // Load artist tracks from database
    void loadArtistTracksFromPlaylist(String playlist, String artist){

        setListener(repository.loadArtistTracksFromPlaylist(playlist, artist, artistTracks -> {

            tracks = artistTracks;

            if (getView() != null) {
                applyFilter(((PlaylistArtistTracksFragment)getView()).getFilter());
            }
        }));
    }

    void loadArtistTracksFromFolder(String folder, String artist){

        setListener(repository.loadArtistTracksFromFolder(folder, artist, artistTracks -> {

            tracks = artistTracks;

            if (getView() != null) {
                applyFilter(((PlaylistArtistTracksFragment)getView()).getFilter());
            }
        }));
    }

    // Apply filter to tracks
    @SuppressLint("CheckResult")
    void applyFilter(String filter) {

        if (filter.equals("")){

            if (getView() != null) {
                ((PlaylistArtistTracksView) getView()).setTracks(tracks);
            }

        }else {

            Observable.fromIterable(tracks)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(track -> (track.getTitle().toLowerCase().contains(filter.toLowerCase())))
                    .toList()
                    .subscribe(tracks -> {

                        if (getView() != null) {
                            ((PlaylistArtistTracksView) getView()).setTracks(tracks);
                        }
                    });
        }
    }
}
