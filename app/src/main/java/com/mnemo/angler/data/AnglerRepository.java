package com.mnemo.angler.data;


import android.net.Uri;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.database.AnglerDB;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.data.file_storage.AnglerFileStorage;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.data.networking.AnglerNetworking;
import com.mnemo.angler.data.preferences.AnglerPreferences;
import com.mnemo.angler.ui.main_activity.classes.Album;
import com.mnemo.angler.util.MediaAssistant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

public class AnglerRepository {

    public interface OnGatherBackgroundImagesListener{
        void backgroundImagesGathered(List<String> images);
    }

    @Inject
    AnglerDB anglerDB;

    @Inject
    AnglerFileStorage anglerFileStorage;

    @Inject
    AnglerNetworking anglerNetworking;

    @Inject
    AnglerPreferences anglerPreferences;

    @Inject
    public AnglerRepository() {

        AnglerApp.getAnglerComponent().injectAnglerRepository(this);

        Completable.fromAction(() -> {
            anglerPreferences.initializePreferences();
            anglerFileStorage.createAppFolder();

            ArrayList<Track> tracks = anglerFileStorage.scanTracks(AnglerFileStorage.PHONE_STORAGE);
            anglerDB.updateDatabase(tracks);

        }).subscribeOn(Schedulers.io()).subscribe(() -> {

            loadAlbumCovers();
            loadArtistImagesAndBios();
        });


    }

    // Networking methods
    // Load album covers and save them to file storage
    private void loadAlbumCovers(){

        anglerDB.loadPlaylistTracks("library", tracks -> {
            List<Album> albums = MediaAssistant.getAlbums(tracks);

            for (Album album : albums){
                if (!anglerFileStorage.isAlbumCoverExist(album.getArtist(), album.getAlbum())) {
                    anglerNetworking.loadAlbum(album.getArtist(), album.getAlbum(),
                            inputStream -> anglerFileStorage.saveAlbumCover(album.getArtist(), album.getAlbum(), inputStream));
                }
            }
        });
    }

    // Load artist images and bios and save them to file storage
    private void loadArtistImagesAndBios(){

        anglerDB.loadArtists("library", artists -> {

            for (String artist : artists){

                if (!anglerFileStorage.isArtistImageExist(artist)) {
                    anglerNetworking.loadArtistImage(artist, inputStream -> anglerFileStorage.saveArtistImage(artist, inputStream));
                }

                if (!anglerFileStorage.isArtistBioExist(artist)) {
                    anglerNetworking.loadArtistBio(artist, bio -> anglerFileStorage.saveArtistBio(artist, bio));
                }
            }
        });
    }

    // Refresh artists images
    public void refreshArtistImages(){

        anglerDB.loadArtists("library", artists -> {

            for (String artist : artists){

                anglerNetworking.loadArtistImage(artist, inputStream -> anglerFileStorage.saveArtistImage(artist, inputStream));
            }
        });
    }



    // Shared preferences methods
    public String getBackgroundImage(){

       String backgroundImage = anglerPreferences.getBackgroundImage();

        if (!backgroundImage.startsWith("R.drawable.")) {
            if (!anglerFileStorage.isFileExist(AnglerFolder.PATH_BACKGROUND_PORTRAIT + File.separator + backgroundImage)) {
                backgroundImage = "R.drawable.back";
                anglerPreferences.setBackgroundImage(backgroundImage);
            }
        }

       return backgroundImage;

    }

    public int getBackgroundOpacity(){
        return anglerPreferences.getBackgroundOpacity();
    }

    public void saveBackground(String backgroundImage, int opacity){
        anglerPreferences.setBackgroundImage(backgroundImage);
        anglerPreferences.setBackgroundOpacity(opacity);
    }


    public void setMainPlaylist(String playlist){
        anglerPreferences.setMainPlaylist(playlist);
    }

    // Equalizer methods
    public boolean getEqualizerState(){
        return anglerPreferences.getEqualizerState();
    }

    public void saveEqualizerState(boolean equalizerState){
        anglerPreferences.setEqualizerState(equalizerState);
    }

    public int getEqualizerPreset(){
        return anglerPreferences.getEqualizerPreset();
    }

    public void setEqualizerPreset(int preset){
        anglerPreferences.setEqualizerPreset(preset);
    }

    public List<Short> getBandsLevel(int bandsCount){
        return anglerPreferences.getBandsLevel(bandsCount);
    }

    public void setBandsLevel(List<Short> bandsLevel){
        anglerPreferences.setBandsLevel(bandsLevel);
    }

    // Virtualizer
    public boolean getVirtualizerState(){
        return anglerPreferences.getVirtualizerState();
    }

    public void setVirtualizerState(boolean virtualizerState){
        anglerPreferences.setVirtualizerState(virtualizerState);
    }

    public int getVirtualizerStrength(){
        return anglerPreferences.getVirtualizerStrength();
    }

    public void setVirtualizerStrength(int virtualizerStrength){
        anglerPreferences.setVirtualizerStrength(virtualizerStrength);
    }

    // Bass boost
    public boolean getBassBoostState(){
        return anglerPreferences.getBassBoostState();
    }

    public void setBassBoostState(boolean bassBoostState){
        anglerPreferences.setBassBoostState(bassBoostState);
    }

    public int getBassBoostStrength(){
        return anglerPreferences.getBassBoostStrength();
    }

    public void setBassBoostStrength(int bassBoostStrength){
        anglerPreferences.setBassBoostStrength(bassBoostStrength);
    }

    // Amplifier
    public boolean getAmplifierState(){
        return anglerPreferences.getAmplifierState();
    }

    public void setAmplifierState(boolean amplifierState){
        anglerPreferences.setAmplifierState(amplifierState);
    }

    public int getAmplifierGain(){
        return anglerPreferences.getAmplifierGain();
    }

    public void setAmplifierGain(int amplifierGain){
        anglerPreferences.setAmplifierGain(amplifierGain);
    }



    // File storage methods
    public void createTempImage(){
        anglerFileStorage.createTempImage();
    }

    public String getTempImageName(){
        return  anglerFileStorage.getTempImageName();
    }

    public void gatherBackgroundImages(OnGatherBackgroundImagesListener listener){

        // Create list of images
        List<String> images = new ArrayList<>();

        // Add background images from file storage
        images.addAll(anglerFileStorage.gatherBackgroundImages());

        // Add default images to list
        images.add("R.drawable.back");
        images.add("R.drawable.back2");
        images.add("R.drawable.back3");
        images.add("R.drawable.back4");

        listener.backgroundImagesGathered(images);
    }

    public void deleteBackgroundImage(String image){
        anglerFileStorage.deleteBackgroundImage(image);
    }

    public String getArtistImagePath(String artist){
        return anglerFileStorage.getArtistImagePath(artist);
    }

    public String getAlbumImagePath(String artist, String album){
        return anglerFileStorage.getAlbumImagePath(artist, album);
    }

    public String loadArtistBio(String artist){
        return anglerFileStorage.loadArtistBio(artist);
    }

    public List<String> gatherImageFolders(){
        return anglerFileStorage.gatherImageFolders(AnglerFileStorage.PHONE_STORAGE);
    }

    public ArrayList<String> getImages(String imageFolder){
        return anglerFileStorage.getImages(imageFolder);
    }

    public String generateNewBackgroundImageName(String image){
        return anglerFileStorage.generateNewImageName(image);
    }

    public Uri getImageUri(String imageFileName, int orientation){
        return anglerFileStorage.getImageUri(imageFileName, orientation);
    }

    public Uri getTempCoverUri(){
        return anglerFileStorage.getTempCoverUri();
    }



    // Database methods
    // Playlists methods
    // Load playlists or/and titles
    public void loadPlaylistTitles(AnglerDB.PlaylistsUpdateListener listener){
        anglerDB.loadPlaylistTitles(listener);
    }

    public void loadPlaylistsCreatedByUser(AnglerDB.UserPlaylistsUpdateListener listener){
        anglerDB.loadPlaylistsCreatedByUser(listener);
    }

    public void loadPlaylistsAndTitlesWithTrack(String trackId, AnglerDB.PlaylistsAndTitlesWithTrackLoadListener listener){
        anglerDB.loadPlaylistsAndTitlesWithTrack(trackId, listener);
    }


    // Load playlist tracks methods
    public void loadPlaylistTrack(String playlist, AnglerDB.PlaylistLoadListener listener){
        anglerDB.loadPlaylistTracks(playlist, listener);
    }

    public void loadCheckedPlaylistTracks(String playlist, AnglerDB.PlaylistCheckedTracksLoadListener listener){
        anglerDB.loadCheckedPlaylistTracks(playlist, listener);
    }


    // Create, Delete playlist methods
    public void createPlaylist(String playlist){

        String coverImageName = anglerFileStorage.generatePlaylistCoverImageName(playlist);

        anglerDB.insertPlaylist(playlist, coverImageName);
        anglerFileStorage.copyImage(anglerFileStorage.getTempImageName(), coverImageName);
    }

    public void deletePlaylist(String playlist){

        anglerDB.deletePlaylist(playlist);
        anglerFileStorage.deleteCoverImage(playlist);
    }


    // Update playlist methods
    public void renamePlaylist(String oldTitle, String newTitle){

        String oldImageName = anglerFileStorage.generatePlaylistCoverImageName(oldTitle);
        String newImageName = anglerFileStorage.generatePlaylistCoverImageName(newTitle);

        anglerFileStorage.renameCover(oldImageName, newImageName);

        anglerDB.updatePlaylist(oldTitle, newTitle, newImageName);
        anglerDB.updatePlaylistLink(oldTitle, newTitle);
    }

    public void updateCover(String image){

        anglerFileStorage.copyImage(anglerFileStorage.getTempImageName(), image);
    }


    // Add tracks to playlist methods
    public void addTracksToPlaylist(String playlist, HashMap<Track, Integer> tracks){
        anglerDB.addTracksToPlaylist(playlist, tracks);
    }

    public void addTrackToPlaylist(String playlist, Track track){
        anglerDB.addTrackToPlaylist(playlist, track);
    }


    // Delete/Restore track in playlist methods
    public void deleteTrack(String playlist, String trackId, AnglerDB.TrackDeleteListener listener){
        anglerDB.deleteTrackFromPlaylist(playlist, trackId, listener);
    }

    public void restoreTrack(String playlist, String trackId, int position){
        anglerDB.restoreTrackInPlaylist(playlist, trackId, position);
    }


    // Artist methods
    public void loadArtists(String playlist, AnglerDB.ArtistsLoadListener listener){
        anglerDB.loadArtists(playlist, listener);
    }

    public void loadArtistTracksFromPlaylist(String playlist, String artist, AnglerDB.ArtistTracksLoadListener listener){
        anglerDB.loadArtistTracksFromPlaylist(playlist, artist, listener);
    }


    // Albums methods
    public void loadAlbumTracks(String artist, String album, AnglerDB.AlbumTracksLoadListener listener){
        anglerDB.loadAlbumTracks(artist, album, listener);
    }

}