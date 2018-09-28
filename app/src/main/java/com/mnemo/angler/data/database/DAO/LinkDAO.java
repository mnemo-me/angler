package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.mnemo.angler.data.database.Entities.Link;

import java.util.List;

import io.reactivex.Flowable;


@Dao
public interface LinkDAO {

    @Query("SELECT * FROM links WHERE playlist=:playlist")
    Flowable<List<Link>> getLinks(String playlist);
}
