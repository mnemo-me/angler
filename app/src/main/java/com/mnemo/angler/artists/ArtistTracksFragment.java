package com.mnemo.angler.artists;


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

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.playlist_manager.PlaybackManager;
import com.mnemo.angler.playlist_manager.PlaylistManager;
import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerContract.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.mnemo.angler.data.AnglerContract.BASE_CONTENT_URI;


public class ArtistTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ArtistTrackCursorAdapter.onTrackClickListener {


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

                        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                            trackList.setItemChecked(PlaylistManager.position, true);
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
                        Uri.withAppendedPath(BASE_CONTENT_URI, SourceEntry.SOURCE_LIBRARY), null,
                        TrackEntry.COLUMN_ARTIST + " = ?", new String[]{artist},
                        TrackEntry.COLUMN_TITLE + " ASC");

            default:
                return  null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

            switch (loader.getId()){

                case LOADER_TRACK_LIST_ID:

                    adapter = new ArtistTrackCursorAdapter(getContext(), data);

                    adapter.setOnTrackClickedListener(this);

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
    public void onTrackClicked(int position) {

        PlaybackManager.isCurrentTrackHidden = false;

        if (!PlaylistManager.currentPlaylistName.equals("artist/" + artist)){
            PlaylistManager.currentPlaylistName = "artist/" + artist;
        }

        PlaylistManager.position = position;
        ((MainActivity)getActivity()).trackClicked();
    }


    @Override
    public void onResume() {
        super.onResume();
/*
        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
            trackList.setItemChecked(PlaylistManager.position, true);
        }*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getContext().unregisterReceiver(receiver);
        unbinder.unbind();
    }
}
