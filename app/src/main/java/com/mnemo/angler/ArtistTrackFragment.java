package com.mnemo.angler;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;
import com.mnemo.angler.playlist_manager.PlaylistCursorAdapter;
import com.mnemo.angler.playlist_manager.Track;


public class ArtistTrackFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, PlaylistCursorAdapter.onTrackClickListener, PlaylistCursorAdapter.onTrackRemoveListener {

    public ArtistTrackFragment() {
        // Required empty public constructor
    }


    private MainPlaylistFragment.TrackFragmentListener trackListener;

    private String artist;
    private String localPlaylistName;

    private Parcelable state;

    private PlaylistCursorAdapter adapter;

    private static final int LOADER_ARTIST_PLAYLIST_ID = 0;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        trackListener = (MainPlaylistFragment.TrackFragmentListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null){
            state = savedInstanceState.getParcelable("state");
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // get artist
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            artist = arguments.getString("artist");
        }else{
            artist = savedInstanceState.getString("artist");
        }

        localPlaylistName = "playlist_artist/" + PlaylistManager.mainPlaylistName.replace("/", "\\") + "/" + artist.replace("/", "\\");

        // Setup empty text
        setEmptyText("");
        TextView emptyText = (TextView) getListView().getEmptyView();
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(R.color.gGrey, null));

        getLoaderManager().initLoader(LOADER_ARTIST_PLAYLIST_ID, null, this);


        getListView().setDividerHeight(0);
        getListView().setPadding(0, (int)(8 * MainActivity.density),0,(int)(8 * MainActivity.density));
        getListView().setClipToPadding(false);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setListAdapter(adapter);

        if (state != null) {
            getListView().onRestoreInstanceState(state);
        }

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){
                    case "track_changed":

                        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                            getListView().setItemChecked(PlaylistManager.position, true);
                        }

                        break;

                    case "filter_applied":

                        getLoaderManager().restartLoader(LOADER_ARTIST_PLAYLIST_ID, null, ArtistTrackFragment.this);
                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);


    }



    /*
    saving state of scroll
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            Parcelable state = getListView().onSaveInstanceState();
            outState.putParcelable("state", state);

        }catch(IllegalStateException e){
            e.printStackTrace();
        }

        outState.putString("artist", artist);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_ARTIST_PLAYLIST_ID:

                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, localPlaylistName),
                        null,
                        TrackEntry.COLUMN_TITLE + " LIKE ?",
                        new String[]{"%" + MainActivity.filter + "%"},
                        TrackEntry.COLUMN_TITLE + " ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_ARTIST_PLAYLIST_ID:

                adapter = new PlaylistCursorAdapter(getContext(), data, AnglerSQLiteDBHelper.createTrackTableName(PlaylistManager.mainPlaylistName), null);

                adapter.setOnTrackClickedListener(this);
                adapter.setOnTrackRemoveListener(this);

                if (!MainActivity.filter.equals("")) {
                    setEmptyText(getResources().getText(R.string.search_empty_tracks));
                }

                getListView().setAdapter(adapter);

                if (localPlaylistName.equals(PlaylistManager.currentPlaylistName) && MainActivity.filter.equals(PlaylistManager.playlistFilter)) {
                    getListView().setItemChecked(PlaylistManager.position, true);
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_ARTIST_PLAYLIST_ID:
                adapter.swapCursor(null);
        }
    }

    @Override
    public void onTrackClicked(int position) {

        PlaybackManager.isCurrentTrackHidden = false;

        PlaylistManager.currentPlaylistName = localPlaylistName;
        PlaylistManager.position = position;

        trackListener.trackClicked();

    }

    @Override
    public void onTrackRemove(int position, Track trackToRemove, boolean isCurrentTrack) {

    }


    @Override
    public void onResume() {
        super.onResume();

        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName) && MainActivity.filter.equals(PlaylistManager.playlistFilter)) {
            getListView().setItemChecked(PlaylistManager.position, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getContext().unregisterReceiver(receiver);
    }


}
