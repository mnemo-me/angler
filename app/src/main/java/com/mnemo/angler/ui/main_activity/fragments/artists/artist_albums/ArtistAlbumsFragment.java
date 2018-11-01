package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;


import android.content.res.Configuration;
import android.os.Bundle;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.ui.main_activity.adapters.ArtistAlbumAdapter;
import com.mnemo.angler.ui.main_activity.classes.Album;

import java.util.List;


public class ArtistAlbumsFragment extends Fragment implements ArtistAlbumsView {


    ArtistAlbumsPresenter presenter;

    RecyclerView recyclerView;
    ArtistAlbumAdapter adapter;

    String artist;

    int orientation;

    public ArtistAlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get orientation
        orientation = getResources().getConfiguration().orientation;

        // Setup recycler view
        recyclerView = new RecyclerView(getContext());

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setPadding((int)(12 * MainActivity.density),(int)(30 * MainActivity.density),(int)(4 * MainActivity.density),(int)(10 * MainActivity.density));
        }else{
            recyclerView.setPadding((int)(4 * MainActivity.density),(int)(16 * MainActivity.density),(int)(4 * MainActivity.density),(int)(8 * MainActivity.density));
        }
        recyclerView.setClipToPadding(false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Get artist
        artist = getArguments().getString("artist");

        return recyclerView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Presenter to View
        presenter = new ArtistAlbumsPresenter();
        presenter.attachView(this);

        // Load albums
        presenter.loadArtistAlbums(artist);
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


    // MVP View methods
    @Override
    public void setArtistAlbums(List<Album> albums) {

        adapter = new ArtistAlbumAdapter(getContext(), artist, albums);
        recyclerView.setAdapter(adapter);
    }
}
