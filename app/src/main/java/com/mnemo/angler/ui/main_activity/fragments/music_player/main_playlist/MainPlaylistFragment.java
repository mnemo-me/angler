package com.mnemo.angler.ui.main_activity.fragments.music_player.main_playlist;



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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainPlaylistFragment extends Fragment implements MainPlaylistView{


    private MainPlaylistPresenter presenter;

    private Unbinder unbinder;

    @BindView(R.id.mp_refreshable_track_list_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.mp_refreshable_track_list_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.mp_refreshable_track_list_empty_text)
    TextView emptyTextView;

    private ShimmerFrameLayout loadingView;

    private TrackAdapter adapter;

    private BroadcastReceiver receiver;

    private String filter = "";

    private boolean isAppFirstLaunch;

    public MainPlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.mp_refreshable_track_list, container, false);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Show track list in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            getActivity().findViewById(R.id.song_list).setVisibility(View.VISIBLE);
        }

        // Loading view appear handler
        loadingView = view.findViewById(R.id.mp_refreshable_track_list_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (adapter == null){
                loadingView.setVisibility(View.VISIBLE);
            }

        }, 1000);

        // Get filter
        filter = ((MainActivity)getActivity()).getFilter();

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

        // Bind Presenter to View
        presenter = new MainPlaylistPresenter();
        presenter.attachView(this);

        isAppFirstLaunch = presenter.checkAppFirstLaunchState();

        if (isAppFirstLaunch){
            loadingView.setVisibility(View.VISIBLE);
        }

        // Load tracks
        String mainPlaylist = ((MainActivity) getActivity()).getMainPlaylistName();

        if (!mainPlaylist.startsWith(getResources().getString(R.string.folder) + ": ")) {
            presenter.loadPlaylist(mainPlaylist);
        }else{
            presenter.loadFolderTracks(mainPlaylist.replace(getResources().getString(R.string.folder) + ": ", ""));
        }

        // Configure swipe behavior
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.updateLibrary());
    }

    @Override
    public void onStart() {
        super.onStart();

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

                        if (adapter != null) {

                            if (trackPlaylist.equals(((MainActivity) getActivity()).getMainPlaylistName())) {
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
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }

    // MVP View methods
    @Override
    public void setTracks(List<Track> playlistTracks) {

        if (isAppFirstLaunch && playlistTracks.size() == 0) {

            isAppFirstLaunch = false;
            presenter.saveAppFirstLaunchState(isAppFirstLaunch);

        }else{

            // Empty text visibility
            if (playlistTracks.size() == 0) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }

            // Loading text visibility
            if (loadingView.getVisibility() == View.VISIBLE) {
                loadingView.setVisibility(View.GONE);
            }

            adapter = new TrackAdapter(getContext(), "music_player", ((MainActivity) getActivity()).getMainPlaylistName(), playlistTracks);
            recyclerView.setAdapter(adapter);

            if (((MainActivity) getActivity()).getCurrentPlaylistName().equals(((MainActivity) getActivity()).getMainPlaylistName())) {
                adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
                adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
            }

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
