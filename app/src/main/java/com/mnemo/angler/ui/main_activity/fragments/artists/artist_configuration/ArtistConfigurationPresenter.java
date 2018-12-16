package com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.BasePresenter;

import java.util.List;

import javax.inject.Inject;


public class ArtistConfigurationPresenter extends BasePresenter {

    @Inject
    AnglerRepository repository;

    private List<Track> tracks;
    private List<Album> albums;

    ArtistConfigurationPresenter() {
        AnglerApp.getAnglerComponent().injectArtistConfigurationPresenter(this);
    }

    // Load artist tracks from database, get tracks and albums count
    void loadTracksAndAlbumsCount(String artist){

        repository.loadArtistTracksFromPlaylist("library", artist, tracks -> {

            this.tracks = tracks;

            repository.loadArtistAlbums(artist, albums -> {

                this.albums = albums;

                if (getView() != null){

                    ((ArtistConfigurationView)getView()).initializeTabs(tracks.size(), albums.size());
                    ((ArtistConfigurationView)getView()).fillCountViews(tracks.size(), albums.size());
                }
            });
        });


    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Get albums

    public List<Album> getAlbums() {
        return albums;
    }
}
