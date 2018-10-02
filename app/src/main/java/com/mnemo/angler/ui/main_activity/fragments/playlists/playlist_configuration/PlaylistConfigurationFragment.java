package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;
import com.mnemo.angler.utils.ImageAssistant;
import com.mnemo.angler.data.file_storage.AnglerFolder;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlaylistConfigurationFragment extends Fragment implements PlaylistConfigurationView{


    public PlaylistConfigurationFragment() {
        // Required empty public constructor
    }

    PlaylistConfigurationPresenter presenter;

    // Bind views via ButterKnife
    Unbinder unbinder;

    @BindView(R.id.playlist_conf_cardview)
    CardView cardView;

    @BindView(R.id.playlist_conf_image)
    ImageView imageView;

    @BindView(R.id.playlist_conf_title)
    TextView titleText;

    @BindView(R.id.playlist_conf_tracks_count)
    TextView tracksCountView;

    @BindView(R.id.playlist_conf_list)
    RecyclerView recyclerView;

    @BindView(R.id.playlist_conf_back)
    ImageButton back;

    @BindView(R.id.playlist_conf_play_all)
    LinearLayout playAllButton;

    TrackAdapter adapter;

    // Playlist variables
    String title;
    String cover;
    String localPlaylistName;

    // Other variables;
    int orientation;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlist_configuration, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Get playlist variables
        if (title == null) {
            title = getArguments().getString("title");
            cover = getArguments().getString("cover");
        }

        localPlaylistName = title;

        // Assign title
        titleText.setText(title);

        // Load cover image
        updateCover();

        // Setup recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new PlaylistConfigurationPresenter();
        presenter.attachView(this);

        // Load tracks
        presenter.loadPlaylistTracks(title);
    }

    @Override
    public void onStart() {
        super.onStart();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);

    }


    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        presenter.deattachView();

        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("image", cover);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }



    // MVP View methods
    public void setPlaylistTracks(List<Track> tracks){

        adapter = new TrackAdapter(getContext(), title, tracks, true);
        recyclerView.setAdapter(adapter);

        checkTracksCount();

        if ((((MainActivity)getActivity()).getCurrentPlaylistName()).equals(localPlaylistName)) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }
    }


    // Setup listeners
    @OnClick(R.id.playlist_conf_cardview)
    void configurePlaylist(){

        PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

        Bundle args = new Bundle();
        args.putString("action", "change");
        args.putString("title", title);
        args.putString("cover", cover);
        playlistCreationDialogFragment.setArguments(args);

        playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");
    }

    @OnClick(R.id.playlist_conf_back)
    void back(){
        getActivity().onBackPressed();
    }



    // Support methods
    // Track counter
    public void checkTracksCount(){

        tracksCountView.setText(getString(R.string.tracks) + " " + (adapter.getItemCount() - 1));
    }

    // Updating cover
    public void updateCover(){

        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 200;
        }

        ImageAssistant.loadImage(getContext(), cover, imageView, imageHeight);
    }



    // Changing playlist title also image
    public void changeTitle(String newTitle){
        title = newTitle;
        cover = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";

        titleText.setText(title);
    }

    // Update tracks
    public void updateTracks(){
        presenter.loadPlaylistTracks(title);
    }
}
