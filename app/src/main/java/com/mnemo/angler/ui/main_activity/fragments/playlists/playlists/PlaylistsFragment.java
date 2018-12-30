package com.mnemo.angler.ui.main_activity.fragments.playlists.playlists;


import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.ui.main_activity.adapters.PlaylistsAdapter;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_configuration.PlaylistConfigurationFragment;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlaylistsFragment extends Fragment implements DrawerItem, PlaylistsView {

    private PlaylistsPresenter presenter;

    // Bind views with ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.playlist_toolbar)
    Toolbar toolbar;

    @BindView(R.id.playlist_grid)
    RecyclerView recyclerView;

    @BindView(R.id.playlist_empty_text)
    TextView emptyTextView;

    private ShimmerFrameLayout loadingView;

    private PlaylistsAdapter adapter;


    public PlaylistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlists, container, false);

        // Get orientation
        int orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Loading view appear handler
        loadingView = view.findViewById(R.id.playlist_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (adapter == null){
                loadingView.setVisibility(View.VISIBLE);
            }

        }, 1000);

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        GridLayoutManager gridLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getContext(), 3);
        }else {
            gridLayoutManager = new GridLayoutManager(getContext(), 5);
        }

        recyclerView.setLayoutManager(gridLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new PlaylistsPresenter();
        presenter.attachView(this);

        // Load playlists
        presenter.loadPlaylists();
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.attachView(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        presenter.deattachView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // Setup add new playlist button
    @OnClick(R.id.new_playlist_button)
    void createNewPlayist(){

        PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

        Bundle args = new Bundle();
        args.putString("action", "create");
        playlistCreationDialogFragment.setArguments(args);

        playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");
    }

    // Setup back button
    @OnClick(R.id.playlist_manager_drawer_back)
    void back(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }



    // MVP View methods
    @Override
    public void setPlaylists(List<Playlist> playlists) {

        // Empty text visibility
        if (playlists.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }

        // Loading text visibility
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
        }

        adapter = new PlaylistsAdapter(getContext(), playlists);
        adapter.setOnPlaylistClickListener((title, cover) -> {

            PlaylistConfigurationFragment playlistConfigurationFragment = new PlaylistConfigurationFragment();

            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("cover", cover);
            playlistConfigurationFragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame, playlistConfigurationFragment, "playlist_configuration_fragment")
                    .addToBackStack(null)
                    .commit();
        });

        adapter.setOnPlaylistLongClickListener(((title, cover) -> {

            PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

            Bundle args = new Bundle();
            args.putString("action", "change");
            args.putString("title", title);
            args.putString("cover", cover);
            playlistCreationDialogFragment.setArguments(args);

            playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");
        }));


        recyclerView.setAdapter(adapter);
    }


    // Support methods
    public void updateGrid(){
        adapter.notifyDataSetChanged();
    }

}
