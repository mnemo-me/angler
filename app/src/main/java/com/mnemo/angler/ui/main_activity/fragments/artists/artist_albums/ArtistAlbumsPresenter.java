package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;
import com.mnemo.angler.ui.main_activity.classes.Album;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;


public class ArtistAlbumsPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    ArtistAlbumsPresenter() {
        AnglerApp.getAnglerComponent().injectArtistAlbumsPresenter(this);
    }

    void loadArtistAlbums(String artist){

        repository.loadArtistTracksFromPlaylist("library", artist, tracks -> {

            if (getView() != null){
                ((ArtistAlbumsView)getView()).setArtistAlbums(getAlbums(tracks));
            }
        });
    }

    // Extract albums from tracks
    private List<Album> getAlbums(List<Track> tracks){

        List<Album> albums = new ArrayList<>();
        Set<String> albumTitles = new HashSet<>();

        for (Track track : tracks){

            if (!albumTitles.contains(track.getAlbum())){

                albumTitles.add(track.getAlbum());
                albums.add(new Album(track.getAlbum(), track.getArtist()));
            }
        }

        return albums;
    }
}