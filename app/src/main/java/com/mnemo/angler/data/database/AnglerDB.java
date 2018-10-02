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
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AnglerDB{

    Context context;

    private AnglerRoomDatabase db;

    public interface PlaylistsUpdateListener{
        void playlistsUpdated(List<String> playlistTitles);
    }

    public interface UserPlaylistsUpdateListener{
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

    public interface PlaylistCheckedTracksLoadListener{
        void checkedTracksLoaded(HashMap<Track, Boolean> tracks);
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
                .subscribe(tracksToUpdate -> db.trackDAO().update(tracksToUpdate.toArray(new Track[tracksToUpdate.size()])));
    }

    private void deleteTracks(List<Track> tracks, List<Track> dbTracks) {

        Observable.fromIterable(dbTracks)
                .filter(dbTrack -> !tracks.contains(dbTrack))
                .toList()
                .subscribe(tracksToDelete -> db.trackDAO().delete(tracksToDelete.toArray(new Track[tracksToDelete.size()])));
    }



    public void loadPlaylistTitles(PlaylistsUpdateListener listener){

        db.playlistDAO().getPlaylistTitles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::playlistsUpdated);
    }


    public void loadPlaylistsCreatedByUser(UserPlaylistsUpdateListener listener){

        db.playlistDAO().getUserPlaylists()
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
                    .subscribe(links -> {

                        Observable.fromIterable(db.trackDAO().getTracks(getTrackIds(links)))
                                .observeOn(AndroidSchedulers.mainThread())
                                .toSortedList((track1, track2) -> {

                                    String trackId1 = track1.get_id();
                                    String trackId2 = track2.get_id();

                                    int trackPosition1 = -1;
                                    int trackPosition2 = -1;

                                    for (Link link : links){
                                        if (link.getTrackId().equals(trackId1)){
                                            trackPosition1 = link.getPosition();
                                        }

                                        if (link.getTrackId().equals(trackId2)){
                                            trackPosition2 = link.getPosition();
                                        }

                                        if (trackPosition1 != -1 && trackPosition2 != -1){
                                            break;
                                        }
                                    }

                                    return trackPosition1 - trackPosition2;

                                })
                                .subscribe(listener::playlistLoaded);



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

            db.linkDAO().getTracksId(playlist)
                    .subscribeOn(Schedulers.io())
                    .subscribe(tracksId -> db.trackDAO().getArtists(tracksId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(listener::artistsLoaded));
        }
    }


    public void loadArtistTracksFromPlaylist(String playlist, String artist, ArtistTracksLoadListener listener){

        if (playlist.equals("library")){

            db.trackDAO().getTracksByArtist(artist)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::artistTracksLoaded);
        }else{

            db.linkDAO().getTracksId(playlist)
                    .subscribeOn(Schedulers.io())
                    .subscribe(tracksId -> {
                        db.trackDAO().getTracksByArtist(tracksId, artist)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(listener::artistTracksLoaded);
                    });

        }
    }

    public void insertPlaylist(String title, String cover){

        Completable.fromAction(() -> {
            Playlist playlist = new Playlist(title, cover);
            db.playlistDAO().insert(playlist);
        })
                .subscribeOn(Schedulers.io())
                .subscribe();


    }

    public void updatePlaylist(String oldTitle, String newTitle, String cover){

        Completable.fromAction(() -> {
            db.playlistDAO().update(oldTitle, newTitle, cover);
        })
                .subscribeOn(Schedulers.io())
                .subscribe();

    }

    public void deletePlaylist(String playlist){

        Completable.fromAction(() -> {
            db.playlistDAO().delete(playlist);
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void loadCheckedPlaylistTracks(String playist, PlaylistCheckedTracksLoadListener listener){

        db.trackDAO().getTracks()
                .map((Function<List<Track>, HashMap<Track, Boolean>>) tracks -> {

                    HashMap checkedTracks = new HashMap();

                    for (Track track : tracks){
                        checkedTracks.put(track, false);
                    }

                    return checkedTracks;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(checkedTracks -> {

                    db.linkDAO().getLinks(playist)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(this::getTrackIds)
                            .subscribe(tracksId -> {

                               for (Track track : checkedTracks.keySet()){
                                   if (tracksId.contains(track.get_id())){
                                       checkedTracks.put(track, true);
                                   }
                               }

                               listener.checkedTracksLoaded(checkedTracks);
                            });
                });
    }

    public void addTracksToPlaylist(String playlist, HashMap<Track, Integer> tracksWithPosition){

        Observable.fromIterable(tracksWithPosition.keySet())
                .subscribeOn(Schedulers.io())
                .map(track -> new Link(track.get_id(), playlist, tracksWithPosition.get(track)))
                .toList()
                .subscribe(links -> db.linkDAO().insert(links.toArray(new Link[links.size()])));

    }

    public void updatePlaylistLink(String oldTitle, String newTitle){

        Completable.fromAction(() -> db.linkDAO().updatePlaylistLink(oldTitle, newTitle))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
