package com.mnemo.angler.data.database.Entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "links", foreignKeys = @ForeignKey(entity = Track.class, parentColumns = "_id", childColumns = "track_id", onDelete = CASCADE))
public class Link {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int _id;

    @ColumnInfo(name = "track_id")
    private String trackId;

    @ColumnInfo(name = "playlist")
    private String playlist;

    @ColumnInfo(name = "position")
    private int position;

    public Link(String trackId, String playlist, int position) {
        this.trackId = trackId;
        this.playlist = playlist;
        this.position = position;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getPlaylist() {
        return playlist;
    }

    public void setPlaylist(String playlist) {
        this.playlist = playlist;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
