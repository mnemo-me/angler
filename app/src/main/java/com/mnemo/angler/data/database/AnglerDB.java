package com.mnemo.angler.data.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;


import com.mnemo.angler.data.database.Entities.Link;
import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.data.database.Entities.Track;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AnglerDB{

    Context context;

    private AnglerRoomDatabase db;

    public interface PlaylistsUpdateListener{
        void playlistsUpdated(List<Playlist> playlists);
    }

    public interface PlaylistLoadListener{
        void playlistLoaded(List<Track> tracks);
    }

    public interface ArtistsLoadListener{
        void artistsLoaded(List<String> artists);
    }

    public interface ArtistTracksLoadListener{
        void artistTracksLoaded(List<Track> tracks);
    }

    @Inject
    public AnglerDB(Context context) {

        this.context = context;

        db = Room.databaseBuilder(this.context, AnglerRoomDatabase.class, "angler-database")
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase database) {
                        super.onCreate(database);

                        Completable.fromAction(() -> db.playlistDAO().insert(new Playlist("library", "R.drawable.back3")))
                                .subscribeOn(Schedulers.io())
                                .subscribe();
                    }
                })
                .build();
    }


    public void updateDatabase(List<Track> tracks){

        db.trackDAO().getTracks()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(dbTracks -> {

                    insertTracks(tracks, dbTracks);
                    updateTracks(tracks, dbTracks);
                    deleteTracks(tracks, dbTracks);

                });

    }

    // Insert, Update, Delete Track methods
    private void insertTracks(List<Track> tracks, List<Track> dbTracks){

        Observable.fromIterable(tracks)
                .filter(track -> {

                    if (track.getTitle() == null){
                        return false;
                    }else if (track.getArtist() == null){
                        return false;
                    }else if (track.getAlbum() == null){
                        return false;
                    }else if (track.getDuration() == 0){
                        return false;
                    }else if (track.getUri() == null){
                        return false;
                    }

                    return true;

                })
                .filter(track -> !dbTracks.contains(track))
                .toList()
                .subscribe(tracksToInsert -> db.trackDAO().insert(tracksToInsert.toArray(new Track[tracksToInsert.size()])));
    }

    private void updateTracks(List<Track> tracks, List<Track> dbTracks){

        Observable.fromIterable(tracks)
                .filter(dbTracks::contains)
                .filter(track -> {
                    Track dbTrack = dbTracks.get(dbTracks.indexOf(track));
                    return !track.getUri().equals(dbTrack.getUri());
                })
                .toList()
                .subscribe(tracksToUpdate -> db.trackDAO().insert(tracksToUpdate.toArray(new Track[tracksToUpdate.size()])));
    }

    private void deleteTracks(List<Track> tracks, List<Track> dbTracks) {

        Observable.fromIterable(dbTracks)
                .filter(dbTrack -> !tracks.contains(dbTrack))
                .toList()
                .subscribe(tracksToDelete -> db.trackDAO().insert(tracksToDelete.toArray(new Track[tracksToDelete.size()])));
    }



    public void loadPlaylists(PlaylistsUpdateListener listener){

        db.playlistDAO().getPlaylists()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::playlistsUpdated);
    }


    public void loadPlaylist(String playlist, PlaylistLoadListener listener){

        if (playlist.equals("library")){

            db.trackDAO().getTracks()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::playlistLoaded);

        }else {

            db.linkDAO().getLinks(playlist)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(links -> {
/*
                        List<Track> tracks = db.trackDAO().getTracks(getTrackIds(links));
                        HashMap<Integer, Track> playlistTracks = new HashMap<>();

                        for (Track track : tracks) {

                            for (Link link : links) {

                                if (track.get_id().equals(link.getTrackId())) {
                                    playlistTracks.put(link.getPosition(), track);
                                    break;
                                }
                            }
                        }

                        listener.playlistLoaded(playlistTracks);*/

                    });
        }
    }

    private List<String> getTrackIds(List<Link> links){

        List<String> trackIds = new ArrayList<>();

        for (Link link : links){
            trackIds.add(link.getTrackId());
        }

        return trackIds;
    }

    public void loadArtists(String playlist, ArtistsLoadListener listener) {


        if (playlist.equals("library")){

            db.trackDAO().getArtists()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::artistsLoaded);
        }else{

        }
    }


    public void loadArtistTracksFromPlaylist(String playlist, String artist, ArtistTracksLoadListener listener){

        if (playlist.equals("library")){

            db.trackDAO().getTracksByArtist(artist)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::artistTracksLoaded);
        }else{

        }
    }

}
