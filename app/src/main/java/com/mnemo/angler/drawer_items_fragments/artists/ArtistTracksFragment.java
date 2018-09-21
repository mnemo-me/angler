package com.mnemo.angler.drawer_items_fragments.artists;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.database.AnglerContract.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.mnemo.angler.data.database.AnglerContract.BASE_CONTENT_URI;


public class ArtistTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    public ArtistTracksFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.artist_tracks_list)
    ListView trackList;

    ArtistTrackCursorAdapter adapter;

    String artist;
    String localPlaylistName;
    private static final int LOADER_TRACK_LIST_ID = 0;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    Unbinder unbinder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.art_fragment_artist_tracks, container, false);

        unbinder = ButterKnife.bind(this, view);

        // Get artist
        artist = getArguments().getString("artist");

        localPlaylistName = "artist/" + artist;

        trackList.setDividerHeight(0);

        // Load tracks
        getLoaderManager().initLoader(LOADER_TRACK_LIST_ID, null, this);


        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        String trackPlaylist = intent.getStringExtra("track_playlist");
                        String mediaId = intent.getStringExtra("media_id");

                        if (trackPlaylist.equals(localPlaylistName)) {
                            try {
                                trackList.setItemChecked(trackList.getPositionForView(trackList.findViewWithTag(mediaId)), true);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
           break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");

        getContext().registerReceiver(receiver, intentFilter);

        return view;
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        switch (id) {
            case LOADER_TRACK_LIST_ID:

                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(BASE_CONTENT_URI, PlaylistEntry.LIBRARY), null,
                        TrackEntry.COLUMN_ARTIST + " = ?", new String[]{artist},
                        TrackEntry.COLUMN_TITLE + " ASC");

            default:
                return  null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, final Cursor data) {

            switch (loader.getId()){

                case LOADER_TRACK_LIST_ID:

                    adapter = new ArtistTrackCursorAdapter(getContext(), data, localPlaylistName);


                    trackList.setAdapter(adapter);


        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_TRACK_LIST_ID:

        }
    }




    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getContext().unregisterReceiver(receiver);
        unbinder.unbind();
    }
}
