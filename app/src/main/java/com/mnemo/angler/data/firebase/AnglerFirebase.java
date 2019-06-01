package com.mnemo.angler.data.firebase;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.firebase.firebase_storage.AnglerFirebaseStorage;

import javax.inject.Inject;


public class AnglerFirebase {

    @Inject
    AnglerFirebaseStorage anglerFirebaseStorage;

    private FirebaseAuth firebaseAuth;

    @Inject
    public AnglerFirebase() {

        AnglerApp.getAnglerComponent().injectAnglerFirebase(this);
        connect();
    }

    // Manage connection
    private void connect(){

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInAnonymously();
    }

    private void disconnect(){
        firebaseAuth.signOut();
    }


    // Album cover
    public void uploadAlbumCover(String artist, Uri albumCoverUri){
        anglerFirebaseStorage.uploadAlbumCover(artist, albumCoverUri);
    }

    public void downloadAlbumCover(String artist, String album, Uri albumCoverPath){
        anglerFirebaseStorage.downloadAlbumCover(artist, album, albumCoverPath);
    }


    // Artist image
    public void downloadArtistImage(String artist, Uri artistImagePath){
        anglerFirebaseStorage.downloadArtistImage(artist, artistImagePath);
    }

    // Background
    public void downloadBackground(String background, Uri backgroundImagePathPort, Uri backgroundImagePathLand){
        anglerFirebaseStorage.downloadBackground(background, backgroundImagePathPort, backgroundImagePathLand);
    }

    public void downloadBackground(String background, Uri firstDefaultBackgroundImagePathPort, Uri firstDefaultBackgroundImagePathLand, AnglerFirebaseStorage.OnBackgroundLoadListener listener){
        anglerFirebaseStorage.downloadBackground(background, firstDefaultBackgroundImagePathPort, firstDefaultBackgroundImagePathLand, listener);
    }
}
