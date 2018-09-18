package com.mnemo.angler.artists;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;


import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.mnemo.angler.R;
import com.mnemo.angler.albums.AlbumConfigurationFragment;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.data.AnglerFolder;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;



public class ArtistAlbumsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public ArtistAlbumsFragment() {
        // Required empty public constructor
    }


    public static final int LOADER_ARTIST_ALBUMS_ID = 0;


    @BindView(R.id.artist_albums_grid)
    GridView albumsGrid;

    SimpleCursorAdapter adapter;

    String artist;

    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_albums, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get artist
        artist = getArguments().getString("artist");


        // Load albums
        getLoaderManager().initLoader(LOADER_ARTIST_ALBUMS_ID, null, this);

        adapter = new SimpleCursorAdapter(getContext(),R.layout.art_album, null,
                new String[]{TrackEntry.COLUMN_ALBUM, TrackEntry.COLUMN_ALBUM},
                new int[]{R.id.album_image, R.id.album_name},0);

        adapter.setViewBinder((view1, cursor, i) -> {

            if (view1.getId() == R.id.album_image){

                String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + cursor.getString(3) + ".jpg";

                ImageAssistant.loadImage(getContext(),albumImagePath, (ImageView) view1, 125);

                return true;
            }
            return false;
        });

        albumsGrid.setAdapter(adapter);


        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


     @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_ARTIST_ALBUMS_ID:

                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, "album_list/" + SourceEntry.SOURCE_LIBRARY),
                        null,
                        TrackEntry.COLUMN_ARTIST + " = ?", new String[]{artist},
                        TrackEntry.COLUMN_ALBUM + " ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_ARTIST_ALBUMS_ID:

                adapter.swapCursor(data);

                albumsGrid.setOnItemClickListener((parent, view, position, id) -> {

                    data.moveToPosition(position);

                    String album = data.getString(3);
                    String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

                    AlbumConfigurationFragment albumConfigurationFragment = new AlbumConfigurationFragment();

                    Bundle args = new Bundle();
                    args.putString("image", albumImagePath);
                    args.putString("album_name", album);
                    args.putString("artist", artist);
                    albumConfigurationFragment.setArguments(args);


                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame, albumConfigurationFragment, "album_conf_fragment")
                            .addToBackStack(null)
                            .commit();


                });

                albumsGrid.setOnItemLongClickListener((adapterView, view, position, id) -> {

                    data.moveToPosition(position);

                    String album = data.getString(3);
                    String albumImagePath = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";

                    ArtistCoverDialogFragment artistCoverDialogFragment = new ArtistCoverDialogFragment();

                    Bundle args = new Bundle();
                    args.putString("artist", artist);
                    args.putString("album", album);
                    args.putString("image", albumImagePath);
                    artistCoverDialogFragment.setArguments(args);

                    artistCoverDialogFragment.show(getActivity().getSupportFragmentManager(), "Album cover fragment");

                    return true;
                });

                break;

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_ARTIST_ALBUMS_ID:
                adapter.swapCursor(null);
                break;
        }
    }
}
