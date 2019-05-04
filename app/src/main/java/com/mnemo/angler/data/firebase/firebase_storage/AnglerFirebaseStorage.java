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
        StorageReference storageReference = firebaseStorage.getReference().child("album cover");

        StorageReference albumCoverStorageReference = storageReference.child(artist + ":::" + albumCoverPath.getLastPathSegment().replace("jpeg", "jpg"));

        albumCoverStorageReference.getDownloadUrl()
                .addOnFailureListener(e1 -> {

                    StorageReference blAlbumCoverStorageReference = storageReference.child("_bl").child(artist + ":::" + albumCoverPath.getLastPathSegment().replace("jpeg", "jpg"));

                    blAlbumCoverStorageReference.getDownloadUrl()
                            .addOnFailureListener(e2 -> {

                                StorageReference modAlbumCoverStorageReference = storageReference.child("_mod").child(artist + ":::" + albumCoverPath.getLastPathSegment().replace("jpeg", "jpg"));

                                modAlbumCoverStorageReference.getDownloadUrl()
                                        .addOnFailureListener(e3 -> modAlbumCoverStorageReference.putFile(albumCoverPath));
                            });
                });

    }

    public void downloadAlbumCover(String artist, String album, Uri albumCoverPath){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("album cover");

        storageReference.child(artist + ":::" + album + ".jpg").getFile(albumCoverPath);
    }


    // Artist image
    public void downloadArtistImage(String artist, Uri artistImagePath) {

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("artist image");

        storageReference.child(artist + ".jpg").getFile(artistImagePath)
                .addOnFailureListener(e -> {

                    StorageReference blArtistStorageReference = storageReference.child("_bl").child(artist + ".jpg");

                    blArtistStorageReference.getDownloadUrl()
                            .addOnFailureListener(e1 -> {

                                StorageReference newArtistStorageReference = storageReference.child("_new artists").child(artist + ".jpg");

                                newArtistStorageReference.getDownloadUrl()
                                        .addOnFailureListener(e2 -> newArtistStorageReference.putBytes(new byte[0]));
                            });
                });
    }

    // Backgrounds
    public void downloadBackground(String background, Uri backgroundImagePathPort, Uri backgroundImagePathLand){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("backgrounds");

        storageReference.child("port").child(background + ".jpeg").getFile(backgroundImagePathPort);
        storageReference.child("land").child(background + ".jpeg").getFile(backgroundImagePathLand);
    }

    public void downloadBackground(String background, Uri firstDefaultBackgroundImagePathPort, Uri firstDefaultBackgroundImagePathLand, OnBackgroundLoadListener listener){

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("backgrounds");

        storageReference.child("port").child(background + ".jpeg").getFile(firstDefaultBackgroundImagePathPort)
                .addOnCompleteListener(task -> storageReference.child("land").child(background + ".jpeg").getFile(firstDefaultBackgroundImagePathLand)
                        .addOnCompleteListener(task2 -> listener.onBackgrondLoaded("/.default/" + background + ".jpeg"))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        listener.onBackgrondLoaded("/.default/" + background + ".jpeg");
                }))
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    listener.onBackgrondLoaded("/.default/" + background + ".jpeg");
                });
    }
}
