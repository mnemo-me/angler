package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.List;

import javax.inject.Inject;


public class PlaylistCreatePresenter extends DisposableBasePresenter {

    @Inject
    AnglerRepository repository;

    private List<String> playlistTitles;

    PlaylistCreatePresenter() {

        AnglerApp.getAnglerComponent().injectPlaylistCreatePresenter(this);
        loadPlaylistTitles();
    }


    // Load already created playlist titles
    private void loadPlaylistTitles(){
        setListener(repository.loadPlaylistTitles(playlists -> playlistTitles = playlists));
    }

    // Create new playlist
    void createPlaylist(String title){
        repository.createPlaylist(title);
    }

    // Get temp image name
    String getTempImageName(){
        return repository.getTempImageName();
    }

    // Create temp image
    void createTempImage(){
        repository.createTempImage();
    }

    // Change playlist title and cover name in database
    void renamePlaylist(String oldTitle, String newTitle){
        repository.renamePlaylist(oldTitle, newTitle);
    }

    // Update cover in file storage
    void updateCover(String cover){
        repository.updateCover(cover);
    }

    // Check is playlist name already used
    boolean checkPlaylistNameIsAlreadyUsed(String playlist){
        return playlistTitles.contains(playlist);
    }

    // Add track to new playlist
    void addTrackToPlaylist(String playlist, Track track){
        repository.addTrackToPlaylist(playlist, track);
    }
}
