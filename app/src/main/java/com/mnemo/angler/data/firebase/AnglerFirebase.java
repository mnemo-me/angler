package com.mnemo.angler.data.firebase;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.firebase.firebase_database.AnglerFirebaseDatabase;
import com.mnemo.angler.data.firebase.firebase_storage.AnglerFirebaseStorage;

import javax.inject.Inject;


public class AnglerFirebase {

    @Inject
    AnglerFirebaseDatabase anglerFirebaseDB;

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


    // Check trial period
    public void syncTimestamps(String accountId, long timestamp, AnglerFirebaseDatabase.OnSyncTimeStampsListener listener) {
        anglerFirebaseDB.syncTimestamps(accountId, timestamp, listener);
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
}
