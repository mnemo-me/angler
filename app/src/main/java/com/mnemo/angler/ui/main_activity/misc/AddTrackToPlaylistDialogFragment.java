package com.mnemo.angler.ui.main_activity.misc;


import android.app.Dialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mnemo.angler.ui.main_activity.activity.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.database.AnglerSQLiteDBHelper;
import com.mnemo.angler.ui.main_activity.fragments.playlists.playlist_create.PlaylistCreationDialogFragment;
import com.mnemo.angler.utils.ImageAssistant;
import com.mnemo.angler.data.database.AnglerContract.*;

public class AddTrackToPlaylistDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    public static final int LOADER_PLAYLIST_ID = 0;

    private GridView playlistGrid;
    SimpleCursorAdapter adapter;

    String mediaId;
    String title;
    String artist;
    String album;
    long duration;
    String uri;

    AnglerSQLiteDBHelper dbHelper;
    SQLiteDatabase db;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Assign track variables
        mediaId = getArguments().getString("media_id");
        title = getArguments().getString("title");
        artist = getArguments().getString("artist");
        album = getArguments().getString("album");
        duration = getArguments().getLong("duration");
        uri = getArguments().getString("uri");

        dbHelper = new AnglerSQLiteDBHelper(getContext());
        db = dbHelper.getWritableDatabase();


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_dialog_title, null, false);
        TextView title = titleLayout.findViewById(R.id.dialog_title);
        title.setText(R.string.add_track_to_playlist);

        builder.setCustomTitle(titleLayout);

        // Setup playlist gridview with adapter
        playlistGrid = new GridView(getActivity());

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            playlistGrid.setNumColumns(2);
        }else{
            playlistGrid.setNumColumns(3);
        }

        playlistGrid.setVerticalSpacing((int) (8 * MainActivity.density));
        playlistGrid.setPadding((int)(8 * MainActivity.density), (int)(8 * MainActivity.density), (int)(8 * MainActivity.density), (int)(8 * MainActivity.density));
        playlistGrid.setClipToPadding(false);
        playlistGrid.setGravity(Gravity.CENTER);

        adapter = new SimpleCursorAdapter(getContext(),R.layout.pm_playlist_v2_mod, null,
                new String[]{PlaylistEntry.COLUMN_IMAGE_RESOURCE, PlaylistEntry.COLUMN_NAME, PlaylistEntry.COLUMN_TRACKS_TABLE},
                new int[]{R.id.playlist_options_image, R.id.playlist_options_name},0);

        adapter.setViewBinder((view, cursor, i) -> {

            if (view.getId() == R.id.playlist_options_image){
                ImageAssistant.loadImage(getContext(),cursor.getString(2), (ImageView) view, 125);
                return true;
            }
            return false;
        });

        getLoaderManager().initLoader(LOADER_PLAYLIST_ID, null, this);

        playlistGrid.setAdapter(adapter);

        builder.setView(playlistGrid);


        builder.setNegativeButton(R.string.close, (dialogInterface, i) -> {

        });

        builder.setNeutralButton(R.string.create_new_playlist, (dialogInterface, i) -> {

            PlaylistCreationDialogFragment playlistCreationDialogFragment = new PlaylistCreationDialogFragment();

            Bundle args = getArguments();
            args.putString("action", "create");

            playlistCreationDialogFragment.setArguments(getArguments());

            playlistCreationDialogFragment.show(getActivity().getSupportFragmentManager(), "playlist_creation_dialog_fragment");

        });

        return builder.create();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case LOADER_PLAYLIST_ID:
                return new CursorLoader(getContext(),
                        PlaylistEntry.CONTENT_URI, null,
                        PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " = 0", null,
                        PlaylistEntry.COLUMN_DEFAULT_PLAYLIST + " DESC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(data);

                playlistGrid.setOnItemClickListener((parent, view, position, id) -> {

                    data.moveToPosition(position);

                    Cursor cursor = db.query(data.getString(3),new String[]{"COUNT(_id) As Count"},null, null, null, null, null);
                    cursor.moveToFirst();
                    int trackPosition = Integer.parseInt(cursor.getString(0)) + 1;
                    cursor.close();

                    dbHelper.insertTrack(db, data.getString(3), title, artist, album, duration, uri, trackPosition);

                    Toast.makeText(getContext(), "'" + artist + " - " + title + "' " + getString(R.string.added_to) + " '" + data.getString(1) + "'", Toast.LENGTH_SHORT).show();

                    dismiss();

                });
                break;

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()){
            case LOADER_PLAYLIST_ID:
                adapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog)getDialog();

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            alertDialog.getWindow().setLayout((int) (320 * MainActivity.density), ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (db != null){
            db.close();
        }
    }
}
