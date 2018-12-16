package com.mnemo.angler.ui.main_activity.fragments.playlists.playlists;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.data.database.Entities.Playlist;
import com.mnemo.angler.ui.main_activity.adapters.PlaylistsAdapter;
import com.mnemo.angler.R;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class PlaylistsFragment extends Fragment implements DrawerItem, PlaylistsView {

    PlaylistsPresenter presenter;

    // Bind views with ButterKnife
    Unbinder unbinder;

    @BindView(R.id.playlist_grid)
    RecyclerView recyclerView;

    PlaylistsAdapter adapter;

    int orientation;


    public PlaylistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pm_fragment_playlists, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

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

        adapter = new PlaylistsAdapter(getContext(), playlists);
        recyclerView.setAdapter(adapter);
    }


    // Support methods
    public void updateGrid(){
        adapter.notifyDataSetChanged();
    }

}
