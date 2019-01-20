package com.mnemo.angler.ui.main_activity.fragments.artists.artist_configuration;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;


public class ArtistConfigurationPresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private Disposable artistAlbumsDisposable;

    private List<Track> tracks;
    private List<Album> albums;

    ArtistConfigurationPresenter() {
        AnglerApp.getAnglerComponent().injectArtistConfigurationPresenter(this);
    }

    // Load artist tracks from database, get tracks and albums count
    void loadTracksAndAlbumsCount(String artist){

        setListener(repository.loadArtistTracksFromPlaylist("library", artist, tracks -> {

            this.tracks = tracks;

            artistAlbumsDisposable = repository.loadArtistAlbums(artist, albums -> {

                this.albums = albums;

                if (getView() != null){

                    ((ArtistConfigurationView)getView()).initializeTabs(tracks.size(), albums.size());
                    ((ArtistConfigurationView)getView()).fillCountViews(tracks.size(), albums.size());
                }
            });
        }));


    }

    // Get tracks
    public List<Track> getTracks() {
        return tracks;
    }

    // Get albums
    public List<Album> getAlbums() {
        return albums;
    }

    @Override
    public void deattachView() {
        super.deattachView();

        if (artistAlbumsDisposable != null){
            artistAlbumsDisposable.dispose();
        }
    }

    // Check bio
    boolean checkArtistBio(String artist){
        return repository.checkArtistBio(artist);
    }
}
