package com.mnemo.angler.data.database.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;


@Entity(tableName = "tracks")
public class Track implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Track createFromParcel(Parcel parcel) {
            return new Track(parcel);
        }

        @Override
        public Track[] newArray(int i) {
            return new Track[i];
        }
    };

    @NonNull
    @PrimaryKey
    private String _id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "uri")
    private String uri;

    @ColumnInfo(name = "year")
    private int year;

    @ColumnInfo(name = "album_position")
    private int albumPosition;

    public Track(@NonNull String _id, String title, String artist, String album, long duration, String uri, int year, int albumPosition) {
        this._id = _id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.uri = uri;
        this.year = year;
        this.albumPosition = albumPosition;
    }

    @NonNull
    public String get_id() {
        return _id;
    }

    public void set_id(@NonNull String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getAlbumPosition() {
        return albumPosition;
    }

    public void setAlbumPosition(int albumPosition) {
        this.albumPosition = albumPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Track track = (Track) o;

        return _id.equals(track._id);
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public String toString() {
        return get_id() + ":::" + getTitle() + ":::" + getArtist() + ":::" + getAlbum() + ":::" + getDuration() + ":::" + getUri();
    }

    // Parcelable
    public Track(Parcel parcel) {
        this._id = parcel.readString();
        this.title = parcel.readString();
        this.artist = parcel.readString();
        this.album = parcel.readString();
        this.duration = parcel.readLong();
        this.uri = parcel.readString();
        this.year = parcel.readInt();
        this.albumPosition = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(this._id);
        parcel.writeString(this.title);
        parcel.writeString(this.artist);
        parcel.writeString(this.album);
        parcel.writeLong(this.duration);
        parcel.writeString(this.uri);
        parcel.writeInt(this.year);
        parcel.writeInt(this.albumPosition);
    }
}
