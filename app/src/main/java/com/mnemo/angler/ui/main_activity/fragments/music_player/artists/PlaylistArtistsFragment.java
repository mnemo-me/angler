package com.mnemo.angler.ui.main_activity.fragments.music_player.artists;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.PlaylistArtistsAdapter;
import com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks.PlaylistArtistTracksFragment;
import com.mnemo.angler.ui.main_activity.fragments.music_player.music_player.MusicPlayerFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class PlaylistArtistsFragment extends Fragment implements PlaylistArtistsView {

    private PlaylistArtistsPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.mp_artist_list_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.mp_artist_list_empty_text)
    TextView emptyTextView;

    private PlaylistArtistsAdapter adapter;

    private BroadcastReceiver receiver;

    private String filter;
    private String selectedArtist = "";

    public PlaylistArtistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_artist_list, container, false);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Show artist list in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            getActivity().findViewById(R.id.song_list).setVisibility(View.VISIBLE);
        }else {
            getActivity().findViewById(R.id.artist_track_separator).setVisibility(View.VISIBLE);
        }

        // Get filter
        filter = ((MainActivity)getActivity()).getFilter();

        // Get selected artist (for landscape layout)
        selectedArtist = ((MusicPlayerFragment)getActivity().getSupportFragmentManager().findFragmentByTag("music_player_fragment")).getArtistSelected();


        // Configure recycler view
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setPadding(0, (int) (8 * MainActivity.density), 0, (int) (8 * MainActivity.density));
        }else{
            recyclerView.setPadding(0, (int) (4 * MainActivity.density), 0, (int) (4 * MainActivity.density));
            recyclerView.getLayoutParams().width = (int)(0.4 * view.getLayoutParams().width);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Pass to artist tracks fragment
        if (getArguments() != null) {

            String passArtist = getArguments().getString("artist");

            if (passArtist != null) {

                setArguments(null);

                openArtistTracks(passArtist);
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new PlaylistArtistsPresenter();
        presenter.attachView(this);

        // Load artists
        String mainPlaylist = ((MainActivity) getActivity()).getMainPlaylistName();

        if (!mainPlaylist.startsWith(getResources().getString(R.string.folder) + ": ")) {
            presenter.loadPlaylistArtists(mainPlaylist);
        }else{
            presenter.loadFolderArtists(mainPlaylist.replace(getResources().getString(R.string.folder) + ": ", ""));
        }

    }

    @Override
    public void onStart() {
        super.onStart();

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }


    // MVP View methods
    @Override
    public void setArtists(List<String> artists) {

        // Empty text visibility
        if (artists.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

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
    private void openArtistTracks(String artist) {

        selectedArtist = artist;
        if (!filter.equals("")) {
            ((SearchView)getActivity().findViewById(R.id.search_toolbar)).setQuery("", false);
        }

        PlaylistArtistTracksFragment playlistArtistTracksFragment = new PlaylistArtistTracksFragment();

        Bundle args = new Bundle();
        args.putString("artist", artist);

        playlistArtistTracksFragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.artist_song_list, playlistArtistTracksFragment, "artist_track_fragment")
                .commit();


        ((MusicPlayerFragment)getActivity().getSupportFragmentManager().findFragmentByTag("music_player_fragment")).setArtistSelected(artist);

    }

}
