package com.mnemo.angler.ui.main_activity.fragments.albums.album_configuration;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;
import com.mnemo.angler.ui.main_activity.misc.cover.CoverDialogFragment;
import com.mnemo.angler.ui.main_activity.misc.play_all.PlayAllDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;


public class AlbumConfigurationFragment extends Fragment implements AlbumConfigurationView{

    private AlbumConfigurationPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.album_conf_cardview)
    CardView cardView;

    @BindView(R.id.album_conf_image)
    ImageView imageView;

    @BindView(R.id.album_conf_title)
    TextView titleText;

    @Nullable
    @BindView(R.id.album_conf_collapsed_title)
    TextView collapsedTitleText;

    @BindView(R.id.album_conf_artist)
    TextView artistView;

    @BindView(R.id.album_conf_year)
    TextView yearView;

    @BindView(R.id.album_conf_tracks_count)
    TextView tracksCountView;

    @Nullable
    @BindView(R.id.album_conf_black_stripe)
    View blackStripe;

    @BindView(R.id.album_conf_play_all)
    Button playAllLayout;


    @BindView(R.id.album_conf_list)
    RecyclerView recyclerView;

    @Nullable
    @BindView(R.id.album_conf_app_bar)
    AppBarLayout appBarLayout;

    @Nullable
    @BindView(R.id.album_conf_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    private TrackAdapter adapter;


    // Album variables
    private String image;
    private String title;
    private String artist;
    private int year;
    private String localPlaylistName;


    // Other variables;
    private int orientation;

    private BroadcastReceiver receiver;


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
        year = getArguments().getInt("year");


        localPlaylistName = "album/" + artist + "/" + title;

        // Assign title & artist & year
        titleText.setText(title);

        if (orientation == Configuration.ORIENTATION_PORTRAIT){

            collapsedTitleText.setText(title);

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            titleText.setMaxWidth((int)(0.42 * size.x));
            artistView.setMaxWidth((int)(0.42 * size.x));
        }

        artistView.setText(artist);

        tracksCountView.setText(getString(R.string.tracks) + ": " + 0);

        setAlbumYear(year);

        // Setup appbar behavior
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {

                    float alpha = 0;

                    titleText.setAlpha(alpha);
                    artistView.setAlpha(alpha);
                    yearView.setAlpha(alpha);
                    tracksCountView.setAlpha(alpha);
                    blackStripe.setAlpha(alpha);
                    playAllLayout.setAlpha(alpha);
                    cardView.setAlpha(alpha);

                    collapsedTitleText.setVisibility(View.VISIBLE);

                } else {

                    float alpha = 1f - (float) Math.abs(verticalOffset) / (float) (appBarLayout.getTotalScrollRange());

                    titleText.setAlpha(alpha);
                    artistView.setAlpha(alpha);
                    yearView.setAlpha(alpha);
                    tracksCountView.setAlpha(alpha);
                    blackStripe.setAlpha(alpha);
                    playAllLayout.setAlpha(alpha);
                    cardView.setAlpha(alpha);

                    if (alpha < 0.5f) {
                        if (collapsedTitleText.getVisibility() == View.GONE) {
                            collapsedTitleText.setVisibility(View.VISIBLE);
                        }
                    }else{
                        if (collapsedTitleText.getVisibility() == View.VISIBLE) {
                            collapsedTitleText.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        // Setup recycler view
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
        presenter = new AlbumConfigurationPresenter();
        presenter.attachView(this);

        // Load cover image
        loadCover();

        // Get year from database if year == 0 in arguments
        if (year == 0){
            presenter.getYear(artist, title);
        }

        // Load album tracks
        presenter.loadAlbumTracks(artist, title);
    }

    @Override
    public void onStart() {
        super.onStart();

        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        presenter.deattachView();
        unbinder.unbind();
    }


    // Setup listeners
    @OnClick(R.id.album_conf_cardview)
    void openCover() {

        if (presenter.checkAlbumCoverExist(artist, title)) {

            CoverDialogFragment coverDialogFragment = new CoverDialogFragment();

            Bundle args = new Bundle();
            args.putString("artist", artist);
            args.putString("album", title);
            args.putString("image", image);
            coverDialogFragment.setArguments(args);

            coverDialogFragment.show(getActivity().getSupportFragmentManager(), "Album cover fragment");

        }else{

            Toast.makeText(getContext(), R.string.no_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Optional
    @OnClick(R.id.album_conf_play_all)
    void playAll(){

        PlayAllDialogFragment playAllDialogFragment = new PlayAllDialogFragment();

        Bundle args = new Bundle();
        args.putString("type", "album");
        args.putString("playlist", localPlaylistName);
        args.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) presenter.getTracks());
        playAllDialogFragment.setArguments(args);

        playAllDialogFragment.show(getActivity().getSupportFragmentManager(), "play_all_dialog_fragment");
    }


    @OnClick(R.id.album_conf_back)
    void back(){
        getActivity().onBackPressed();
    }


    // MVP View methods
    @Override
    public void setAlbumTracks(List<Track> tracks) {

        adapter = new TrackAdapter(getContext(), "album", localPlaylistName, tracks);
        recyclerView.setAdapter(adapter);

        checkTracksCount();

        if ((((MainActivity)getActivity()).getCurrentPlaylistName()).equals(localPlaylistName)) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }
    }

    @Override
    public void setAlbumYear(int year) {

        if (year != 10000 && year != 0) {
            yearView.setText(String.valueOf(year) + " · ");
        }
    }

    // Support methods
    // Track counter
    private void checkTracksCount(){
        tracksCountView.setText(getString(R.string.tracks) + ": " + (adapter.getItemCount()));
    }

    // Load cover image
    private void loadCover(){

        int imageHeight = getResources().getConfiguration().screenWidthDp / 2;

        if (presenter.checkAlbumCoverExist(artist, title)){
            ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);
        }else{
            ImageAssistant.loadImage(getContext(), "R.drawable.album_default", imageView, imageHeight);
        }

    }
}
