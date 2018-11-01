package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mnemo.angler.data.database.Entities.Playlist;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface PlaylistDAO {

    @Query("SELECT * FROM playlists")
    Flowable<List<Playlist>> getPlaylists();

    @Query("SELECT * FROM playlists WHERE title NOT LIKE 'library'")
    Flowable<List<Playlist>> getUserPlaylists();

    @Query("SELECT title FROM playlists")
    Flowable<List<String>> getPlaylistTitles();

    @Insert
    void insert(Playlist playlist);

    @Query("DELETE FROM playlists WHERE title=:playlist")
    void delete(String playlist);

    @Query("UPDATE playlists SET title=:newTitle, cover=:cover WHERE title=:oldTitle")
    void update(String oldTitle, String newTitle, String cover);

}
