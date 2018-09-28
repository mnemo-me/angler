package com.mnemo.angler.ui.main_activity.fragments.artists;


import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.ChangeTransform;
import android.support.transition.Fade;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.mnemo.angler.ui.main_activity.fragments.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.utils.ImageAssistant;
import com.mnemo.angler.data.database.AnglerContract.*;
import com.mnemo.angler.data.file_storage.AnglerFolder;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.mnemo.angler.data.database.AnglerContract.BASE_CONTENT_URI;


public class ArtistsFragment extends Fragment implements DrawerItem, LoaderManager.LoaderCallbacks<Cursor> {


    private static final int LOADER_ARTISTS_ID = 100;


    @BindView(R.id.artists_drawer_back)
    ImageView back;

    @BindView(R.id.artists_grid)
    GridView gridView;

    private SimpleCursorAdapter adapter;


    Unbinder unbinder;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artists, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Load artists
        getLoaderManager().initLoader(LOADER_ARTISTS_ID, null, this);

        // Setup playlist GridView with adapter
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            gridView.setNumColumns(2);
        }else{
            gridView.setNumColumns(3);
        }
        adapter = new SimpleCursorAdapter(getContext(), R.layout.art_artists, null,
                new String[]{TrackEntry.COLUMN_ARTIST, TrackEntry.COLUMN_ARTIST}, new int[]{R.id.artist_image, R.id.artist_artist}, 0);

        adapter.setViewBinder((view12, cursor, i) -> {

            if (view12.getId() == R.id.artist_image){

                String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + cursor.getString(1) + ".jpg";
                ImageAssistant.loadImage(getContext(), imagePath, (ImageView) view12, 200);

                return true;
            }
            return false;
        });
        gridView.setAdapter(adapter);



        // Setup drawer menu button
        back.setOnClickListener(view1 -> ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START));

        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_ARTISTS_ID:
                return new CursorLoader(getContext(), Uri.withAppendedPath(BASE_CONTENT_URI, "artist_list/" + PlaylistEntry.LIBRARY),
                        new String[]{"_id", TrackEntry.COLUMN_ARTIST},
                        null, null,
                        TrackEntry.COLUMN_ARTIST + " ASC");

        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {
            case LOADER_ARTISTS_ID:
                adapter.swapCursor(data);

                gridView.setOnItemClickListener((parent, view, position, id) -> {

                    data.moveToPosition(position);

                    String artist = data.getString(1);
                    String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

                    ArtistConfigurationFragment artistConfigurationFragment = new ArtistConfigurationFragment();
                    Bundle args = new Bundle();
                    args.putString("image", imagePath);
                    args.putString("artist", data.getString(1));
                    artistConfigurationFragment.setArguments(args);

                    artistConfigurationFragment.setSharedElementEnterTransition(new TransitionSet()
                        .addTransition(new ChangeBounds())
                        .addTransition(new ChangeTransform()));


                    artistConfigurationFragment.setEnterTransition(new Fade().setStartDelay(100));
                    artistConfigurationFragment.setReturnTransition(null);

                    setReenterTransition(new Fade().setStartDelay(100));

                    getActivity().getSupportFragmentManager().beginTransaction()
                        .addSharedElement(back, "back")
                        .replace(R.id.frame, artistConfigurationFragment, "artist_conf_fragment")
                                .addToBackStack(null)
                                .commit();


                });

                gridView.setOnItemLongClickListener((adapterView, view, position, id) -> {

                    data.moveToPosition(position);

                    String artist = data.getString(1);
                    String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";

                    ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

                    Bundle args = new Bundle();
                    args.putString("artist", artist);
                    args.putString("image", imagePath);
                    artistCoverDialogFragment.setArguments(args);

                    artistCoverDialogFragment.show(getActivity().getSupportFragmentManager(), "Artist cover fragment");

                    return true;
                });

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
      switch (loader.getId()) {
            case LOADER_ARTISTS_ID:
                adapter.swapCursor(null);
                break;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
