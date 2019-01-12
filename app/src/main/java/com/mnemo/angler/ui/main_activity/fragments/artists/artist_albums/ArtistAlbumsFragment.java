package com.mnemo.angler.ui.main_activity.fragments.artists.artist_albums;


import android.os.Bundle;


import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.mnemo.angler.R;
import com.mnemo.angler.data.database.Entities.Album;
import com.mnemo.angler.ui.main_activity.adapters.ArtistAlbumAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ArtistAlbumsFragment extends Fragment implements ArtistAlbumsView {


    private ArtistAlbumsPresenter presenter;

    // Bind views via ButterKnife
    private Unbinder unbinder;

    @BindView(R.id.artist_albums_list)
    RecyclerView recyclerView;

    private ShimmerFrameLayout loadingView;

    ArtistAlbumAdapter adapter;

    private String artist;

    public ArtistAlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_albums, container, false);

        // Inject views
        unbinder = ButterKnife.bind(this, view);

        // Loading view appear handler
        loadingView = view.findViewById(R.id.artist_albums_loading);

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            if (adapter == null){
                loadingView.setVisibility(View.VISIBLE);
            }

        }, 1000);

        // Setup recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        // Get artist
        artist = getArguments().getString("artist");

        return view;
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }


    // MVP View methods
    @Override
    public void setArtistAlbums(List<Album> albums) {

        // Loading text visibility
        if (loadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
        }

        adapter = new ArtistAlbumAdapter(getContext(), artist, albums);
        recyclerView.setAdapter(adapter);
    }
}
