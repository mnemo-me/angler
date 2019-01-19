package com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist;


import com.mnemo.angler.AnglerApp;
import com.mnemo.angler.data.AnglerRepository;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.base.DisposableBasePresenter;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class AddTracksPresenter extends DisposableBasePresenter {

    private int initialPosition = 0;

    @Inject
    AnglerRepository repository;

    AddTracksPresenter() {

        AnglerApp.getAnglerComponent().injectAddTracksPresenter(this);
    }

    // Load tracks from database and check them in map
    void loadTracks(String playlist){
        setListener(repository.loadCheckedPlaylistTracks(playlist, tracks -> {

            if (getView() != null){
                ((AddTracksView)getView()).setTracks(tracks);
            }

            for (Track track : tracks.keySet()){
                if (tracks.get(track)){
                    initialPosition++;
                }
            }
        }));
    }

    // Add new tracks in playlist
    void addTracksToPlaylist(String playlist, List<Track> tracks){
        repository.addTracksToPlaylist(playlist, assignPositionToTrack(tracks));
    }

    // Map tracks with position
    private HashMap<Track, Integer> assignPositionToTrack(List<Track> tracks){

        HashMap<Track, Integer> tracksWithPosition = new HashMap<>();

        for (Track track : tracks){
            tracksWithPosition.put(track, initialPosition + tracksWithPosition.size() + 1);
        }

        return tracksWithPosition;
    }
}
