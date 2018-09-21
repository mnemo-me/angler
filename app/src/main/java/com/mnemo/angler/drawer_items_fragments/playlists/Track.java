package com.mnemo.angler.drawer_items_fragments.playlists;


import android.support.annotation.NonNull;

public class Track implements Comparable{

    private String id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String uri;

    private boolean isAlreadyAdded;

    public Track(String id, String title, String artist, String album, long duration, String uri) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.uri = uri;

        isAlreadyAdded = false;
    }

    public void setAlreadyAdded(boolean alreadyAdded) {
        isAlreadyAdded = alreadyAdded;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public long getDuration() {
        return duration;
    }

    public String getUri() {
        return uri;
    }

    public boolean isAlreadyAdded() {
        return isAlreadyAdded;
    }

    @Override
    public int compareTo(@NonNull Object o) {

        if (title.equals(((Track)o).getTitle())){
            return artist.compareTo(((Track)o).getArtist());
        }else {
            return title.compareTo(((Track) o).getTitle());
        }
    }
}
