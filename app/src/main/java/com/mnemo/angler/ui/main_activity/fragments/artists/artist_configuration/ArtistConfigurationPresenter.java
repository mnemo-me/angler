package com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration;

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


public class ArtistConfigurationPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;

    ArtistConfigurationPresenter() {
        AnglerApp.getAnglerComponent().injectArtistConfigurationPresenter(this);
    }

    // Load artist tracks from database, get tracks and albums count
    void loadTracksAndAlbumsCount(String artist){

        repository.loadArtistTracksFromPlaylist("library", artist, tracks -> {

            if (getView() != null){

                this.tracks = tracks;

                ((ArtistConfigurationView)getView()).initializeTabs(tracks.size(), getAlbums(tracks).size());
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

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }
}
