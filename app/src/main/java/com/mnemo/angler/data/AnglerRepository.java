package com.mnemo.angler.data;


import android.annotation.SuppressLint;
import android.net.Uri;

import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.database.AnglerDB;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.data.file_storage.AnglerFileStorage;
import com.mnemo.angler.data.file_storage.AnglerFolder;
import com.mnemo.angler.data.firebase.AnglerFirebase;
import com.mnemo.angler.data.firebase.firebase_database.AnglerFirebaseDatabase;
import com.mnemo.angler.data.networking.AnglerNetworking;
import com.mnemo.angler.data.preferences.AnglerPreferences;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AnglerRepository {

    public interface OnAppInitializationListener{
        void onAppInitialized(List<Track> tracks);
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
    AnglerFirebase anglerFirebase;

    private OnAppInitializationListener onAppInitializationListener;

    @SuppressLint("CheckResult")
    @Inject
    public AnglerRepository() {

        AnglerApp.getAnglerComponent().injectAnglerRepository(this);

        Completable.fromAction(() -> {
            anglerPreferences.initializePreferences();
            anglerFileStorage.createAppFolder();
            loadDefaultBackgrounds();

        }).subscribeOn(Schedulers.io())
                .subscribe();


    }

    // Networking methods
    // Load album covers and save them to file storage
    private void loadAlbumCovers(){

        anglerDB.loadAlbumsInBackground(albums -> {

            for (Album album : albums){
                if (!anglerFileStorage.isAlbumCoverExist(album.getArtist(), album.getAlbum())) {

                    anglerDB.loadAlbumTracks(album.getArtist(), album.getAlbum(), tracks1 -> Completable.fromAction(() -> {
                        for (Track track : tracks1) {

                            anglerFileStorage.createArtistAlbumsDirectory(track.getArtist());

                            InputStream inputStream1 = anglerFileStorage.extractAlbumImage(track.getUri());

                            if (inputStream1 != null){

                                Uri albumCoverUri = anglerFileStorage.saveAlbumCover(track.getArtist(), track.getAlbum(), inputStream1);

                                if (anglerNetworking.checkNetworkConnection()) {
                                    anglerFirebase.uploadAlbumCover(album.getArtist(), albumCoverUri);
                                }

                                return;
                            }else{

                                // Download album cover from firebase
                                anglerFirebase.downloadAlbumCover(album.getArtist(), album.getAlbum(), anglerFileStorage.getAlbumImageUri(album.getArtist(), album.getAlbum()));
                            }
                        }
                    }).subscribeOn(Schedulers.io()).subscribe());
                }
            }

        });
    }

    // Load missing album years
    private void loadAlbumYear(){

        if (anglerNetworking.checkNetworkConnection()) {

            anglerDB.loadAlbumsWithUnknownYear(albums -> {

                for (Album album : albums) {

                    anglerNetworking.loadAlbumYear(album.getArtist(), album.getAlbum(), year -> {
                        if (year != 10000) {
                            anglerDB.updateAlbumYear(album.get_id(), year);
                        }
                    });
                }
            });
        }
    }

    // Load artist images and bios and save them to file storage
    private void loadArtistImagesAndBios(){

        if (anglerNetworking.checkNetworkConnection()) {

            anglerDB.loadArtists("library", artists -> {

                for (String artist : artists) {

                    if (!anglerFileStorage.isArtistImageExist(artist)) {
                        anglerFirebase.downloadArtistImage(artist, anglerFileStorage.getArtistImageUri(artist));
                    }

                    if (!anglerFileStorage.isArtistBioExist(artist)) {
                        anglerNetworking.loadArtistBio(artist, bio -> anglerFileStorage.saveArtistBio(artist, bio));
                    }
                }
            });
        }
    }

    // Load missing album track positions
    private void loadTrackAlbumPosition(){

        if (anglerNetworking.checkNetworkConnection()) {

            anglerDB.getTrackWithUnknownAlbumPosition(tracks -> Completable.fromAction(() -> {

                AtomicBoolean isNewAlbumPositionAppear = new AtomicBoolean(false);

                for (Track track : tracks) {

                    anglerNetworking.loadTrackAlbumPosition(track.getTitle(), track.getArtist(), track.getAlbum(), albumPosition -> {

                        if (albumPosition != 10000) {
                            track.setAlbumPosition(albumPosition);

                            if (!isNewAlbumPositionAppear.get()) {
                                isNewAlbumPositionAppear.set(true);
                            }
                        }
                    });
                }

                if (isNewAlbumPositionAppear.get()) {
                    anglerDB.updateTracks(tracks);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe());
        }
    }

    // Load default backgrounds
    private void loadDefaultBackgrounds(){

        for (int i = 1; i <= 4; i++){

            if (!anglerFileStorage.isDefaultBackgroundExist("back" + i)) {

                anglerFirebase.downloadBackground("back" + i, anglerFileStorage.getDefaultBackgroundUriPort("back" + i), anglerFileStorage.getDefaultBackgroundUriLand("back" + i));
            }
        }
    }

    // Shared preferences methods
    public boolean getFirstLaunch(){
        return anglerPreferences.getFirstLaunch();
    }

    public void setFirstLaunch(boolean firstLaunch){
        anglerPreferences.setFirstLaunch(firstLaunch);
    }

    public long getTrialTimestamp(){
        return anglerPreferences.getTrialTimestamp();
    }

    public void setTrialTimestamp(long timestamp){
        anglerPreferences.setTrialTimestamp(timestamp);
    }

    public void syncTimestamps(String accountId, long timestamp, AnglerFirebaseDatabase.OnSyncTimeStampsListener listener){
        anglerFirebase.syncTimestamps(accountId, timestamp, listener);
    }

    public String getBackgroundImage(){

        return anglerPreferences.getBackgroundImage();
    }


    public void getBackgroundImage(AnglerFileStorage.OnBackgroundCheckListener listener){

        String backgroundImage = anglerPreferences.getBackgroundImage();

        if (!backgroundImage.contains("/.default/")) {
            if (!anglerFileStorage.isFileExist(AnglerFolder.PATH_BACKGROUND_PORTRAIT + File.separator + backgroundImage)) {
                backgroundImage = "/.default/" + "back1.jpeg";
                anglerPreferences.setBackgroundImage(backgroundImage);

                getBackgroundImage(listener);
            }else{
                listener.onBackgroundChecked(backgroundImage);
            }

        }else{

            String backgroundImageShort = backgroundImage.replace("/.default", "").replace(".jpeg", "");

            if (!anglerFileStorage.isDefaultBackgroundExist(backgroundImageShort)){
                anglerFirebase.downloadBackground(backgroundImageShort, anglerFileStorage.getDefaultBackgroundUriPort(backgroundImageShort), anglerFileStorage.getDefaultBackgroundUriLand(backgroundImageShort),
                        listener::onBackgroundChecked);
            }else{
                listener.onBackgroundChecked(backgroundImage);
            }
        }

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

    // Queue methods
    public String getQueueTitle(){
        return anglerPreferences.getQueueTitle();
    }

    public void setQueueTitle(String queueTitle){
        anglerPreferences.setQueueTitle(queueTitle);
    }

    public Set<String> getQueue(){
        return anglerPreferences.getQueue();
    }

    public void setQueue(HashSet<String> queue){
        anglerPreferences.setQueue(queue);
    }

    public int getQueueIndex(){
        return anglerPreferences.getQueueIndex();
    }

    public void setQueueIndex(int queueIndex){
        anglerPreferences.setQueueIndex(queueIndex);
    }

    public String getQueueFilter(){
        return anglerPreferences.getQueueFilter();
    }

    public void setQueueFilter(String queueFilter){
        anglerPreferences.setQueueFilter(queueFilter);
    }


    // Playback methods
    // Seekbar
    public String getCurrentTrack(){

        String currentTrack = anglerPreferences.getCurrentTrack();

        if (currentTrack != null) {

            String trackUri = currentTrack.split(":::")[5];

            if (!anglerFileStorage.isFileExist(trackUri)) {
                currentTrack = null;
            }
        }

        return currentTrack;
    }

    public void setCurrentTrack(String track){
        anglerPreferences.setCurrentTrack(track);
    }

    public int getSeekbarPosition(){
        return anglerPreferences.getSeekbarPosition();
    }

    public void setSeekbarPosition(int seekbarPosition){
        anglerPreferences.setSeekbarPosition(seekbarPosition);
    }

    // Repeat
    public int getRepeatMode(){
        return anglerPreferences.getRepeatMode();
    }

    public void setRepeatMode(int repeatMode){
        anglerPreferences.setRepeatMode(repeatMode);
    }

    // Shuffle
    public int getShuffleMode(){
        return anglerPreferences.getShuffleMode();
    }

    public void setShuffleMode(int shuffleMode){
        anglerPreferences.setShuffleMode(shuffleMode);
    }



    // File storage methods
    public void createTempImage(){
        anglerFileStorage.createTempImage();
    }

    public String getTempImageName(){
        return  anglerFileStorage.getTempImageName();
    }

    public void gatherBackgroundImages(AnglerFileStorage.OnGatherBackgroundImagesListener listener){

        anglerFileStorage.gatherBackgroundImages(listener);
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

    public void loadArtistBio(String artist, AnglerFileStorage.OnArtistBioLoadListener listener){
        anglerFileStorage.loadArtistBio(artist, listener);
    }

    public List<String> gatherImageFolders(){
        return anglerFileStorage.gatherImageFolders(AnglerFileStorage.PHONE_STORAGE);
    }

    public void getImages(String imageFolder, AnglerFileStorage.OnImageFolderLoadListener listener){
        anglerFileStorage.getImages(imageFolder, listener);
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

    public boolean checkArtistBio(String artist){
        return anglerFileStorage.checkArtistBio(artist);
    }

    public boolean checkAlbumCoverExist(String artist, String album){
        return anglerFileStorage.isAlbumCoverExist(artist, album);
    }

    public boolean checkArtistImageExist(String artist){
        return anglerFileStorage.isArtistImageExist(artist);
    }



    // Database methods
    @SuppressLint("CheckResult")
    public void updateLibrary(AnglerDB.LibraryUpdateListener listener){

        Completable.fromAction(() -> {

                    HashSet<Track> tracks = anglerFileStorage.scanTracks(AnglerFileStorage.PHONE_STORAGE);

                    String removableSDCardPath = anglerFileStorage.getRemovableSDCardPath();

                    if (removableSDCardPath != null){
                        tracks.addAll(anglerFileStorage.scanTracks(removableSDCardPath));
                    }

                    anglerDB.updateDatabase(tracks, () -> {

                        if (onAppInitializationListener != null){

                            onAppInitializationListener.onAppInitialized(anglerDB.loadLibrary());
                        }

                        loadAlbumCovers();
                        loadAlbumYear();
                        loadArtistImagesAndBios();
                        loadTrackAlbumPosition();

                    });


                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::libraryUpdated);
    }

    public void getLibraryTracksCount(AnglerDB.LibraryTracksCountListener listener){
        anglerDB.getLibraryTracksCount(listener);
    }

    // Playlists methods
    // Load playlists or/and titles
    public Disposable loadPlaylistTitles(AnglerDB.PlaylistsUpdateListener listener){
        return anglerDB.loadPlaylistTitles(listener);
    }

    public Disposable loadPlaylistsCreatedByUser(AnglerDB.UserPlaylistsUpdateListener listener){
        return anglerDB.loadPlaylistsCreatedByUser(listener);
    }

    public Disposable loadPlaylistsAndTitlesWithTrack(String trackId, AnglerDB.PlaylistsAndTitlesWithTrackLoadListener listener){
        return anglerDB.loadPlaylistsAndTitlesWithTrack(trackId, listener);
    }


    // Load playlist tracks methods
    public Disposable loadPlaylistTracks(String playlist, AnglerDB.PlaylistLoadListener listener){
        return anglerDB.loadPlaylistTracks(playlist, listener);
    }

    public Disposable loadCheckedPlaylistTracks(String playlist, AnglerDB.PlaylistCheckedTracksLoadListener listener){
        return anglerDB.loadCheckedPlaylistTracks(playlist, listener);
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

    public void updatePlaylistTracks(String playlist, List<Track> tracks){
        anglerDB.deleteAllTracksFromPlaylist(playlist, () -> anglerDB.addTracksToPlaylist(playlist, tracks));
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

    public void clearPlaylist(String playlist){
        anglerDB.deleteAllTracksFromPlaylist(playlist);
    }


    // Artist methods
    public Disposable loadArtists(String playlist, AnglerDB.ArtistsLoadListener listener){
        return anglerDB.loadArtists(playlist, listener);
    }

    public Disposable loadArtistTracksFromPlaylist(String playlist, String artist, AnglerDB.ArtistTracksLoadListener listener){
        return anglerDB.loadArtistTracksFromPlaylist(playlist, artist, listener);
    }


    // Albums methods
    public Disposable loadAlbums(AnglerDB.AlbumsLoadListener listener){
        return anglerDB.loadAlbums(listener);
    }

    public Disposable loadArtistAlbums(String artist, AnglerDB.ArtistAlbumsLoadListener listener){
         return anglerDB.loadArtistAlbums(artist, listener);
    }

    public Disposable loadAlbumTracks(String artist, String album, AnglerDB.AlbumTracksLoadListener listener){
        return anglerDB.loadAlbumTracks(artist, album, listener);
    }

    public void loadAlbumYear(String artist, String album, AnglerDB.OnAlbumYearLoadListener listener){
        anglerDB.loadAlbumYear(artist, album, listener);
    }




    // Listeners
    public void setOnAppInitializationListener(OnAppInitializationListener onAppInitializationListener) {
        this.onAppInitializationListener = onAppInitializationListener;
    }
}
