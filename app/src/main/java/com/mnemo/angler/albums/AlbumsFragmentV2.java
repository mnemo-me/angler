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
import android.widget.ListView;

import com.mnemo.angler.DrawerItem;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.SourceEntry;
import com.mnemo.angler.data.AnglerContract.TrackEntry;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class AlbumsFragmentV2 extends Fragment implements DrawerItem, LoaderManager.LoaderCallbacks<Cursor> {


    public AlbumsFragmentV2() {
        // Required empty public constructor
    }

    public static final int LOADER_ALBUM_ID = 0;

    @BindView(R.id.albums_list)
    ListView listView;

    AlbumListAdapter adapter;

    ArrayList<Album> albums;

    Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.alb_fragment_albums, container, false);

        unbinder = ButterKnife.bind(this, view);

        albums = new ArrayList<>();

        // Setup complex listview (albums inside artists)
        listView.setDividerHeight(0);
        listView.addHeaderView(LayoutInflater.from(getContext()).inflate(R.layout.alb_album_list_header, null, false));
        listView.addFooterView(LayoutInflater.from(getContext()).inflate(R.layout.alb_album_list_header, null, false));

        getLoaderManager().initLoader(LOADER_ALBUM_ID, null, this);


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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unbinder.unbind();
    }

    // Setup drawer menu button
    @OnClick(R.id.albums_drawer_back)
    void drawerBack(){
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).openDrawer(Gravity.START);
    }

}
