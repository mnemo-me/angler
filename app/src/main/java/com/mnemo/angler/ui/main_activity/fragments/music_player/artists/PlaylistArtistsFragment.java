package com.mnemo.angler.ui.main_activity.fragments.music_player.artists;


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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.PlaylistArtistsAdapter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.PlaylistArtistTracksFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerFragment;

import java.util.List;


public class PlaylistArtistsFragment extends Fragment implements PlaylistArtistsView {

    PlaylistArtistsPresenter presenter;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    private PlaylistArtistsAdapter adapter;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    int orientation;

    private String filter;
    private String selectedArtist = "";

    public PlaylistArtistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Get filter
        filter = ((MainActivity)getActivity()).getFilter();

        // Get selected artist (for landscape layout)
        selectedArtist = ((MusicPlayerFragment)getActivity().getSupportFragmentManager().findFragmentByTag("music_player_fragment")).getArtistSelected();


        // Configure recycler view
        recyclerView = new RecyclerView(getContext());

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setPadding(0, (int) (8 * MainActivity.density), 0, (int) (8 * MainActivity.density));
        }else{
            recyclerView.setPadding(0, (int) (2 * MainActivity.density), 0, (int) (2 * MainActivity.density));
        }

        recyclerView.setClipToPadding(false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new PlaylistArtistsPresenter();
        presenter.attachView(this);

        // Load artists
        presenter.loadArtists(((MainActivity)getActivity()).getMainPlaylistName());

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

                    case "filter_applied":

                        filter = intent.getExtras().getString("filter");
                        presenter.applyFilter(filter);

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.deattachView();
        getContext().unregisterReceiver(receiver);
    }


    // MVP View methods
    @Override
    public void setArtists(List<String> artists) {

        adapter = new PlaylistArtistsAdapter(getContext(), artists);
        adapter.setOnArtistSelectedListener(this::openArtistTracks);
        adapter.setArtist(selectedArtist);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public String getFilter() {
        return filter;
    }



    // Open track list based on artist
    public void openArtistTracks(String artist) {

        selectedArtist = artist;
        if (!filter.equals("")) {
            ((SearchView)getActivity().findViewById(R.id.search_toolbar)).setQuery("", false);
        }

        PlaylistArtistTracksFragment playlistArtistTracksFragment = new PlaylistArtistTracksFragment();

        Bundle args = new Bundle();
        args.putString("artist", artist);

        playlistArtistTracksFragment.setArguments(args);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            getParentFragment().getChildFragmentManager().beginTransaction()
                    .replace(R.id.song_list, playlistArtistTracksFragment, "artist_track_fragment")
                    .addToBackStack(null)
                    .commit();
        }else{

            getParentFragment().getChildFragmentManager().beginTransaction()
                    .replace(R.id.artist_song_list, playlistArtistTracksFragment, "artist_track_fragment")
                    .commit();
        }

        ((MusicPlayerFragment)getActivity().getSupportFragmentManager().findFragmentByTag("music_player_fragment")).setArtistSelected(artist);

    }

}
