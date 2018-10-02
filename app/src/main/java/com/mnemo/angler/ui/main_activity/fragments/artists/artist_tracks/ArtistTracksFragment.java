package com.mnemo.angler.ui.main_activity.fragments.artists.artist_tracks;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;

import java.util.List;




public class ArtistTracksFragment extends Fragment implements ArtistTracksView{

    ArtistTracksPresenter presenter;

    RecyclerView recyclerView;

    TrackAdapter adapter;

    // Artist tracks variables
    String artist;
    String localPlaylistName;

    int orientation;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    public ArtistTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Setup recycler view
        recyclerView = new RecyclerView(getContext());

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setPadding(0, (int)(46 * MainActivity.density), 0, 0);
        }else{
            recyclerView.setPadding(0, (int)(12 * MainActivity.density), 0, (int)(8 * MainActivity.density));
        }
        recyclerView.setClipToPadding(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);


        // Get artist
        artist = getArguments().getString("artist");

        localPlaylistName = "artist/" + artist;

        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistTracksPresenter();
        presenter.attachView(this);

        // Load tracks
        presenter.loadArtistTracks(artist);
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.attachView(this);

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (trackPlaylist.equals(localPlaylistName)) {
                            try {
                                adapter.setTrack(mediaId);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }

                        break;

                    case "playback_state_changed":

                        adapter.setPlaybackState(intent.getExtras().getInt("playback_state"));

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");
        intentFilter.addAction("playback_state_changed");

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(receiver);

        presenter.deattachView();
    }


    // MVP View methods
    @Override
    public void setArtistTracks(List<Track> tracks) {

        adapter = new TrackAdapter(getContext(), localPlaylistName, tracks, false);
        recyclerView.setAdapter(adapter);

        if ((((MainActivity)getActivity()).getCurrentPlaylistName()).equals(localPlaylistName)) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }

    }
}
