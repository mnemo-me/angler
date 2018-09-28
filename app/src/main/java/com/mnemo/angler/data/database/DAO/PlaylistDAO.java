package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mnemo.angler.data.database.Entities.Playlist;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface PlaylistDAO {

    @Query("SELECT * FROM playlists")
    Flowable<List<Playlist>> getPlaylists();

    @Insert
    void insert(Playlist playlist);

    @Delete
    void delete(Playlist playlist);

}
