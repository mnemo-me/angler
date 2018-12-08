package com.mnemo.angler.ui.main_activity.fragments.artists.artists;


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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mnemo.angler.ui.main_activity.adapters.ArtistAdapter;
import com.mnemo.angler.ui.main_activity.classes.DrawerItem;
import com.mnemo.angler.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ArtistsFragment extends Fragment implements DrawerItem, ArtistsView {

    ArtistsPresenter presenter;


    // Bind views via ButterKnife
    Unbinder unbinder;

    @BindView(R.id.artists_refresh_images)
    ImageButton refreshButton;

    @BindView(R.id.artists_refresh_progress)
    ProgressBar refreshProgressBar;

    @BindView(R.id.artists_grid)
    RecyclerView recyclerView;

    ArtistAdapter adapter;

    int orientation;

    boolean isRefreshing = false;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artists, container, false);

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Restore refreshing state
        if (savedInstanceState != null){
            isRefreshing = savedInstanceState.getBoolean("is_refreshing");

            if (isRefreshing){
                refreshButton.setVisibility(View.GONE);
                refreshProgressBar.setVisibility(View.VISIBLE);
            }
        }

        // Setup recycler view
        GridLayoutManager gridLayoutManager;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            gridLayoutManager = new GridLayoutManager(getContext(), 2);
        }else{
            gridLayoutManager = new GridLayoutManager(getContext(), 3);
        }

        recyclerView.setLayoutManager(gridLayoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistsPresenter();
        presenter.attachView(this);

        // Load artists
        presenter.loadArtists();
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("is_refreshing", isRefreshing);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // Refresh artists images
    @OnClick(R.id.artists_refresh_images)
    void refreshArtistImages(){
        if (!isRefreshing) {
            presenter.refreshArtistsImages();
            isRefreshing = true;

            refreshButton.setVisibility(View.GONE);
            refreshProgressBar.setVisibility(View.VISIBLE);
        }
    }

    // Setup drawer menu button
    @OnClick(R.id.artists_drawer_back)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }


    // MVP View methods
    @Override
    public void setArtists(List<String> artists) {

        adapter = new ArtistAdapter(getContext(), artists);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void completeRefreshingImages() {

        Toast.makeText(getContext(), R.string.artist_images_updated, Toast.LENGTH_SHORT).show();
        isRefreshing = false;

        refreshProgressBar.setVisibility(View.GONE);
        refreshButton.setVisibility(View.VISIBLE);

        adapter.notifyDataSetChanged();
    }
}
