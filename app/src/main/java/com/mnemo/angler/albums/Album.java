package com.mnemo.angler.albums;


import android.support.annotation.NonNull;

public class Album implements Comparable{

    private String album;
    private String artist;

    Album(String album, String artist) {
        this.album = album;
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public int hashCode() {
        return album.hashCode() + artist.hashCode();
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Album two = (Album)o;

        if (artist.equals(two.getAlbum())) {
            return album.compareTo(two.getAlbum());
        }else{
            return artist.compareTo(two.getArtist());
        }
    }
}
