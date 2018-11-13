package com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainPlaylistFragment extends Fragment implements MainPlaylistView{


    MainPlaylistPresenter presenter;

    Unbinder unbinder;

    @BindView(R.id.mp_list_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.mp_list_recycler_view)
    RecyclerView recyclerView;

    TrackAdapter adapter;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    int orientation;

    String filter = "";

    public MainPlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_list, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Get filter
        filter = ((MainActivity)getActivity()).getFilter();

        // Configure recycler view
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setPadding(0, (int) (12 * MainActivity.density), 0, (int) (8 * MainActivity.density));
        }else{
            recyclerView.setPadding(0, (int) (4 * MainActivity.density), 0, (int) (4 * MainActivity.density));
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new MainPlaylistPresenter();
        presenter.attachView(this);

        // Load tracks
        presenter.loadPlaylist(((MainActivity) getActivity()).getMainPlaylistName());

        // Configure swipe behavior
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.updateLibrary());
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.attachView(this);

        // Set current track
        if (((MainActivity)getActivity()).getCurrentPlaylistName().equals(((MainActivity)getActivity()).getMainPlaylistName())) {

            if (adapter != null) {
                adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
                adapter.setPlaybackState(((MainActivity)getActivity()).getPlaybackState());
            }
        }

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (trackPlaylist.equals(((MainActivity)getActivity()).getMainPlaylistName())) {

                            if (adapter != null) {
                                adapter.setTrack(mediaId);
                            }

                        }else{

                            if (adapter != null){
                                adapter.setTrack("");
                            }
                        }

                        break;

                    case "playback_state_changed":

                        adapter.setPlaybackState(intent.getExtras().getInt("playback_state"));

                        break;

                    case "filter_applied":

                        filter = intent.getStringExtra("filter");
                        presenter.applyFilter(filter);

                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");
        intentFilter.addAction("playback_state_changed");
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.deattachView();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // MVP View methods
    @Override
    public void setTracks(List<Track> playlistTracks) {

        adapter = new TrackAdapter(getContext(), "music_player", ((MainActivity)getActivity()).getMainPlaylistName(), playlistTracks);
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

    @Override
    public void completeUpdate() {

        Toast.makeText(getContext(), R.string.library_updated, Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }
}
