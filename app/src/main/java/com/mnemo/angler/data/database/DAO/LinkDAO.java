package com.mnemo.angler.data.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mnemo.angler.data.database.Entities.Link;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;


@Dao
public interface LinkDAO {

    @Query("SELECT * FROM links WHERE playlist=:playlist ORDER BY position ASC")
    Flowable<List<Link>> getLinks(String playlist);

    @Query("SELECT * FROM links WHERE playlist=:playlist ORDER BY position ASC")
    Single<List<Link>> getLinksOnce(String playlist);

    @Query("SELECT track_id FROM links WHERE playlist=:playlist")
    Flowable<List<String>> getTracksId(String playlist);

    @Query("SELECT playlist FROM links WHERE track_id=:trackId")
    Single<List<String>> getPlaylistsWithTrack(String trackId);

    @Query("SELECT count(track_id) FROM links WHERE playlist=:playlist")
    Single<Integer> getTracksCount(String playlist);

    @Insert
    void insert(Link... links);

    @Query("UPDATE links SET playlist=:newTitle WHERE playlist=:oldTitle")
    void updatePlaylistLink(String oldTitle, String newTitle);

    @Query("SELECT position FROM links WHERE playlist=:playlist AND track_id=:trackId")
    Single<Integer> getPositionOfTrack(String playlist, String trackId);

    @Query("DELETE FROM links WHERE playlist=:playlist AND track_id=:trackId")
    void deleteTrackFromPlaylist(String playlist, String trackId);

    @Query("DELETE FROM links WHERE playlist=:playlist")
    void deleteAllTracksFromPlaylist(String playlist);

    @Query("UPDATE links SET position = position - 1 WHERE playlist=:playlist AND position > :position")
    void decreasePositionsHigherThan(String playlist, int position);

    @Query("UPDATE links SET position = position + 1 WHERE playlist=:playlist AND position >= :position")
    void increasePositionsHigherOrEqual(String playlist, int position);
}
