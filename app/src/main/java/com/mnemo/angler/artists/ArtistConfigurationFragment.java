package com.mnemo.angler.artists;


import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.data.AnglerContract.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.mnemo.angler.data.AnglerContract.BASE_CONTENT_URI;


public class ArtistConfigurationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public ArtistConfigurationFragment() {
        // Required empty public constructor
    }

    private static final int LOADER_ARTIST_TRACKS_COUNT_ID = 100;
    private static final int LOADER_ARTIST_ALBUMS_COUNT_ID = 200;

    Unbinder unbinder;


    // Bind views via butterknife

    @BindView(R.id.artist_conf_cardview)
    CardView cardView;

    @BindView(R.id.artist_conf_image)
    ImageView imageView;

    @BindView(R.id.artist_conf_artist)
    TextView artistText;


    @BindView(R.id.artist_conf_tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.artist_conf_view_pager)
    ViewPager viewPager;

    // artist variables
    String image;
    String artist;

    int tracksCount;
    int albumsCount;


    // other variables;
    int orientation;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_configuration, container, false);

        unbinder = ButterKnife.bind(this, view);

        orientation = getResources().getConfiguration().orientation;


        // Initialize artist variables
        image = getArguments().getString("image");
        artist = getArguments().getString("artist");

        // Extract artist info (tracks and albums count)
        getLoaderManager().initLoader(LOADER_ARTIST_TRACKS_COUNT_ID, null, this);

        // Load cover image
        int imageHeight;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageHeight = 125;
        }else{
            imageHeight = 240;
        }

        ImageAssistant.loadImage(getContext(), image, imageView, imageHeight);

        // Set on click listener on cover
        cardView.setOnClickListener(view1 -> {

            ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

            Bundle args = new Bundle();
            args.putString("artist", artist);
            args.putString("image", image);
            artistCoverDialogFragment.setArguments(args);

            artistCoverDialogFragment.show(getActivity().getSupportFragmentManager(), "Artist cover fragment");
        });

        // Assign artist text
        artistText.setText(artist);


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    }

    @Override
    public void onStop() {
        super.onStop();
        DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();

    }


    // Setup back button
    @OnClick(R.id.artist_conf_back)
    void back(){
        getActivity().onBackPressed();
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        switch (id){

            case LOADER_ARTIST_TRACKS_COUNT_ID:

                return new CursorLoader(getContext(), Uri.withAppendedPath(BASE_CONTENT_URI, SourceEntry.SOURCE_LIBRARY),
                        null,
                        TrackEntry.COLUMN_ARTIST + " = ?", new String[]{artist},
                        null);

            case LOADER_ARTIST_ALBUMS_COUNT_ID:

                return new CursorLoader(getContext(), Uri.withAppendedPath(BASE_CONTENT_URI, "album_list/" + SourceEntry.SOURCE_LIBRARY),
                        null,
                        TrackEntry.COLUMN_ARTIST + " = ?", new String[]{artist},
                        TrackEntry.COLUMN_ALBUM + " ASC");

            default:

                return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){

            case LOADER_ARTIST_TRACKS_COUNT_ID:

                tracksCount = data.getCount();
                getLoaderManager().initLoader(LOADER_ARTIST_ALBUMS_COUNT_ID, null, this);

                break;

            case  LOADER_ARTIST_ALBUMS_COUNT_ID:

                albumsCount = data.getCount();

                // Initialize tab layout with view pager (tracks, albums, bio)
                viewPager.setAdapter(new ArtistTabsAdapter(getActivity().getSupportFragmentManager(), artist, tracksCount, albumsCount, orientation));
                tabLayout.setupWithViewPager(viewPager);

        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
