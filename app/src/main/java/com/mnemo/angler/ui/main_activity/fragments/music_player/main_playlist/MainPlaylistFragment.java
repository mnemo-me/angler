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
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.player.AnglerService;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;

import java.util.List;


public class MainPlaylistFragment extends Fragment implements MainPlaylistView{


    public MainPlaylistFragment() {
        // Required empty public constructor
    }

    MainPlaylistPresenter presenter;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    TrackAdapter adapter;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    int orientation;

    String filter = "";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // get orientation
        orientation = getResources().getConfiguration().orientation;

        // get filter
        filter = ((MainActivity)getActivity()).getFilter();

        // configure recycler view
        recyclerView = new RecyclerView(getContext());

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setPadding(0, (int) (8 * MainActivity.density), 0, (int) (8 * MainActivity.density));
        }else{
            recyclerView.setPadding(0, (int) (4 * MainActivity.density), 0, (int) (4 * MainActivity.density));
        }

        recyclerView.setClipToPadding(false);

        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // bind Presenter to View
        presenter = new MainPlaylistPresenter();
        presenter.attachView(this);

        // load tracks
        presenter.loadPlaylist(((MainActivity) getActivity()).getMainPlaylistName());
    }


    @Override
    public void onResume() {
        super.onResume();

        presenter.attachView(this);

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (trackPlaylist.equals(((MainActivity)getActivity()).getMainPlaylistName())) {
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
    public void onPause() {
        super.onPause();

        presenter.deattachView();
        getContext().unregisterReceiver(receiver);
    }


    // MVP View methods
    @Override
    public void setTracks(List<Track> playlistTracks) {

        adapter = new TrackAdapter(getContext(), ((MainActivity)getActivity()).getMainPlaylistName(), playlistTracks, false);
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

    public void initializeQueue(){

        if (!AnglerService.isQueueInitialized){
            //((MainActivity)getActivity()).getAnglerClient().addToQueue(((MainActivity)getActivity()).getAnglerClient().getMainPlaylistName(), data, false);
            MediaControllerCompat.getMediaController(getActivity()).getTransportControls().prepare();
            AnglerService.isQueueInitialized = true;
        }
    }


}
