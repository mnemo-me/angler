package com.mnemo.angler.ui.main_activity.fragments.folders.folder_configuration;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Track;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.TrackAdapter;
import com.mnemo.angler.ui.main_activity.misc.play_all.PlayAllDialogFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderConfigurationFragment extends Fragment implements FolderConfigurationView {

    private FolderConfigurationPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.folder_conf_title)
    TextView titleText;

    @BindView(R.id.folder_conf_tracks_count)
    TextView tracksCountView;

    @Nullable
    @BindView(R.id.folder_conf_image)
    ImageView folderImage;

    @BindView(R.id.folder_conf_play_all)
    Button playAllButton;

    @BindView(R.id.folder_conf_link)
    Button linkButton;

    @BindView(R.id.folder_conf_list)
    RecyclerView recyclerView;

    private TrackAdapter adapter;


    // folder variables
    private String folder;


    // Other variables;
    private int orientation;

    private BroadcastReceiver receiver;


    public FolderConfigurationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fold_fragment_folder_configuration, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Initialize folder variables
        folder = getArguments().getString("folder");

        // Assign variables
        titleText.setText(new File(folder).getName());

        // Load folder image in landscape layout
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            ImageAssistant.loadImage(getContext(), "R.drawable.folder", folderImage, 140);
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
        presenter = new FolderConfigurationPresenter();
        presenter.attachView(this);

        // Check link
        presenter.checkLink(folder);

        // Load folder tracks
        presenter.loadFolderTracks(folder);
    }

    @Override
    public void onStart() {
        super.onStart();

        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Set current track
        if (((MainActivity)getActivity()).getCurrentPlaylistName().equals(folder)){

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

                            if (trackPlaylist.equals(folder)) {
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
    @OnClick(R.id.folder_conf_play_all)
    void playAll(){

        PlayAllDialogFragment playAllDialogFragment = new PlayAllDialogFragment();

        Bundle args = new Bundle();
        args.putString("type", "folder");
        args.putString("playlist", folder);
        args.putParcelableArrayList("tracks", (ArrayList<? extends Parcelable>) presenter.getTracks());
        playAllDialogFragment.setArguments(args);

        playAllDialogFragment.show(getActivity().getSupportFragmentManager(), "play_all_dialog_fragment");
    }

    @OnClick(R.id.folder_conf_back)
    void back(){
        getActivity().onBackPressed();
    }

    @OnClick(R.id.folder_conf_link)
    void link(){

        if (!linkButton.isSelected()){
            presenter.addFolderLink(new File(folder).getName(), folder);
            linkButton.setSelected(true);
        }else{
            presenter.removeFolderLink(folder);
            linkButton.setSelected(false);
        }
    }




    // MVP View methods
    @Override
    public void setFolderTracks(List<Track> tracks) {

        adapter = new TrackAdapter(getContext(), "folder", folder, tracks);
        recyclerView.setAdapter(adapter);

        checkTracksCount();

        if ((((MainActivity)getActivity()).getCurrentPlaylistName()).equals(folder)) {
            adapter.setTrack(((MainActivity) getActivity()).getCurrentMediaId());
            adapter.setPlaybackState(((MainActivity) getActivity()).getPlaybackState());
        }
    }

    @Override
    public void linkAdded() {
        Toast.makeText(getContext(), getResources().getString(R.string.linked), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void linkRemoved() {
        Toast.makeText(getContext(), getResources().getString(R.string.link_removed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void linkActive(boolean linkStatus) {
        linkButton.setSelected(linkStatus);
    }

    // Support methods
    // Track counter
    private void checkTracksCount(){
        tracksCountView.setText(String.valueOf(adapter.getItemCount()));
    }

}
