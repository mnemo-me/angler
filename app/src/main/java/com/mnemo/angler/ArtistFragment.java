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
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.mnemo.angler.data.AnglerContract.*;

import static com.mnemo.angler.data.AnglerContract.BASE_CONTENT_URI;


public class ArtistFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    public ArtistFragment() {
        // Required empty public constructor
    }

    interface ArtistFragmentListener{
        void artistClicked(String artist);
    }

    private ArtistFragmentListener artistFragmentListener;

    private SimpleCursorAdapter adapter;

    Parcelable state;

    private static final int LOADER_ARTISTS_ID = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        artistFragmentListener = (ArtistFragmentListener) context;
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

        // Setup empty text
        setEmptyText("");
        TextView emptyText = (TextView) getListView().getEmptyView();
        emptyText.setTextSize(16);
        emptyText.setTextColor(getResources().getColor(R.color.gGrey, null));

        getLoaderManager().initLoader(LOADER_ARTISTS_ID, null, this);
        /*
        populating list view with artists
         */
        adapter = new SimpleCursorAdapter(
                getContext(), R.layout.alb_artist_item, null,
                new String[] {TrackEntry.COLUMN_ARTIST},
                new int[] {R.id.artist_item},0);
        getListView().setDividerHeight(0);
        getListView().setPadding(0, (int)(6 * MainActivity.density),0,0);
        getListView().setClipToPadding(false);
        getListView().setVerticalScrollBarEnabled(false);
        setListAdapter(adapter);

        if (state != null) {
            getListView().onRestoreInstanceState(state);
        }

        // Initialize broadcast receiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (intent.getAction()){

                    case "filter_applied":

                        getLoaderManager().restartLoader(LOADER_ARTISTS_ID, null, ArtistFragment.this);
                        break;
                }

            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("filter_applied");

        getContext().registerReceiver(receiver, intentFilter);

    }


    /*
    saving state of scroll
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
            case LOADER_ARTISTS_ID:
                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(BASE_CONTENT_URI, "artist_list/" + PlaylistManager.mainPlaylistName),
                        new String[] {"_id", TrackEntry.COLUMN_ARTIST},
                        TrackEntry.COLUMN_ARTIST + " LIKE ?",
                        new String[]{"%" + MainActivity.filter + "%"},
                        TrackEntry.COLUMN_ARTIST + " ASC");
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_ARTISTS_ID:
                adapter.swapCursor(data);

                /*
                move to selected artist tracks fragment
                 */
                getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        data.moveToPosition(i);
                        String artist = data.getString(1);

                        artistFragmentListener.artistClicked(artist);
                    }
                });

                if (!MainActivity.filter.equals("")) {
                    setEmptyText(getResources().getText(R.string.search_empty_artists));
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_ARTISTS_ID:
                adapter.swapCursor(null);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        getContext().unregisterReceiver(receiver);
    }
}
