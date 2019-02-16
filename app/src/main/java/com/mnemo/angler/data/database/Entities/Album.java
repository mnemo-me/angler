package com.mnemo.angler.data.database.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "albums")
public class Album {

    @NonNull
    @PrimaryKey
    private String _id;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "year")
    private int year;

    public Album(@NonNull String _id, String album, String artist, int year) {
        this._id = _id;
        this.album = album;
        this.artist = artist;
        this.year = year;
    }

    @NonNull
    public String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
