package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mnemo.angler.data.database.Entities.Track;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface TrackDAO {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    Flowable<List<Track>> getTracks();

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    Single<List<Track>> getTracksOnce();

    @Query("SELECT * FROM tracks WHERE _id IN (:tracksId)")
    List<Track> getTracks(List<String> tracksId);

    @Query("SELECT artist FROM tracks GROUP BY artist ORDER BY artist ASC")
    Flowable<List<String>> getArtists();

    @Query("SELECT artist FROM tracks WHERE _id IN (:tracksId) GROUP BY artist ORDER BY artist ASC")
    Flowable<List<String>> getArtists(List<String> tracksId);

    @Query("SELECT * FROM tracks WHERE artist=:artist ORDER BY title ASC")
    Flowable<List<Track>> getTracksByArtist(String artist);

    @Query("SELECT * FROM tracks WHERE artist=:artist AND _id IN (:tracksId) ORDER BY title ASC")
    Flowable<List<Track>> getTracksByArtist(List<String> tracksId, String artist);

    @Query("SELECT * FROM tracks WHERE artist=:artist AND album=:album ORDER BY album_position,title ASC")
    Flowable<List<Track>> getAlbumTracks(String artist, String album);

    @Query("SELECT COUNT(*) FROM tracks WHERE artist=:artist AND album=:album")
    int getAlbumsTrackCount(String album, String artist);

    @Query("SELECT * FROM tracks WHERE album_position=:albumPosition")
    Single<List<Track>> getTracksWithAlbumPosition(int albumPosition);

    @Query("UPDATE tracks SET album_position=:albumPosition WHERE _id=:id")
    void updateTrackAlbumPosition(String id, int albumPosition);

    @Insert
    void insert(Track... tracks);

    @Update
    void update(Track... tracks);

    @Delete
    void delete(Track... tracks);
}
