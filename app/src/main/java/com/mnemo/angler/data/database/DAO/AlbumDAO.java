package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mnemo.angler.data.database.Entities.Album;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface AlbumDAO {

    @Query("SELECT * FROM albums ORDER BY year ASC")
    Flowable<List<Album>> getAlbums();

    @Query("SELECT * FROM albums ORDER BY year ASC")
    Single<List<Album>> getAlbumsOnce();

    @Query("SELECT * FROM albums WHERE artist=:artist ORDER BY year ASC")
    Flowable<List<Album>> getArtistAlbums(String artist);

    @Query("SELECT * FROM albums WHERE year=:year")
    Single<List<Album>> getAlbumsByYear(int year);

    @Query("UPDATE albums SET year=:year WHERE _id=:id")
    void updateAlbumYear(String id, int year);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Album... albums);

    @Update
    void update(Album... albums);

    @Delete
    void delete(Album... albums);
}
