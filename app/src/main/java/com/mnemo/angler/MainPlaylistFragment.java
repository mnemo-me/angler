package com.mnemo.angler;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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


public class MainPlaylistFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, PlaylistCursorAdapter.onTrackClickListener, PlaylistCursorAdapter.onTrackRemoveListener {


    public MainPlaylistFragment() {
        // Required empty public constructor
    }

    interface TrackFragmentListener {
        void trackClicked();
        void showBackgroundLabel(String label);
        void hideBackgroundLabel();
    }

    private TrackFragmentListener trackListener;

    private PlaylistCursorAdapter adapter;

    private Parcelable state;

    private static final int LOADER_PLAYLIST_ID = 0;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        trackListener = (TrackFragmentListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /*
        restore scrolls state
         */
        if (savedInstanceState != null){
            state = savedInstanceState.getParcelable("state");
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup empty text
        setEmptyText("");
        TextView emptyText = (TextView) getListView().getEmptyView();
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(R.color.gGrey, null));

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

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

                        if (PlaylistManager.mainPlaylistName.equals(PlaylistManager.currentPlaylistName)) {
                            getListView().setItemChecked(PlaylistManager.position, true);
                        }

                        break;

                    case "filter_applied":

                        getLoaderManager().restartLoader(LOADER_PLAYLIST_ID, null, MainPlaylistFragment.this);
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
    saving state of scrolls
    */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        try{
            Parcelable state = getListView().onSaveInstanceState();
            outState.putParcelable("state", state);


        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_PLAYLIST_ID:
                    return new CursorLoader(getContext(),
                            Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, AnglerSQLiteDBHelper.createTrackTableName(PlaylistManager.mainPlaylistName)),
                            null,
                            TrackEntry.COLUMN_TITLE + " LIKE ?",
                            new String[]{"%" + MainActivity.filter + "%"},
                            TrackEntry.COLUMN_POSITION + " ASC, " + TrackEntry.COLUMN_TITLE + " ASC, " + TrackEntry.COLUMN_ARTIST + " ASC");
                default:
                    return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){

            case LOADER_PLAYLIST_ID:

                adapter = new PlaylistCursorAdapter(getContext(), data, "main", PlaylistManager.mainPlaylistName, AnglerSQLiteDBHelper.createTrackTableName(PlaylistManager.mainPlaylistName), null);

                adapter.setOnTrackClickedListener(this);
                adapter.setOnTrackRemoveListener(this);

                if (MainActivity.filter.equals("")) {
                    setEmptyText(getResources().getText(R.string.empty_playlist));
                }else{
                    setEmptyText(getResources().getText(R.string.search_empty_tracks));
                }

                getListView().setAdapter(adapter);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(null);
        }
    }

    @Override
    public void onTrackClicked(int position) {

        PlaybackManager.isCurrentTrackHidden = false;

        PlaylistManager.currentPlaylistName = PlaylistManager.mainPlaylistName;
        PlaylistManager.position = position;

        trackListener.trackClicked();

    }

    @Override
    public void onTrackRemove(int position, Track trackToRemove, boolean isCurrentTrack) {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        getContext().unregisterReceiver(receiver);
    }
}
