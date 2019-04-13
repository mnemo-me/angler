package com.mnemo.angler.data.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mnemo.angler.data.database.Entities.Track;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface TrackDAO {

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    Flowable<List<Track>> getTracks();

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    Single<List<Track>> getTracksOnce();

    @Query("SELECT * FROM tracks WHERE _id IN (:tracksId)")
    List<Track> getTracks(List<String> tracksId);

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    List<Track> getLibrary();

    @Query("SELECT COUNT(*) FROM tracks")
    Single<Integer> getLibraryTrackCount();

    @Query("SELECT artist FROM tracks GROUP BY artist ORDER BY artist COLLATE NOCASE ASC")
    Flowable<List<String>> getArtists();

    @Query("SELECT artist FROM tracks WHERE _id IN (:tracksId) GROUP BY artist ORDER BY artist COLLATE NOCASE ASC")
    Single<List<String>> getArtists(List<String> tracksId);

    @Query("SELECT * FROM tracks WHERE artist=:artist ORDER BY title COLLATE NOCASE ASC")
    Flowable<List<Track>> getTracksByArtist(String artist);

    @Query("SELECT * FROM tracks WHERE artist=:artist AND _id IN (:tracksId) ORDER BY title COLLATE NOCASE ASC")
    Single<List<Track>> getTracksByArtist(List<String> tracksId, String artist);

    @Query("SELECT * FROM tracks WHERE artist=:artist AND album=:album ORDER BY album_position,title COLLATE NOCASE ASC")
    Flowable<List<Track>> getAlbumTracks(String artist, String album);

    @Query("SELECT COUNT(*) FROM tracks WHERE artist=:artist AND album=:album")
    int getAlbumsTrackCount(String album, String artist);

    @Query("SELECT * FROM tracks WHERE album_position=:albumPosition")
    Single<List<Track>> getTracksWithAlbumPosition(int albumPosition);

    @Query("UPDATE tracks SET album_position=:albumPosition WHERE _id=:id")
    void updateTrackAlbumPosition(String id, int albumPosition);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Track... tracks);

    @Update
    void update(Track... tracks);

    @Delete
    void delete(Track... tracks);
}
