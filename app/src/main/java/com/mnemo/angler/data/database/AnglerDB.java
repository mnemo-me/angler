package com.mnemo.angler.data.database;


import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;


import com.mnemo.angler.data.database.Entities.Album;
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
import io.reactivex.schedulers.Schedulers;

public class AnglerDB{

    private AnglerRoomDatabase db;

    // Listener interfaces
    public interface LibraryUpdateListener{
        void libraryUpdated();
    }

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

    public interface AlbumTracksLoadListener{
        void albumTracksLoaded(List<Track> tracks);
    }

    public interface PlaylistCheckedTracksLoadListener{
        void checkedTracksLoaded(HashMap<Track, Boolean> tracks);
    }

    public interface PlaylistsAndTitlesWithTrackLoadListener{
        void playlistsAndTitlesWithTrackLoaded(List<Playlist> playlists, List<String> playlistsWithTrack);
    }

    public interface TrackDeleteListener{
        void trackDeleted(int position);
    }

    public interface PlaylistClearListener{
        void playlistCleared();
    }

    public interface AlbumsLoadListener{
        void albumsLoaded(List<Album> albums);
    }

    public interface ArtistAlbumsLoadListener{
        void artistAlbumsLoaded(List<Album> albums);
    }

    public interface UnknownYearAlbumsLoadListener{
        void unknownYearAlbumsLoaded(List<Album> albums);
    }

    @Inject
    public AnglerDB(Context context) {

        db = Room.databaseBuilder(context, AnglerRoomDatabase.class, "angler-database")
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

    // Update database
    public void updateDatabase(List<Track> tracks){

        db.trackDAO().getTracksOnce()
                .subscribeOn(Schedulers.io())
                .subscribe(dbTracks -> {

                    insertTracks(tracks, dbTracks);
                    updateTracks(tracks, dbTracks);
                    deleteTracks(tracks, dbTracks);

                    cleanAlbums();
                });

    }

    public void updateDatabase(List<Track> tracks, LibraryUpdateListener listener){

        db.trackDAO().getTracksOnce()
                .subscribeOn(Schedulers.io())
                .subscribe(dbTracks -> Completable.fromAction(() -> {
                    insertTracks(tracks, dbTracks);
                    updateTracks(tracks, dbTracks);
                    deleteTracks(tracks, dbTracks);
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> listener.libraryUpdated()));
    }

    // Insert, Update, Delete track methods
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
                .subscribe(tracksToInsert -> {
                    db.trackDAO().insert(tracksToInsert.toArray(new Track[tracksToInsert.size()]));
                    db.albumDAO().insert(getAlbums(tracksToInsert).toArray(new Album[tracksToInsert.size()]));
                });
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

    // Tracks support method
    private List<String> getTrackIds(List<Link> links){

        List<String> trackIds = new ArrayList<>();

        for (Link link : links){
            trackIds.add(link.getTrackId());
        }

        return trackIds;
    }

    // Albums methods
    private List<Album> getAlbums(List<Track> tracks){

        List<Album> albums = new ArrayList<>();

        for (Track track : tracks){

            String album = track.getAlbum();
            String artist = track.getArtist();
            int year = track.getYear();

            String id = (album + "-" + artist).replace(" ", "_");

            albums.add(new Album(id, album, artist, year));
        }
        return albums;
    }

    // Delete unused albums
    private void cleanAlbums(){

        db.albumDAO().getAlbumsOnce()
                .subscribe(albums -> Observable.fromIterable(albums)
                        .filter(album -> db.trackDAO().getAlbumsTrackCount(album.getAlbum(), album.getArtist()) == 0)
                        .toList()
                        .subscribe(albumsToDelete -> db.albumDAO().delete(albumsToDelete.toArray(new Album[albumsToDelete.size()]))));

    }

    // Load albums
    public void loadAlbums(AlbumsLoadListener listener){

        db.albumDAO().getAlbums()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::albumsLoaded);
    }

    public void loadAlbumsInBackground(AlbumsLoadListener listener){

        db.albumDAO().getAlbumsOnce()
                .subscribeOn(Schedulers.io())
                .subscribe(listener::albumsLoaded);
    }

    public void loadArtistAlbums(String artist, ArtistAlbumsLoadListener listener){

        db.albumDAO().getArtistAlbums(artist)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::artistAlbumsLoaded);
    }

    public void loadAlbumsWithUnknownYear(UnknownYearAlbumsLoadListener listener){

        db.albumDAO().getAlbumsByYear(10000)
                .subscribeOn(Schedulers.io())
                .subscribe(listener::unknownYearAlbumsLoaded);
    }

    public void updateAlbumYear(String id, int year){
        db.albumDAO().updateAlbumYear(id, year);
    }

    // Playlists methods
    // Load playlists or/and titles methods
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

    public void loadPlaylistsAndTitlesWithTrack(String trackId, PlaylistsAndTitlesWithTrackLoadListener listener){

        db.playlistDAO().getUserPlaylists()
                .subscribeOn(Schedulers.io())
                .subscribe(playlists -> db.linkDAO().getPlaylistsWithTrack(trackId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(playlistsWithTrack -> listener.playlistsAndTitlesWithTrackLoaded(playlists, playlistsWithTrack)));
    }


    // Load playlist tracks mwthods
    public void loadPlaylistTracks(String playlist, PlaylistLoadListener listener){

        if (playlist.equals("library")){

            db.trackDAO().getTracks()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::playlistLoaded);

        }else {

            db.linkDAO().getLinks(playlist)
                    .subscribeOn(Schedulers.io())
                    .subscribe(links -> Observable.fromIterable(db.trackDAO().getTracks(getTrackIds(links)))
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
                            .subscribe(listener::playlistLoaded));
        }
    }


    public void loadCheckedPlaylistTracks(String playist, PlaylistCheckedTracksLoadListener listener){

        db.trackDAO().getTracks()
                .map(tracks -> {

                    HashMap<Track, Boolean> checkedTracks = new HashMap<>();

                    for (Track track : tracks){
                        checkedTracks.put(track, false);
                    }

                    return checkedTracks;
                })
                .subscribeOn(Schedulers.io())
                .subscribe(checkedTracks -> db.linkDAO().getLinks(playist)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(this::getTrackIds)
                        .subscribe(tracksId -> {

                            for (Track track : checkedTracks.keySet()){
                                if (tracksId.contains(track.get_id())){
                                    checkedTracks.put(track, true);
                                }
                            }

                            listener.checkedTracksLoaded(checkedTracks);
                        }));
    }

    // Insert, Update, Delete playlist methods
    public void insertPlaylist(String title, String cover){

        Completable.fromAction(() -> {
            Playlist playlist = new Playlist(title, cover);
            db.playlistDAO().insert(playlist);
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void updatePlaylist(String oldTitle, String newTitle, String cover){

        Completable.fromAction(() -> db.playlistDAO().update(oldTitle, newTitle, cover))
                .subscribeOn(Schedulers.io())
                .subscribe();

    }

    public void deletePlaylist(String playlist){

        Completable.fromAction(() -> db.playlistDAO().delete(playlist))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    // Add tracks to playlist methods
    public void addTracksToPlaylist(String playlist, HashMap<Track, Integer> tracksWithPosition){

        Observable.fromIterable(tracksWithPosition.keySet())
                .subscribeOn(Schedulers.io())
                .map(track -> new Link(track.get_id(), playlist, tracksWithPosition.get(track)))
                .toList()
                .subscribe(links -> db.linkDAO().insert(links.toArray(new Link[links.size()])));

    }

    public void addTracksToPlaylist(String playlist, List<Track> tracks){

        Observable.fromIterable(tracks)
                .map(track -> new Link(track.get_id(), playlist,tracks.indexOf(track)))
                .toList()
                .subscribe(links -> db.linkDAO().insert(links.toArray(new Link[links.size()])));
    }

    public void addTrackToPlaylist(String playlist, Track track){

        db.linkDAO().getTracksCount(playlist)
                .subscribeOn(Schedulers.io())
                .subscribe(tracksCount -> db.linkDAO().insert(new Link(track.get_id(), playlist, tracksCount + 1)));

    }


    // Update playlist title in links method
    public void updatePlaylistLink(String oldTitle, String newTitle){

        Completable.fromAction(() -> db.linkDAO().updatePlaylistLink(oldTitle, newTitle))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    // Delete/Restore track in playlist methods
    public void deleteTrackFromPlaylist(String playlist, String trackId, TrackDeleteListener listener){

        db.linkDAO().getPositionOfTrack(playlist, trackId)
                .subscribeOn(Schedulers.io())
                .subscribe(position -> Completable.fromAction(() -> db.linkDAO().deleteTrackFromPlaylist(playlist, trackId))
                        .subscribe(() -> Completable.fromAction(() -> db.linkDAO().decreasePositionsHigherThan(playlist, position))
                                .subscribe(() -> listener.trackDeleted(position))));
    }

    public void restoreTrackInPlaylist(String playlist, String trackId, int position){

        Completable.fromAction(() -> db.linkDAO().increasePositionsHigherOrEqual(playlist, position))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> db.linkDAO().insert(new Link(trackId, playlist, position)));
    }

    public void deleteAllTracksFromPlaylist(String playlist){

        Completable.fromAction(() -> db.linkDAO().deleteAllTracksFromPlaylist(playlist))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    public void deleteAllTracksFromPlaylist(String playlist, PlaylistClearListener listener){

        Completable.fromAction(() -> db.linkDAO().deleteAllTracksFromPlaylist(playlist))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> listener.playlistCleared());
    }

    // Artists methods
    // Load artists method
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

    // Load artist tracks methods
    public void loadArtistTracksFromPlaylist(String playlist, String artist, ArtistTracksLoadListener listener){

        if (playlist.equals("library")){

            db.trackDAO().getTracksByArtist(artist)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::artistTracksLoaded);
        }else{

            db.linkDAO().getTracksId(playlist)
                    .subscribeOn(Schedulers.io())
                    .subscribe(tracksId -> db.trackDAO().getTracksByArtist(tracksId, artist)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(listener::artistTracksLoaded));

        }
    }

    // Albums methods
    // Load album tracks method
    public void loadAlbumTracks(String artist, String album, AlbumTracksLoadListener listener){

        db.trackDAO().getAlbumTracks(artist, album)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::albumTracksLoaded);
    }

}
