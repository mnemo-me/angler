package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mnemo.angler.data.database.Entities.Link;

import java.util.List;

import io.reactivex.Flowable;


@Dao
public interface LinkDAO {

    @Query("SELECT * FROM links WHERE playlist=:playlist ORDER BY position ASC")
    Flowable<List<Link>> getLinks(String playlist);

    @Query("SELECT track_id FROM links WHERE playlist=:playlist")
    Flowable<List<String>> getTracksId(String playlist);

    @Insert
    void insert(Link... links);

    @Query("UPDATE links SET playlist=:newTitle WHERE playlist=:oldTitle")
    void updatePlaylistLink(String oldTitle, String newTitle);
}
