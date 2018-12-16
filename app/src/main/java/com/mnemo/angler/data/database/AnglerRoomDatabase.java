package com.mnemo.angler.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.mnemo.angler.data.database.DAO.AlbumDAO;
import com.mnemo.angler.data.database.DAO.LinkDAO;
import com.mnemo.angler.data.database.DAO.PlaylistDAO;
import com.mnemo.angler.data.database.DAO.TrackDAO;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.data.database.Entities.Link;
import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.data.database.Entities.Track;

@Database(entities = {Playlist.class, Track.class, Link.class, Album.class}, version = 1)
public abstract class AnglerRoomDatabase extends RoomDatabase {

    public abstract PlaylistDAO playlistDAO();
    public abstract TrackDAO trackDAO();
    public abstract LinkDAO linkDAO();
    public abstract AlbumDAO albumDAO();
}
