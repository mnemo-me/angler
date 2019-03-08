package com.mnemo.angler.ui.main_activity.fragments.artists.artist_tracks;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ArtistTracksFragment extends Fragment implements ArtistTracksView{

    private ArtistTracksPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.artist_tracks_list)
    RecyclerView recyclerView;

    private TrackAdapter adapter;

    // Artist tracks variables
    private String artist;
    private String localPlaylistName;

    private BroadcastReceiver receiver;

    public ArtistTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_tracks, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            recyclerView.setPadding(0, (int)getResources().getDimension(R.dimen.playlist_track_list_padding), 0, 0);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);


        // Get artist
        artist = getArguments().getString("artist");

        localPlaylistName = "artist/" + artist;

        return view;
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

        // Set current track
        if (((MainActivity)getActivity()).getCurrentPlaylistName().equals(localPlaylistName)){

            if (adapter != null){
                adapter.setTrack(((MainActivity)getActivity()).getCurrentMediaId());
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
                }

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");
        intentFilter.addAction("playback_state_changed");

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        presenter.deattachView();
        unbinder.unbind();
    }

    // MVP View methods
    @Override
    public void setArtistTracks(List<Track> tracks) {

        adapter = new TrackAdapter(getContext(), "artist", localPlaylistName, tracks);
        recyclerView.setAdapter(adapter);

        if ((((MainActivity)getActivity()).getCurrentPlaylistName()).equals(localPlaylistName)) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }

    }
}
