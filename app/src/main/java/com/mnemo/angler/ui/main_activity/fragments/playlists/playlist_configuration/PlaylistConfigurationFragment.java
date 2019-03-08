package com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration;


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
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;
import com.mnemo.angler.ui.main_activity.fragments.playlists.add_tracks_to_playlist.AddTracksDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.manage_tracks.ManageTracksDialogFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;
import com.mnemo.angler.ui.main_activity.misc.play_all.PlayAllDialogFragment;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.data.file_storage.AnglerFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;


public class PlaylistConfigurationFragment extends Fragment implements PlaylistConfigurationView{


    private PlaylistConfigurationPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.playlist_conf_cardview)
    CardView cardView;

    @BindView(R.id.playlist_conf_image)
    ImageView imageView;

    @BindView(R.id.playlist_conf_title)
    TextView titleText;

    @Nullable
    @BindView(R.id.playlist_conf_collapsed_title)
    TextView collapsedTitleText;

    @BindView(R.id.playlist_conf_tracks_count)
    TextView tracksCountView;

    @Nullable
    @BindView(R.id.playlist_conf_play_all)
    Button playAllLayout;

    @Nullable
    @BindView(R.id.playlist_conf_play_all_button)
    ImageButton playAllButton;

    @Nullable
    @BindView(R.id.playlist_conf_add_tracks)
    Button addTracksButton;

    @Nullable
    @BindView(R.id.playlist_conf_manage_tracks)
    Button manageTracksButton;

    @BindView(R.id.playlist_conf_list)
    RecyclerView recyclerView;

    @BindView(R.id.playlist_conf_back)
    ImageButton back;

    @Nullable
    @BindView(R.id.playlist_conf_app_bar)
    AppBarLayout appBarLayout;

    @Nullable
    @BindView(R.id.playlist_conf_collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.playlist_conf_empty_text)
    TextView emptyTextView;

    private TrackAdapter adapter;

    // Playlist variables
    private String type;
    private String title;
    private String cover;
    private String localPlaylistName;

    // Other variables;
    private int orientation;

    private BroadcastReceiver receiver;

    public PlaylistConfigurationFragment() {
        // Required empty public constructor
    }


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

        // Set type
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            type = "playlist";
        }else{
            type = "playlist(land)";
        }

        // Assign title
        titleText.setText(title);

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            collapsedTitleText.setText(title);

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            titleText.setMaxWidth((int)(0.45 * size.x));
        }

        // Load cover image
        updateCover();

        // Setup appbar behavior
        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {

                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {

                    float alpha = 0;

                    titleText.setAlpha(alpha);
                    tracksCountView.setAlpha(alpha);
                    playAllLayout.setAlpha(alpha);
                    cardView.setAlpha(alpha);

                    collapsedTitleText.setVisibility(View.VISIBLE);

                } else {

                    float alpha = 1f - (float) Math.abs(verticalOffset) / (float) (appBarLayout.getTotalScrollRange());

                    titleText.setAlpha(alpha);
                    tracksCountView.setAlpha(alpha);
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
        recyclerView.setItemViewCacheSize(20);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
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

                switch (intent.getAction()){
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (adapter != null) {

                            if (trackPlaylist.equals(localPlaylistName)) {
                                adapter.setTrack(mediaId);
                            }else{
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

        presenter.deattachView();
        unbinder.unbind();
    }



    // MVP View methods
    public void setPlaylistTracks(List<Track> tracks){

        // Empty text visibility
        if (tracks.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        adapter = new TrackAdapter(getContext(), type, title, tracks);
        recyclerView.setAdapter(adapter);

        checkTracksCount();
        presenter.checkLibraryTracksCount();

        if (((((MainActivity)getActivity()).getCurrentPlaylistName())).equals(localPlaylistName)) {

            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }
    }

    @Override
    public void setAddTracksAvailable(boolean isAddTracksAvailable) {

        if (!isAddTracksAvailable) {

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                addTracksButton.setEnabled(false);
                addTracksButton.setAlpha(0.3f);
            }else{
                adapter.disableAddTracksButton();
            }
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

    @Optional
    @OnClick(R.id.playlist_conf_play_all)
    void playAll(){

        if (presenter.getTracks().size() != 0) {

            PlayAllDialogFragment playAllDialogFragment = new PlayAllDialogFragment();

            Bundle args = new Bundle();
            args.putString("type", type);
            args.putString("playlist", title);
            args.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) presenter.getTracks());
            playAllDialogFragment.setArguments(args);

            playAllDialogFragment.show(getActivity().getSupportFragmentManager(), "play_all_dialog_fragment");

        }else{

            Toast.makeText(getContext(), R.string.empty_playlist, Toast.LENGTH_SHORT).show();
        }
    }

    @Optional
    @OnClick(R.id.playlist_conf_play_all_button)
    void playAllButton(){
        playAll();
    }

    @Optional
    @OnClick(R.id.playlist_conf_add_tracks)
    void addTracks(){

        AddTracksDialogFragment addTracksDialogFragment = new AddTracksDialogFragment();

        Bundle argsToTracks = new Bundle();
        argsToTracks.putString("title", title);
        addTracksDialogFragment.setArguments(argsToTracks);

        addTracksDialogFragment.show(((MainActivity)getContext()).getSupportFragmentManager(), "add_tracks_dialog_fragment");
    }

    @Optional
    @OnClick(R.id.playlist_conf_manage_tracks)
    void manageTracks(){

        ManageTracksDialogFragment manageTracksDialogFragment = new ManageTracksDialogFragment();

        Bundle argsToTracks = new Bundle();
        argsToTracks.putString("title", title);
        manageTracksDialogFragment.setArguments(argsToTracks);

        manageTracksDialogFragment.show(((MainActivity) getContext()).getSupportFragmentManager(), "manage_tracks_dialog_fragment");

    }

    @OnClick(R.id.playlist_conf_back)
    void back(){
        getActivity().onBackPressed();
    }



    // Support methods
    // Track counter
    private void checkTracksCount(){

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            tracksCountView.setText(getString(R.string.tracks) + ": " + (adapter.getItemCount()));

            if (adapter.getItemCount() == 0){
                manageTracksButton.setEnabled(false);
                manageTracksButton.setAlpha(0.3f);
            }else{

                if (!manageTracksButton.isEnabled()){
                    manageTracksButton.setEnabled(true);
                    manageTracksButton.setAlpha(1f);
                }
            }

        }else{
            tracksCountView.setText(getString(R.string.tracks) + ": " + (adapter.getItemCount() - 1));
        }
    }

    // Updating cover
    public void updateCover(){

        int imageHeight = getResources().getConfiguration().screenWidthDp / 2;

        ImageAssistant.loadImage(getContext(), cover, imageView, imageHeight);
    }



    // Changing playlist title also image
    public void changeTitle(String newTitle) {
        title = newTitle;
        cover = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";

        titleText.setText(title);

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            collapsedTitleText.setText(title);
        }
    }

    // Update tracks
    public void updateTracks(){
        presenter.loadPlaylistTracks(title);
    }
}
