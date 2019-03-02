package com.mnemo.angler.ui.main_activity.fragments.music_player.artist_tracks;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class PlaylistArtistTracksFragment extends Fragment implements PlaylistArtistTracksView {

    private PlaylistArtistTracksPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.mp_track_list_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.mp_track_list_empty_text)
    TextView emptyTextView;

    private ShimmerFrameLayout loadingView;

    private TrackAdapter adapter;

    private BroadcastReceiver receiver;

    private String artist;
    private String localPlaylistName;

    private String filter;

    public PlaylistArtistTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_track_list, container, false);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Hide artist list in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            getActivity().findViewById(R.id.song_list).setVisibility(View.GONE);
        }

        // Loading view appear handler
        loadingView = view.findViewById(R.id.mp_track_list_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (adapter == null){
                loadingView.setVisibility(View.VISIBLE);
            }

        }, 1000);

        // Get filter
        filter = ((MainActivity)getActivity()).getFilter();

        // Get artist
        if (savedInstanceState == null) {
            artist = getArguments().getString("artist");
        }else{
            artist = savedInstanceState.getString("artist");
        }

        // Create local playlist name
        localPlaylistName = "playlist_artist/" + ((MainActivity)getActivity()).getMainPlaylistName()
                .replace("/", "\\") + "/" + artist.replace("/", "\\");

        // Configure recycler view
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setPadding(0, (int) (12 * MainActivity.density), 0, (int) (8 * MainActivity.density));
        }else{
            recyclerView.setPadding(0, (int) (4 * MainActivity.density), 0, (int) (4 * MainActivity.density));
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind presenter ro View
        presenter = new PlaylistArtistTracksPresenter();
        presenter.attachView(this);

        // Load artist tracks
        presenter.loadArtistTracksFromPlaylist(((MainActivity)getActivity()).getMainPlaylistName(), artist);
    }


    @Override
    public void onStart() {
        super.onStart();

        // Set current track
        if (((MainActivity)getActivity()).getCurrentPlaylistName().equals(localPlaylistName)) {

            if (adapter != null) {
                adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
                adapter.setPlaybackState(((MainActivity)getActivity()).getPlaybackState());
            }
        }


        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()) {
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (adapter != null) {

                            if (trackPlaylist.equals(localPlaylistName)) {
                                adapter.setTrack(mediaId);
                            } else {
                                adapter.setTrack("");
                            }

                        }

                        break;

                    case "playback_state_changed":

                        if (adapter != null) {
                            adapter.setPlaybackState(intent.getExtras().getInt("playback_state"));
                        }

                        break;

                    case "filter_applied":

                        filter = intent.getStringExtra("filter");
                        presenter.applyFilter(filter);

                        break;
                }

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");
        intentFilter.addAction("playback_state_changed");
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(receiver);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("artist", artist);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }


    // MVP View methods
    @Override
    public void setTracks(List<Track> artistTracks) {

        // Empty text visibility
        if (artistTracks.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        // Loading text visibility
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
        }

        adapter = new TrackAdapter(getContext(), "music_player", localPlaylistName, artistTracks);
        recyclerView.setAdapter(adapter);

        if (((MainActivity)getActivity()).getCurrentPlaylistName().equals(((MainActivity)getActivity()).getMainPlaylistName())) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }
    }

    @Override
    public String getFilter() {
        return filter;
    }
}
