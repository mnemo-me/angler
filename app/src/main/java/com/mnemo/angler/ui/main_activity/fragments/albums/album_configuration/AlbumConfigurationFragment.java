package com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration;


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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;
import com.mnemo.angler.ui.main_activity.misc.ArtistCoverDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AlbumConfigurationFragment extends Fragment implements AlbumConfigurationView{

    AlbumConfigurationPresenter presenter;

    // Bind views via ButterKnife
    Unbinder unbinder;


    @BindView(R.id.album_conf_image)
    ImageView imageView;

    @BindView(R.id.album_conf_title)
    TextView titleText;

    @BindView(R.id.album_conf_artist)
    TextView artistView;

    @BindView(R.id.album_conf_tracks_count)
    TextView tracksCountView;

    @BindView(R.id.album_conf_list)
    RecyclerView recyclerView;

    @BindView(R.id.album_conf_play_all)
    LinearLayout playAllButton;

    TrackAdapter adapter;


    // Album variables
    String image;
    String title;
    String artist;
    String localPlaylistName;


    // Other variables;
    int orientation;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;


    public AlbumConfigurationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_album_configuration, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Initialize album variables
        image = getArguments().getString("image");
        title = getArguments().getString("album_name");
        artist = getArguments().getString("artist");

        localPlaylistName = "album/" + artist + "/" + title;

        // Load cover image
        loadCover();

        // Assign title & artist
        titleText.setText(title);
        artistView.setText(artist);

        // Setup recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new AlbumConfigurationPresenter();
        presenter.attachView(this);

        // Load album tracks
        presenter.loadAlbumTracks(artist, title);
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

        getContext().registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        presenter.deattachView();

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }


    // Setup listeners
    // Setup open cover
    @OnClick(R.id.album_conf_cardview)
    void openCover() {

     ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

     Bundle args = new Bundle();
     args.putString("artist", artist);
     args.putString("album", title);
     args.putString("image", image);
     artistCoverDialogFragment.setArguments(args);

     artistCoverDialogFragment.show(getActivity().getSupportFragmentManager(), "Album cover fragment");
    }

    // Setup back button
    @OnClick(R.id.album_conf_back)
    void back(){
        getActivity().onBackPressed();
    }


    // MVP View methods
    @Override
    public void setAlbumTracks(List<Track> tracks) {

        adapter = new TrackAdapter(getContext(), localPlaylistName, tracks, false);
        recyclerView.setAdapter(adapter);

        checkTracksCount();

        if ((((MainActivity)getActivity()).getCurrentPlaylistName()).equals(localPlaylistName)) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }
    }

    // Support methods
    // Track counter
    public void checkTracksCount(){
        tracksCountView.setText(getString(R.string.tracks) + " " + (adapter.getItemCount()));
    }

    // Load cover image
    public void loadCover(){
        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);
    }
}
