package com.mnemo.angler.artists;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.background_changer.ImageAssistent;
import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.data.AnglerFolder;
import com.mnemo.angler.playlist_manager.PlaylistConfigurationFragment;

import java.io.File;

import static com.mnemo.angler.data.AnglerContract.BASE_CONTENT_URI;


public class ArtistsFragment extends Fragment implements DrawerItem, LoaderManager.LoaderCallbacks<Cursor> {


    private static final int LOADER_ARTISTS_ID = 100;

    private GridView gridView;
    private SimpleCursorAdapter adapter;

    public ArtistsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artists, container, false);

        getLoaderManager().initLoader(LOADER_ARTISTS_ID, null, this);

        gridView = view.findViewById(R.id.artists_grid);
        adapter = new SimpleCursorAdapter(getContext(), R.layout.art_artists, null,
                new String[]{TrackEntry.COLUMN_ARTIST, TrackEntry.COLUMN_ARTIST}, new int[]{R.id.artist_image, R.id.artist_artist}, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {

                if (view.getId() == R.id.artist_image){

                    String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + cursor.getString(1) + ".jpg";
                    ImageAssistent.loadImage(getContext(), imagePath, (ImageView)view, 205);

                    return true;
                }
                return false;
            }
        });
        gridView.setAdapter(adapter);

        // Setup drawer menu button
        ImageView drawerBack = view.findViewById(R.id.artists_drawer_back);
        drawerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
            }
        });

        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_ARTISTS_ID:
                return new CursorLoader(getContext(), Uri.withAppendedPath(BASE_CONTENT_URI, "artist_list/" + SourceEntry.SOURCE_LIBRARY),
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

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        data.moveToPosition(position);

                        String imagePath = AnglerFolder.PATH_ARTIST_IMAGE + File.separator + data.getString(1) + ".jpg";

                        PlaylistConfigurationFragment artistConfigurationFragment = new PlaylistConfigurationFragment();
                        Bundle args = new Bundle();
                        args.putString("type", "artist");
                        args.putString("image", imagePath);
                        args.putString("artist", data.getString(1));
                        artistConfigurationFragment.setArguments(args);

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame,artistConfigurationFragment, "playlist_conf_fragment")
                                .addToBackStack(null)
                                .commit();


                    }
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

}
