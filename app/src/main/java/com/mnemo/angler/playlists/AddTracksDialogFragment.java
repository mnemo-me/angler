package com.mnemo.angler.playlists;


import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerContract;
import com.mnemo.angler.data.AnglerContract.*;

import java.util.ArrayList;
import java.util.Collections;

import static com.mnemo.angler.data.AnglerContract.BASE_CONTENT_URI;


public class AddTracksDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int LOADER_ADD_TRACKS_ID = 0;
    private static final int LOADER_ALREADY_ADDED_TRACKS_ID = 1;

    ExpandableListView expandableListView;
    AddTracksExpandableListAdapter adapter;

    ArrayList<Track> tracksToAdd;
    ArrayList<String> alreadyAddedTracksIds;
    String dbName;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        dbName = getArguments().getString("db_name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_dialog_title, null, false);
        TextView title = titleLayout.findViewById(R.id.dialog_title);
        title.setText(R.string.add_tracks_to_playlist);

        builder.setCustomTitle(titleLayout);

        expandableListView = new ExpandableListView(getContext());
        expandableListView.setDividerHeight(0);
        expandableListView.setSelector(android.R.color.transparent);

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            expandableListView.addHeaderView(LayoutInflater.from(getContext()).inflate(R.layout.pm_add_tracks_header, null, false));
            expandableListView.addFooterView(LayoutInflater.from(getContext()).inflate(R.layout.pm_add_tracks_header, null, false));
        }

        getLoaderManager().initLoader(LOADER_ADD_TRACKS_ID, null, this);

        builder.setView(expandableListView);

        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ContentValues contentValues = new ContentValues();

                int currentPlaylistSize = alreadyAddedTracksIds.size();

                for (Track track : adapter.getNewTracks()) {

                    contentValues.put("_id", track.getId());
                    contentValues.put(TrackEntry.COLUMN_TITLE, track.getTitle());
                    contentValues.put(TrackEntry.COLUMN_ARTIST, track.getArtist());
                    contentValues.put(TrackEntry.COLUMN_ALBUM, track.getAlbum());
                    contentValues.put(TrackEntry.COLUMN_DURATION, track.getDuration());
                    contentValues.put(TrackEntry.COLUMN_URI, track.getUri());
                    contentValues.put(TrackEntry.COLUMN_SOURCE, track.getSource());
                    contentValues.put(TrackEntry.COLUMN_POSITION, ++currentPlaylistSize);

                    getActivity().getContentResolver().insert(Uri.withAppendedPath(AnglerContract.BASE_CONTENT_URI, dbName), contentValues);

                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        return builder.create();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_ADD_TRACKS_ID:
                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(BASE_CONTENT_URI, SourceEntry.SOURCE_LIBRARY), null,
                        null,null,null);
            case LOADER_ALREADY_ADDED_TRACKS_ID:
                return new CursorLoader(getContext(),
                        Uri.withAppendedPath(BASE_CONTENT_URI,dbName), null,
                        null, null, null);
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case LOADER_ADD_TRACKS_ID:

                ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                tracksToAdd = new ArrayList<>();

                data.moveToFirst();

                do {
                    tracksToAdd.add(new Track(data.getString(0), data.getString(1), data.getString(2), data.getString(3),
                            data.getLong(4), data.getString(5), data.getString(6)));
                } while (data.moveToNext());

                Collections.sort(tracksToAdd);

                getLoaderManager().initLoader(LOADER_ALREADY_ADDED_TRACKS_ID, null, this);

                break;

            case LOADER_ALREADY_ADDED_TRACKS_ID:

                alreadyAddedTracksIds = new ArrayList<>();

                if (data.getCount() != 0) {
                    data.moveToFirst();

                    do {
                        alreadyAddedTracksIds.add(data.getString(0));
                    } while (data.moveToNext());

                    for (Track track : tracksToAdd) {
                        if (alreadyAddedTracksIds.contains(track.getId())) {
                            track.setAlreadyAdded(true);
                        }
                    }
                }

                adapter = new AddTracksExpandableListAdapter(getContext(), tracksToAdd);


                adapter.setOnTrackCountChangeListener(new AddTracksExpandableListAdapter.OnTrackCountChangeListener() {
                    @Override
                    public void onTrackCountChange() {
                        if (adapter.getNewTracks().size() == 0){
                            ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }else{
                            if (!((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) {
                                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                        }
                    }
                });

                expandableListView.setAdapter(adapter);

                for (int i = 0; i < adapter.getGroupCount(); i++){
                    expandableListView.expandGroup(i);
                }

                expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {

                        if (expandableListView.isGroupExpanded(i)){
                            expandableListView.collapseGroup(i);
                        }else{
                            expandableListView.expandGroup(i);
                        }

                        return true;
                    }
                });

                break;

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //AnglerApplication.getRefWatcher(getActivity()).watch(this);
    }
}
