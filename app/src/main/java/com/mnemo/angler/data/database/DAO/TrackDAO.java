package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mnemo.angler.data.database.Entities.Track;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface TrackDAO {

    @Query("SELECT * FROM tracks ORDER BY title ASC")
    Flowable<List<Track>> getTracks();

    @Query("SELECT * FROM tracks WHERE _id IN (:trackIds)")
    List<Track> getTracks(List<String> trackIds);

    @Query("SELECT artist FROM tracks GROUP BY artist ORDER BY artist ASC")
    Flowable<List<String>> getArtists();

    @Query("SELECT * FROM tracks WHERE artist=:artist ORDER BY title ASC")
    Flowable<List<Track>> getTracksByArtist(String artist);

    @Insert
    void insert(Track... tracks);

    @Update
    void update(Track... tracks);

    @Delete
    void delete(Track... tracks);
}
