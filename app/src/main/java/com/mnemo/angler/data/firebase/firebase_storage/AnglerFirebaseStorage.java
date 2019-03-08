package com.mnemo.angler.data.firebase.firebase_storage;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class AnglerFirebaseStorage {

    public interface OnBackgroundLoadListener {
        void onBackgrondLoaded(String background);
    }

    public AnglerFirebaseStorage() {

    }

    // Album cover
    public void uploadAlbumCover(String artist, Uri albumCoverPath){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("album cover").child(artist);

        StorageReference albumCoverStorageReference = storageReference.child(albumCoverPath.getLastPathSegment().replace("jpeg", "jpg"));

        albumCoverStorageReference.getDownloadUrl()
                .addOnFailureListener(e -> albumCoverStorageReference.putFile(albumCoverPath));

    }

    public void downloadAlbumCover(String artist, String album, Uri albumCoverPath){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("album cover").child(artist);

        storageReference.child(album + ".jpg").getFile(albumCoverPath);
    }


    // Artist image
    public void downloadArtistImage(String artist, Uri artistImagePath) {

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("artist image");

        storageReference.child(artist + ".jpg").getFile(artistImagePath)
                .addOnFailureListener(e -> {

                    StorageReference newArtistStorageReference = storageReference.child("_new artists").child(artist);

                    newArtistStorageReference.getDownloadUrl()
                            .addOnFailureListener(e1 -> newArtistStorageReference.putBytes(new byte[0]));
                });
    }

    // Backgrounds
    public void downloadBackground(String background, Uri backgroundImagePath){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("backgrounds");

        storageReference.child(background + ".jpg").getFile(backgroundImagePath);
    }

    public void downloadBackground(String background, Uri firstDefaultBackgroundImagePath, OnBackgroundLoadListener listener){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("backgrounds");

        storageReference.child(background + ".jpg").getFile(firstDefaultBackgroundImagePath)
                .addOnCompleteListener(task -> listener.onBackgrondLoaded(background))
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    listener.onBackgrondLoaded(background);
                });
    }
}
