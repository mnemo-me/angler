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

    private String localPlaylistName;

    private Parcelable state;
    private String artist;

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

        /*
        get artist from artist fragment
         */
        Bundle arguments = getArguments();
        artist = arguments.getString("artist");
        trackListener.showBackgroundLabel(artist);

        localPlaylistName = "playlist_artist/" + PlaylistManager.mainPlaylistName.replace("/", "\\") + "/" + artist.replace("/", "\\");

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
                getListView().setItemChecked(PlaylistManager.position, true);
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("track_changed");


    }


    @Override
    public void onStop() {
        super.onStop();
        trackListener.hideBackgroundLabel();
        if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
            getListView().setItemChecked(PlaylistManager.position, false);
            if (receiver != null) {
                getContext().unregisterReceiver(receiver);
            }
        }
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
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_ARTIST_PLAYLIST_ID:
                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, localPlaylistName),
                        null,
                        null, null,
                        TrackEntry.COLUMN_TITLE + " ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_ARTIST_PLAYLIST_ID:

                adapter = new PlaylistCursorAdapter(getContext(), data, "main", PlaylistManager.mainPlaylistName, AnglerSQLiteDBHelper.createTrackTableName(PlaylistManager.mainPlaylistName), null);

                adapter.setOnTrackClickedListener(this);
                adapter.setOnTrackRemoveListener(this);

                getListView().setAdapter(adapter);

                if (localPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                    getListView().setItemChecked(PlaylistManager.position, true);
                    getContext().registerReceiver(receiver, intentFilter);
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

        PlaylistManager.currentPlaylistName = "playlist_artist/" + PlaylistManager.mainPlaylistName.replace("/", "\\") + "/" + artist.replace("/", "\\");
        PlaylistManager.position = position;

        getContext().registerReceiver(receiver, intentFilter);

        trackListener.trackClicked();

    }

    @Override
    public void onTrackRemove(int position, Track trackToRemove, boolean isCurrentTrack) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }
}
