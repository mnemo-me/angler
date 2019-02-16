package com.mnemo.angler.data.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT year FROM albums WHERE artist=:artist AND album=:album")
    Single<Integer> getAlbumYear(String artist, String album);

    @Query("UPDATE albums SET year=:year WHERE _id=:id")
    void updateAlbumYear(String id, int year);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Album... albums);

    @Update
    void update(Album... albums);

    @Delete
    void delete(Album... albums);
}
