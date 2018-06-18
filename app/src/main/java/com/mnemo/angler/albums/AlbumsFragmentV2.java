package com.mnemo.angler.albums;


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
import android.widget.ImageView;
import android.widget.ListView;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.SourceEntry;
import com.mnemo.angler.data.AnglerContract.TrackEntry;

import java.util.ArrayList;
import java.util.Collections;


public class AlbumsFragmentV2 extends Fragment implements DrawerItem, LoaderManager.LoaderCallbacks<Cursor> {


    public AlbumsFragmentV2() {
        // Required empty public constructor
    }

    public static final int LOADER_ALBUM_ID = 0;

    ArrayList<Album> albums;

    ListView listView;
    AlbumListAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_albums, container, false);

        albums = new ArrayList<>();

        // Setup complex listview (albums inside artists)
        listView = view.findViewById(R.id.albums_list);
        listView.setDividerHeight(0);
        listView.addHeaderView(LayoutInflater.from(getContext()).inflate(R.layout.alb_album_list_header, null, false));
        listView.addFooterView(LayoutInflater.from(getContext()).inflate(R.layout.alb_album_list_header, null, false));

        getLoaderManager().initLoader(LOADER_ALBUM_ID, null, this);

        // Setup drawer menu button
        ImageView drawerBack = view.findViewById(R.id.albums_drawer_back);
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

        switch (id){
            case LOADER_ALBUM_ID:
                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, "album_list/" + SourceEntry.SOURCE_LIBRARY),
                        new String[]{"_id",TrackEntry.COLUMN_ARTIST, TrackEntry.COLUMN_ALBUM, TrackEntry.COLUMN_URI},
                        null, null,
                        TrackEntry.COLUMN_ALBUM + " ASC");
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_ALBUM_ID:

                albums.clear();

                if (data.getCount() != 0) {
                    data.moveToFirst();

                    do {
                        String artist = data.getString(1);
                        String album = data.getString(2);

                        Album albumItem = new Album(album, artist);

                        if (!albums.contains(albumItem)) {
                            albums.add(albumItem);
                        }

                    } while (data.moveToNext());
                }

                Collections.sort(albums);

                adapter = new AlbumListAdapter(getContext(), albums);
                listView.setAdapter(adapter);

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_ALBUM_ID:

                break;
        }

    }

}
